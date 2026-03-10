from __future__ import annotations

import json
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path

from .market_updater import SnapshotUpdateResult, refresh_market_snapshot
from .update_scheduler import DailyMidnightUpdatePolicy


@dataclass(slots=True)
class DailyUpdateState:
    last_success_unix: int | None = None


def load_state(path: Path) -> DailyUpdateState:
    if not path.exists():
        return DailyUpdateState(last_success_unix=None)
    with path.open("r", encoding="utf-8") as file:
        payload = json.load(file)
    return DailyUpdateState(last_success_unix=payload.get("last_success_unix"))


def save_state(path: Path, state: DailyUpdateState) -> None:
    payload = {"last_success_unix": state.last_success_unix}
    with path.open("w", encoding="utf-8") as file:
        json.dump(payload, file, indent=2, sort_keys=True)
        file.write("\n")


def run_daily_update_if_due(
    policy: DailyMidnightUpdatePolicy,
    state_path: Path,
    snapshot_path: Path,
    feed_paths: list[Path],
    now: datetime | None = None,
) -> SnapshotUpdateResult | None:
    now = now or datetime.now(tz=timezone.utc)
    state = load_state(state_path)
    last_run_at = None
    if state.last_success_unix is not None:
        last_run_at = datetime.fromtimestamp(state.last_success_unix, tz=timezone.utc)

    if not policy.is_due(last_run_at=last_run_at, current_time=now):
        return None

    result = refresh_market_snapshot(
        snapshot_path=snapshot_path,
        feed_paths=feed_paths,
        output_path=snapshot_path,
        source="offline_local_market_snapshot_daily_refresh",
        now=now,
    )
    save_state(state_path, DailyUpdateState(last_success_unix=result.timestamp_unix))
    return result
