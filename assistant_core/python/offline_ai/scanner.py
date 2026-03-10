from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

from .models import IdentifiedItem, ScanRequest


@dataclass(slots=True)
class ScanAliasMatcher:
    """Lightweight offline identifier from image labels or text hints."""

    aliases: dict[str, list[str]]

    def identify(self, request: ScanRequest) -> IdentifiedItem | None:
        tokens = [token.strip().lower() for token in request.image_labels if token.strip()]
        if request.typed_hint.strip():
            tokens.extend(request.typed_hint.lower().split())

        if not tokens:
            return None

        best_name = ""
        best_hits: list[str] = []
        best_score = 0

        for canonical, alias_list in self.aliases.items():
            hits = [alias for alias in alias_list if any(alias in token for token in tokens)]
            if len(hits) > best_score:
                best_name = canonical
                best_hits = hits
                best_score = len(hits)

        if not best_name:
            return None

        confidence = min(0.99, 0.45 + 0.15 * best_score)
        return IdentifiedItem(
            canonical_name=best_name,
            confidence=confidence,
            matched_aliases=sorted(set(best_hits)),
        )


def load_scan_aliases(path: Path) -> dict[str, list[str]]:
    with path.open("r", encoding="utf-8") as file:
        payload = json.load(file)
    return payload["aliases"]
