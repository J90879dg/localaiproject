# Separation Map ("separate places")

To keep the system expandable and secure, capabilities are isolated into separate places:

## 1) Simulation Place
- Location: `assistant_core/python/offline_ai/chemistry.py`, `physics.py`, `safety.py`
- Responsibility: experiment prediction, formula outputs, hazard scoring
- Data: experiment request payloads + deterministic model parameters

## 2) Planning Place
- Location: `assistant_core/python/offline_ai/planner.py`
- Responsibility: project decomposition and material/tool suggestions
- Data: `assistant_core/python/data/materials.json`

## 3) Scan + Worth Place
- Location: `assistant_core/python/offline_ai/scanner.py`, `valuation.py`
- Responsibility: identify item from labels/hints and estimate current worth offline
- Data:
  - `assistant_core/python/data/scan_aliases.json`
  - `assistant_core/python/data/market_values.json`
  - `assistant_core/python/data/feeds/daily_market_feed.json`

## 4) Daily Update Place
- Location:
  - `assistant_core/python/offline_ai/update_scheduler.py`
  - `assistant_core/python/offline_ai/market_updater.py`
  - `assistant_core/python/offline_ai/daily_update_service.py`
  - `assistant_core/python/tools/run_daily_market_refresh.py`
- Responsibility: refresh local worth data at **12:00 AM every day**
- State: `assistant_core/python/data/daily_update_state.json`

## 5) Secure Device Bridge Place
- Location: `assistant_core/python/offline_ai/protocol.py`
- Responsibility: signed command envelopes and verification for Android↔Windows

## 6) App Client Places
- Android shell: `apps/android/app/src/main/java/com/localaiproject/android/...`
- Windows shell: `apps/windows/desktop/src/main/kotlin/com/localaiproject/windows/...`

## 7) Visual Experience Place
- Android visual layer:
  - `apps/android/app/src/main/java/com/localaiproject/android/ui/...`
- Windows visual layer:
  - `apps/windows/desktop/src/main/kotlin/com/localaiproject/windows/ui/...`
- Responsibility: professional/cool dashboard look and consistent brand feel

This split avoids one giant module and allows each area to evolve independently.
