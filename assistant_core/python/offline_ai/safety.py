from __future__ import annotations

from typing import Any


def evaluate_chemistry_safety(reactants: list[str], conditions: dict[str, float] | None = None) -> dict[str, Any]:
    reactants_lc = [item.lower().strip() for item in reactants]
    conditions = conditions or {}

    score = 0
    warnings: list[str] = []
    protocols: list[str] = [
        "Use gloves, goggles, and lab coat.",
        "Work in a ventilated environment.",
    ]

    if any(item in {"h2", "oxygen", "o2", "acetone"} for item in reactants_lc):
        score += 2
        warnings.append("Flammable/explosive risk detected.")
        protocols.append("Keep ignition sources away from reaction area.")

    if any(item in {"hcl", "h2so4", "naoh", "nh3"} for item in reactants_lc):
        score += 2
        warnings.append("Corrosive material risk detected.")
        protocols.append("Prepare neutralization and spill-control materials.")

    temperature_c = conditions.get("temperature_c", 25.0)
    if temperature_c > 80:
        score += 1
        warnings.append("High temperature increases reaction hazard.")
        protocols.append("Use thermal shielding and controlled heat ramp.")

    pressure_atm = conditions.get("pressure_atm", 1.0)
    if pressure_atm > 2.0:
        score += 1
        warnings.append("Elevated pressure condition detected.")
        protocols.append("Use pressure-rated containers and shields.")

    if score >= 4:
        level = "high"
    elif score >= 2:
        level = "medium"
    else:
        level = "low"

    return {
        "hazard_level": level,
        "warnings": list(dict.fromkeys(warnings)),
        "protocols": list(dict.fromkeys(protocols)),
    }
