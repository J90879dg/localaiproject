from __future__ import annotations

from datetime import datetime, timezone
from pathlib import Path

from offline_ai.daily_update_service import run_daily_update_if_due
from offline_ai.update_scheduler import DailyMidnightUpdatePolicy


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    data_root = root / "data"
    state_path = data_root / "daily_update_state.json"
    snapshot_path = data_root / "market_values.json"
    feed_paths = [data_root / "feeds" / "daily_market_feed.json"]

    local_tz = datetime.now(tz=timezone.utc).astimezone().tzinfo or timezone.utc
    policy = DailyMidnightUpdatePolicy(hour=0, minute=0, tz=local_tz)
    result = run_daily_update_if_due(
        policy=policy,
        state_path=state_path,
        snapshot_path=snapshot_path,
        feed_paths=feed_paths,
        now=datetime.now(tz=timezone.utc),
    )
    if result is None:
        print("Daily market refresh not due yet.")
        return 0

    print(
        "Daily market refresh complete:",
        {
            "updated_items": result.updated_items,
            "updated_regions": result.updated_regions,
            "timestamp_unix": result.timestamp_unix,
        },
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
