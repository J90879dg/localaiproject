"""Offline AI core package for virtual lab assistant."""

from .chemistry import concentration_molar, estimate_gas_pressure_atm, predict_reaction
from .daily_update_service import DailyUpdateState, load_state, run_daily_update_if_due, save_state
from .market_updater import SnapshotUpdateResult, merge_market_feed, refresh_market_snapshot
from .orchestrator import OfflineAssistantOrchestrator
from .physics import (
    de_broglie_wavelength_m,
    drag_force_newtons,
    electric_current_amps,
    kinetic_displacement_m,
    lorentz_factor,
    magnetic_force_newtons,
)
from .planner import load_material_catalog, suggest_items_for_goal
from .protocol import CommandEnvelope, sign_envelope, verify_envelope
from .scanner import ScanAliasMatcher, load_scan_aliases
from .safety import evaluate_chemistry_safety
from .update_scheduler import DailyMidnightUpdatePolicy, NightlyUpdatePolicy
from .valuation import estimate_item_worth, load_market_snapshot

__all__ = [
    "CommandEnvelope",
    "DailyMidnightUpdatePolicy",
    "DailyUpdateState",
    "OfflineAssistantOrchestrator",
    "SnapshotUpdateResult",
    "concentration_molar",
    "de_broglie_wavelength_m",
    "drag_force_newtons",
    "electric_current_amps",
    "estimate_gas_pressure_atm",
    "evaluate_chemistry_safety",
    "kinetic_displacement_m",
    "load_material_catalog",
    "lorentz_factor",
    "magnetic_force_newtons",
    "merge_market_feed",
    "predict_reaction",
    "refresh_market_snapshot",
    "run_daily_update_if_due",
    "save_state",
    "sign_envelope",
    "ScanAliasMatcher",
    "suggest_items_for_goal",
    "verify_envelope",
    "estimate_item_worth",
    "load_state",
    "load_market_snapshot",
    "load_scan_aliases",
    "NightlyUpdatePolicy",
]
