# Offline AI Core (Python)

This package provides a deterministic offline baseline for:
- Chemistry and physics simulation helpers
- Safety evaluation for chemistry workflows
- Project planning with local materials/tool catalog
- Scan alias matching from image labels/text hints
- Offline item worth estimation from local market snapshots
- Signed command envelopes for cross-device communication
- Orchestration entrypoint combining all core capabilities

## Run Tests

```bash
cd assistant_core/python
python -m unittest discover -s tests -p "test_*.py"
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
