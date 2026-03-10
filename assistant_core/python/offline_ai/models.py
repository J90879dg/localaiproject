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


@dataclass(slots=True)
class ScanRequest:
    image_labels: list[str] = field(default_factory=list)
    typed_hint: str = ""
    include_second_hand: bool = True
    region: str = "us"


@dataclass(slots=True)
class IdentifiedItem:
    canonical_name: str
    confidence: float
    matched_aliases: list[str] = field(default_factory=list)


@dataclass(slots=True)
class ValuationResult:
    item_name: str
    region: str
    currency: str
    low_estimate: float
    mid_estimate: float
    high_estimate: float
    data_timestamp_unix: int
    source: str
    assumptions: list[str] = field(default_factory=list)
    notes: list[str] = field(default_factory=list)
