# Offline AI Core (Python)

This package provides a deterministic offline baseline for:
- Chemistry and physics simulation helpers
- Safety evaluation for chemistry workflows
- Project planning with local materials/tool catalog
- Scan alias matching from image labels/text hints
- Offline item worth estimation from local market snapshots
- Signed command envelopes for cross-device communication
- Daily market snapshot refresh service (12:00 AM schedule)
- Orchestration entrypoint combining all core capabilities

## Run Tests

```bash
cd assistant_core/python
python3 -m unittest discover -s tests -p "test_*.py"
```

## Main Entry Points

- `offline_ai.orchestrator.OfflineAssistantOrchestrator`
- `offline_ai.chemistry`
- `offline_ai.physics`
- `offline_ai.planner`
- `offline_ai.scanner`
- `offline_ai.valuation`
- `offline_ai.protocol`
- `offline_ai.safety`
- `offline_ai.market_updater`
- `offline_ai.daily_update_service`
- `offline_ai.update_scheduler`
- `offline_ai.local_api`

## Scan + Worth Example

```python
from pathlib import Path
from offline_ai.models import ScanRequest
from offline_ai.orchestrator import OfflineAssistantOrchestrator

root = Path("assistant_core/python/data")
orchestrator = OfflineAssistantOrchestrator(
    catalog_path=root / "materials.json",
    market_snapshot_path=root / "market_values.json",
    scan_aliases_path=root / "scan_aliases.json",
)
result = orchestrator.scan_and_value(
    ScanRequest(image_labels=["titleist golfball"], typed_hint="used golf balls")
)
print(result)
```

## Daily 12:00 AM Refresh

Run this command from your local scheduler (Android worker, Windows task scheduler, cron, etc.):

```bash
cd assistant_core/python
python3 tools/run_daily_market_refresh.py
```

The script checks whether the local timezone has crossed midnight and refreshes
`data/market_values.json` from local feed files when due.

## Local API Server

```bash
cd assistant_core/python
python3 tools/run_local_api_server.py --host 127.0.0.1 --port 8765
```

This exposes offline endpoints for:
- scan + worth
- project planning
- experiment simulation
- daily refresh trigger
