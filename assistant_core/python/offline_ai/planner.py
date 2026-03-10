from __future__ import annotations

import json
from pathlib import Path

from .models import ProjectPlan


def load_material_catalog(catalog_path: Path) -> dict:
    with catalog_path.open("r", encoding="utf-8") as file:
        return json.load(file)


def suggest_items_for_goal(goal: str, catalog: dict) -> ProjectPlan:
    goal_lc = goal.lower()
    suggested_items: list[str] = []
    steps: list[str] = []
    cautions: list[str] = []

    keyword_to_category = {
        "battery": "electronics",
        "motor": "mechanics",
        "drone": "aerodynamics",
        "chem": "chemistry",
        "circuit": "electronics",
        "water": "chemistry",
        "rocket": "aerodynamics",
    }

    for keyword, category in keyword_to_category.items():
        if keyword in goal_lc and category in catalog:
            suggested_items.extend(catalog[category].get("items", []))
            steps.extend(catalog[category].get("default_steps", []))
            cautions.extend(catalog[category].get("cautions", []))

    if not suggested_items:
        suggested_items = catalog.get("general", {}).get("items", [])
        steps = catalog.get("general", {}).get("default_steps", [])
        cautions = catalog.get("general", {}).get("cautions", [])

    # Preserve order but remove duplicates.
    deduped_items = list(dict.fromkeys(suggested_items))
    deduped_steps = list(dict.fromkeys(steps))
    deduped_cautions = list(dict.fromkeys(cautions))

    return ProjectPlan(
        goal=goal,
        recommended_items=deduped_items,
        steps=deduped_steps,
        cautions=deduped_cautions,
    )
