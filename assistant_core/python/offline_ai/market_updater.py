from __future__ import annotations

import json
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


@dataclass(slots=True)
class SnapshotUpdateResult:
    updated_items: int
    updated_regions: int
    output_path: Path
    timestamp_unix: int
    source: str


def load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as file:
        return json.load(file)


def write_json(path: Path, payload: dict[str, Any]) -> None:
    with path.open("w", encoding="utf-8") as file:
        json.dump(payload, file, indent=2, sort_keys=True)
        file.write("\n")


def merge_market_feed(base_snapshot: dict[str, Any], feed_payload: dict[str, Any]) -> tuple[dict[str, Any], int, int]:
    merged = {
        "source": base_snapshot.get("source", "offline_local_market_snapshot"),
        "timestamp_unix": base_snapshot.get("timestamp_unix", int(datetime.now(tz=timezone.utc).timestamp())),
        "regions": dict(base_snapshot.get("regions", {})),
    }

    feed_regions = feed_payload.get("regions", {})
    updated_items = 0
    updated_regions: set[str] = set()

    for region_key, region_data in feed_regions.items():
        if region_key not in merged["regions"]:
            merged["regions"][region_key] = {}
        target_region = merged["regions"][region_key]
        target_region["currency"] = region_data.get("currency", target_region.get("currency", "USD"))
        if "items" not in target_region:
            target_region["items"] = {}

        for item_name, item_value in region_data.get("items", {}).items():
            target_region["items"][item_name.lower()] = item_value
            updated_items += 1
            updated_regions.add(region_key)

    return merged, updated_items, len(updated_regions)


def refresh_market_snapshot(
    snapshot_path: Path,
    feed_paths: list[Path],
    output_path: Path | None = None,
    source: str = "offline_local_market_snapshot",
    now: datetime | None = None,
) -> SnapshotUpdateResult:
    now = now or datetime.now(tz=timezone.utc)
    target_path = output_path or snapshot_path
    base_snapshot = load_json(snapshot_path)

    total_items = 0
    region_names: set[str] = set()
    merged = base_snapshot
    for feed_path in feed_paths:
        feed_payload = load_json(feed_path)
        merged, updated_items, updated_regions = merge_market_feed(merged, feed_payload)
        total_items += updated_items
        if updated_regions > 0:
            for region_key in feed_payload.get("regions", {}).keys():
                region_names.add(region_key)

    merged["source"] = source
    merged["timestamp_unix"] = int(now.timestamp())
    write_json(target_path, merged)

    return SnapshotUpdateResult(
        updated_items=total_items,
        updated_regions=len(region_names),
        output_path=target_path,
        timestamp_unix=merged["timestamp_unix"],
        source=source,
    )
