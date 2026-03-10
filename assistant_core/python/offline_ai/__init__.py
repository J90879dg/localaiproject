"""Offline AI core package for virtual lab assistant."""

from .chemistry import concentration_molar, estimate_gas_pressure_atm, predict_reaction
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
from .safety import evaluate_chemistry_safety

__all__ = [
    "CommandEnvelope",
    "OfflineAssistantOrchestrator",
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
    "predict_reaction",
    "sign_envelope",
    "suggest_items_for_goal",
    "verify_envelope",
]
