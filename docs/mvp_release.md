# Offline MVP Release Guide

This repository now includes a complete offline MVP stack:

1. Core simulation/planning/valuation engine (`assistant_core/python/offline_ai`)
2. Local API bridge (`assistant_core/python/tools/run_local_api_server.py`)
3. Android shell wired to local API (`apps/android/...`)
4. Windows shell wired to local API (`apps/windows/...`)
5. Daily 12:00 AM refresh flow with scheduler hooks

## Quick Run (Core + API)

```bash
python3 -m unittest discover -s assistant_core/python/tests -p "test_*.py"
./scripts/start_offline_api.sh
```

## Endpoint Smoke Test

```bash
curl -s http://127.0.0.1:8765/health
curl -s -X POST http://127.0.0.1:8765/api/scan-value \
  -H "Content-Type: application/json" \
  -d '{"imageLabels":["golfball"],"typedHint":"used golf balls","includeSecondHand":true,"region":"us"}'
```

## App Build Targets

- Android: `cd apps/android && ./gradlew :app:assembleDebug`
- Windows JVM: `cd apps/windows && ./gradlew run`

## Known MVP Boundaries

- Vision providers currently use deterministic placeholder labels.
- UI shell is professional and functional but not yet full 3D interactive lab runtime.
- Local API must be running for live data transport; app clients include fallback logic.
