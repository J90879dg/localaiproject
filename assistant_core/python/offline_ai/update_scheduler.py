from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, time, timedelta, timezone


@dataclass(slots=True)
class NightlyUpdatePolicy:
    hour_utc: int = 0
    minute_utc: int = 0

    def next_run_after(self, current_utc: datetime) -> datetime:
        if current_utc.tzinfo is None:
            current_utc = current_utc.replace(tzinfo=timezone.utc)

        today_run = datetime.combine(
            current_utc.date(),
            time(self.hour_utc, self.minute_utc, tzinfo=timezone.utc),
        )
        if current_utc < today_run:
            return today_run
        return today_run + timedelta(days=1)
