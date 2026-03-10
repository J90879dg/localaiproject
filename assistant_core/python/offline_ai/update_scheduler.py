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


@dataclass(slots=True)
class DailyMidnightUpdatePolicy:
    """Schedule updates daily at 12:00 AM for a chosen timezone."""

    hour: int = 0
    minute: int = 0
    tz: timezone = timezone.utc

    def next_run_after(self, current_time: datetime) -> datetime:
        if current_time.tzinfo is None:
            current_time = current_time.replace(tzinfo=self.tz)
        local_now = current_time.astimezone(self.tz)

        today_run = datetime.combine(
            local_now.date(),
            time(self.hour, self.minute, tzinfo=self.tz),
        )
        if local_now < today_run:
            return today_run
        return today_run + timedelta(days=1)

    def is_due(self, last_run_at: datetime | None, current_time: datetime) -> bool:
        if current_time.tzinfo is None:
            current_time = current_time.replace(tzinfo=self.tz)
        local_now = current_time.astimezone(self.tz)
        today_run = datetime.combine(
            local_now.date(),
            time(self.hour, self.minute, tzinfo=self.tz),
        )
        if local_now < today_run:
            return False
        if last_run_at is None:
            return True
        if last_run_at.tzinfo is None:
            last_run_at = last_run_at.replace(tzinfo=self.tz)
        return last_run_at.astimezone(self.tz) < today_run
