from __future__ import annotations

from dataclasses import asdict
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from .chemistry import estimate_gas_pressure_atm, predict_reaction
from .daily_update_service import run_daily_update_if_due
from .market_updater import SnapshotUpdateResult
from .models import ExperimentRequest, ScanRequest, SimulationResult
from .physics import (
    drag_force_newtons,
    electric_current_amps,
    kinetic_displacement_m,
    lorentz_factor,
)
from .planner import load_material_catalog, suggest_items_for_goal
from .scanner import ScanAliasMatcher, load_scan_aliases
from .safety import evaluate_chemistry_safety
from .update_scheduler import DailyMidnightUpdatePolicy
from .valuation import estimate_item_worth, load_market_snapshot


class OfflineAssistantOrchestrator:
    """Offline orchestrator for simulation and project planning requests."""

    def __init__(
        self,
        catalog_path: Path,
        market_snapshot_path: Path | None = None,
        scan_aliases_path: Path | None = None,
    ):
        self.catalog_path = catalog_path
        self.market_snapshot_path = market_snapshot_path
        self.catalog = load_material_catalog(catalog_path)
        self.market_snapshot = (
            load_market_snapshot(market_snapshot_path) if market_snapshot_path else None
        )
        aliases = load_scan_aliases(scan_aliases_path) if scan_aliases_path else {}
        self.scan_matcher = ScanAliasMatcher(aliases=aliases)

    def plan_project(self, goal: str) -> dict[str, Any]:
        plan = suggest_items_for_goal(goal, self.catalog)
        return {
            "goal": plan.goal,
            "recommended_items": plan.recommended_items,
            "steps": plan.steps,
            "cautions": plan.cautions,
        }

    def run_experiment(self, request: ExperimentRequest) -> SimulationResult:
        domain = request.domain.lower().strip()
        if domain == "chemistry":
            return self._run_chemistry(request)
        if domain == "physics":
            return self._run_physics(request)
        return SimulationResult(
            domain=request.domain,
            summary="Unsupported domain in current offline baseline",
            outputs={},
            assumptions=["Domain router currently supports chemistry and physics."],
            warnings=[],
        )

    def scan_and_value(self, request: ScanRequest) -> dict[str, Any]:
        identified = self.scan_matcher.identify(request)
        if identified is None:
            return {
                "identified_item": None,
                "valuation": None,
                "message": "Unable to confidently identify item from current labels/hints.",
            }

        if self.market_snapshot is None:
            return {
                "identified_item": asdict(identified),
                "valuation": None,
                "message": "No local market snapshot configured.",
            }

        valuation = estimate_item_worth(
            item_name=identified.canonical_name,
            snapshot=self.market_snapshot,
            region=request.region,
            include_second_hand=request.include_second_hand,
        )
        return {
            "identified_item": asdict(identified),
            "valuation": asdict(valuation),
            "message": "Offline scan + worth estimation completed.",
        }

    def refresh_market_data_if_due(
        self,
        state_path: Path,
        feed_paths: list[Path],
        now: datetime | None = None,
        policy: DailyMidnightUpdatePolicy | None = None,
    ) -> SnapshotUpdateResult | None:
        if self.market_snapshot_path is None:
            return None
        if policy is None:
            local_tz = datetime.now(tz=timezone.utc).astimezone().tzinfo or timezone.utc
            policy = DailyMidnightUpdatePolicy(hour=0, minute=0, tz=local_tz)
        result = run_daily_update_if_due(
            policy=policy,
            state_path=state_path,
            snapshot_path=self.market_snapshot_path,
            feed_paths=feed_paths,
            now=now,
        )
        if result is not None:
            self.market_snapshot = load_market_snapshot(self.market_snapshot_path)
        return result

    def _run_chemistry(self, request: ExperimentRequest) -> SimulationResult:
        reaction = predict_reaction(request.reactants, request.conditions)
        safety = evaluate_chemistry_safety(request.reactants, request.conditions)
        outputs: dict[str, Any] = {
            "reaction": reaction,
            "safety": safety,
        }

        temp_k = request.conditions.get("temperature_k")
        moles = request.conditions.get("moles")
        volume_l = request.conditions.get("volume_l")
        if temp_k and moles and volume_l:
            outputs["estimated_pressure_atm"] = estimate_gas_pressure_atm(
                moles=moles,
                temperature_k=temp_k,
                volume_l=volume_l,
            )

        return SimulationResult(
            domain="chemistry",
            summary="Offline chemistry prediction completed.",
            outputs=outputs,
            assumptions=[
                "Reaction prediction is based on a deterministic local rule set.",
                "Gas behavior uses ideal-gas approximation.",
            ],
            warnings=safety["warnings"],
        )

    def _run_physics(self, request: ExperimentRequest) -> SimulationResult:
        c = request.conditions
        displacement = kinetic_displacement_m(
            initial_velocity_mps=c.get("initial_velocity_mps", 0.0),
            acceleration_mps2=c.get("acceleration_mps2", 0.0),
            time_s=c.get("time_s", 0.0),
        )
        current, power = electric_current_amps(
            voltage_v=c.get("voltage_v", 0.0),
            resistance_ohm=max(c.get("resistance_ohm", 1.0), 1e-9),
        )
        drag = drag_force_newtons(
            fluid_density_kg_m3=max(c.get("fluid_density_kg_m3", 1.225), 1e-9),
            velocity_mps=c.get("velocity_mps", 0.0),
            drag_coefficient=max(c.get("drag_coefficient", 0.47), 1e-9),
            reference_area_m2=max(c.get("reference_area_m2", 0.01), 1e-9),
        )
        gamma = lorentz_factor(velocity_mps=min(c.get("rel_velocity_mps", 0.0), 299_792_457.0))

        return SimulationResult(
            domain="physics",
            summary="Offline physics simulation completed.",
            outputs={
                "displacement_m": displacement,
                "current_a": current,
                "power_w": power,
                "drag_force_n": drag,
                "lorentz_factor": gamma,
            },
            assumptions=[
                "Mechanics uses constant-acceleration kinematics.",
                "Circuit model uses Ohm's law for a single resistor.",
                "Relativity output is scalar Lorentz factor.",
            ],
            warnings=[],
        )
