# Windows App Module Blueprint

## Purpose
Secondary endpoint and optional compute/automation node for:
- Running heavier simulation workloads
- Hosting expanded dashboards for labs and project plans
- Executing approved local automation tasks
- Bridging commands with Android over secure local transport

## Recommended Structure

```text
desktop/
  ui/                 # Desktop panels and visualization views
  bridge/             # Pairing, authentication, command relay
  automation/         # Capability-scoped task execution
  orchestration/      # Invokes offline_ai core services
  storage/            # Encrypted logs + local profile cache
```

Current shell files are split by feature (separate places):

```text
desktop/src/main/kotlin/com/localaiproject/windows/
  DesktopMain.kt
  core/
    client/LocalBridgeClient.kt
    client/LocalOfflineApiClient.kt
    model/DesktopModels.kt
  ui/
    DesktopVisualStyle.kt
    ProfessionalDesktopDashboard.kt
  feature/
    control/ControlCoordinator.kt
    scan_value/DesktopScanValueCoordinator.kt
    update/WindowsDailyUpdateCoordinator.kt
    update/WindowsDailyUpdateRunner.kt
    vision/LocalVisionLabelProvider.kt
```

## Security Expectations
- Local-only communication (USB or LAN)
- Signed request envelopes
- Strict permission scopes (no implicit remote control)
- Tamper-evident activity logs

MVP transport:
- `LocalOfflineApiClient` uses local API at `http://127.0.0.1:8765`

## Daily Update Requirement

- `WindowsDailyUpdateCoordinator` builds a task command for **daily 12:00 AM local execution**.
- `WindowsDailyUpdateRunner` can execute the Python refresh script immediately.
- Point task execution to `assistant_core/python/tools/run_daily_market_refresh.py`.

## Visual Quality Direction

- Professional dashboard state model for polished desktop rendering
- Structured panel layout for update status, valuation status, and lab readiness

## Build (Windows Desktop JVM)

```bash
cd apps/windows
./gradlew run
```

If Gradle wrapper files are not present yet, run with a local Gradle install or
generate wrapper files once from an IDE/CI bootstrap environment.

Before running live scans/plans/simulations, start local API:

```bash
./scripts/start_offline_api.sh
```
