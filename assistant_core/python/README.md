# Offline AI Core (Python)

This package provides a deterministic offline baseline for:
- Chemistry and physics simulation helpers
- Safety evaluation for chemistry workflows
- Project planning with local materials/tool catalog
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
- `offline_ai.protocol`
- `offline_ai.safety`
