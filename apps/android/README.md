# Android App Module Blueprint

## Purpose
Primary user-facing client for:
- Voice/text/image command intake
- Avatar response display
- Virtual lab session controls
- Optional bridge to Windows compute/controller node

## Recommended Structure

```text
app/
  ui/                 # Compose screens, avatar widgets, session views
  voice/              # Wake word, STT, TTS adapters
  vision/             # Camera/image preprocessing hooks
  bridge/             # Secure local link client
  orchestration/      # Request pipeline into offline_ai engine
  storage/            # Encrypted local persistence + logs
```

Current shell files are split by feature (separate places):

```text
app/src/main/java/com/localaiproject/android/
  MainActivity.kt
  core/
    client/OfflineCoreClient.kt
    model/UiModels.kt
  feature/
    chat/ChatCoordinator.kt
    lab/LabSessionCoordinator.kt
    scan_value/ScanValueCoordinator.kt
    update/DailyUpdateScheduler.kt
    update/DailyUpdateWorker.kt
    vision/ImageLabelProvider.kt
    vision/TfliteImageLabelProvider.kt
```

## Integration Contract

Android sends normalized payloads to shared offline core:
- user intent
- modality metadata (voice/text/image)
- experiment/project context
- policy and safety preference flags

Expected outputs:
- step-by-step guidance
- predictive outcomes
- safety warnings
- suggested materials/tools
- scan-based item identification with local worth estimates

## Daily Update Requirement

- `DailyUpdateScheduler` configures WorkManager for **every day at 12:00 AM local time**.
- `DailyUpdateWorker` is the hook point to run local market snapshot refresh.
