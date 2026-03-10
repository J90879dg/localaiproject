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
    model/DesktopModels.kt
  feature/
    control/ControlCoordinator.kt
    scan_value/DesktopScanValueCoordinator.kt
    update/WindowsDailyUpdateCoordinator.kt
    vision/LocalVisionLabelProvider.kt
```

## Security Expectations
- Local-only communication (USB or LAN)
- Signed request envelopes
- Strict permission scopes (no implicit remote control)
- Tamper-evident activity logs

## Daily Update Requirement

- `WindowsDailyUpdateCoordinator` builds a task command for **daily 12:00 AM local execution**.
- Point task execution to `assistant_core/python/tools/run_daily_market_refresh.py`.
