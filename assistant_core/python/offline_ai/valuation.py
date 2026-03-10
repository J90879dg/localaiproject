from __future__ import annotations

import json
from dataclasses import dataclass
from pathlib import Path

from .models import ValuationResult


@dataclass(slots=True)
class MarketSnapshot:
    source: str
    timestamp_unix: int
    regions: dict


def load_market_snapshot(snapshot_path: Path) -> MarketSnapshot:
    with snapshot_path.open("r", encoding="utf-8") as file:
        payload = json.load(file)
    return MarketSnapshot(
        source=payload["source"],
        timestamp_unix=payload["timestamp_unix"],
        regions=payload["regions"],
    )


def estimate_item_worth(
    item_name: str,
    snapshot: MarketSnapshot,
    region: str = "us",
    include_second_hand: bool = True,
) -> ValuationResult:
    normalized = item_name.strip().lower()
    region_data = snapshot.regions.get(region.lower()) or snapshot.regions.get("us", {})
    currency = region_data.get("currency", "USD")

    items = region_data.get("items", {})
    item = items.get(normalized)
    if item is None:
        unknown_mid = 0.0
        return ValuationResult(
            item_name=item_name,
            region=region,
            currency=currency,
            low_estimate=unknown_mid,
            mid_estimate=unknown_mid,
            high_estimate=unknown_mid,
            data_timestamp_unix=snapshot.timestamp_unix,
            source=snapshot.source,
            assumptions=[
                "No direct catalog entry found in offline snapshot.",
                "Connect an updated local snapshot file for fresher estimates.",
            ],
            notes=["Unknown item in current market snapshot."],
        )

    new_mid = float(item["new_mid"])
    used_mid = float(item.get("used_mid", new_mid))
    mid = used_mid if include_second_hand else new_mid
    spread = float(item.get("spread_pct", 0.2))
    low = max(0.0, mid * (1.0 - spread))
    high = mid * (1.0 + spread)

    notes = [f"Category: {item.get('category', 'general')}"]
    if include_second_hand and "used_mid" in item:
        notes.append("Second-hand pricing model enabled.")
    else:
        notes.append("New-item pricing model enabled.")

    return ValuationResult(
        item_name=item_name,
        region=region,
        currency=currency,
        low_estimate=round(low, 2),
        mid_estimate=round(mid, 2),
        high_estimate=round(high, 2),
        data_timestamp_unix=snapshot.timestamp_unix,
        source=snapshot.source,
        assumptions=[
            "Prices are estimates from the local market snapshot.",
            "True current worth depends on condition, brand, and local demand.",
        ],
        notes=notes,
    )
