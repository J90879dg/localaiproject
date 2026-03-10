from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any


@dataclass(slots=True)
class ExperimentRequest:
    user_goal: str
    domain: str
    reactants: list[str] = field(default_factory=list)
    chemical_name: str | None = None
    image_labels: list[str] = field(default_factory=list)
    conditions: dict[str, float] = field(default_factory=dict)


@dataclass(slots=True)
class SimulationResult:
    domain: str
    summary: str
    outputs: dict[str, Any]
    assumptions: list[str] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)


@dataclass(slots=True)
class ProjectPlan:
    goal: str
    recommended_items: list[str]
    steps: list[str]
    cautions: list[str] = field(default_factory=list)
