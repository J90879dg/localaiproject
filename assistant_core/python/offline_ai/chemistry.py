from __future__ import annotations

from typing import Any

R_IDEAL_GAS = 0.082057  # L·atm/(mol·K)

_KNOWN_REACTIONS: dict[frozenset[str], dict[str, Any]] = {
    frozenset({"hcl", "naoh"}): {
        "equation": "HCl + NaOH -> NaCl + H2O",
        "category": "neutralization",
        "products": ["NaCl", "H2O"],
        "hazard_level": "low",
    },
    frozenset({"h2", "o2"}): {
        "equation": "2H2 + O2 -> 2H2O",
        "category": "combustion",
        "products": ["H2O"],
        "hazard_level": "high",
    },
    frozenset({"agno3", "nacl"}): {
        "equation": "AgNO3 + NaCl -> AgCl + NaNO3",
        "category": "precipitation",
        "products": ["AgCl", "NaNO3"],
        "hazard_level": "medium",
    },
}


def estimate_gas_pressure_atm(moles: float, temperature_k: float, volume_l: float) -> float:
    """Estimate pressure from ideal gas law (P=nRT/V)."""
    if moles <= 0:
        raise ValueError("moles must be positive")
    if temperature_k <= 0:
        raise ValueError("temperature_k must be positive")
    if volume_l <= 0:
        raise ValueError("volume_l must be positive")
    return (moles * R_IDEAL_GAS * temperature_k) / volume_l


def concentration_molar(solute_moles: float, solution_volume_l: float) -> float:
    """Compute molar concentration M=mol/L."""
    if solute_moles < 0:
        raise ValueError("solute_moles cannot be negative")
    if solution_volume_l <= 0:
        raise ValueError("solution_volume_l must be positive")
    return solute_moles / solution_volume_l


def predict_reaction(reactants: list[str], conditions: dict[str, float] | None = None) -> dict[str, Any]:
    """Predict a reaction outcome using a deterministic offline rule-set."""
    normalized = frozenset(item.strip().lower() for item in reactants if item.strip())
    baseline = {
        "equation": "No confident reaction prediction",
        "category": "unknown",
        "products": [],
        "hazard_level": "unknown",
    }
    reaction = _KNOWN_REACTIONS.get(normalized, baseline).copy()

    conditions = conditions or {}
    temp_c = conditions.get("temperature_c")
    if temp_c is not None and temp_c > 150:
        reaction["hazard_level"] = "high"
        reaction["note"] = "Elevated temperature increases risk; use controlled simulation."

    return reaction
