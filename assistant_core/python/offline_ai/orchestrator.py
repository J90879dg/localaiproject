from __future__ import annotations

from pathlib import Path
from typing import Any

from .chemistry import estimate_gas_pressure_atm, predict_reaction
from .models import ExperimentRequest, SimulationResult
from .physics import (
    drag_force_newtons,
    electric_current_amps,
    kinetic_displacement_m,
    lorentz_factor,
)
from .planner import load_material_catalog, suggest_items_for_goal
from .safety import evaluate_chemistry_safety


class OfflineAssistantOrchestrator:
    """Offline orchestrator for simulation and project planning requests."""

    def __init__(self, catalog_path: Path):
        self.catalog = load_material_catalog(catalog_path)

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
