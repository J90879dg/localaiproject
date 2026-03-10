# Offline Local API (MVP)

This API lets Android and Windows clients call the offline core with one local protocol.

## Start Server

```bash
cd assistant_core/python
python3 tools/run_local_api_server.py --host 127.0.0.1 --port 8765
```

Or from project root:

```bash
./scripts/start_offline_api.sh
```

## Endpoints

### `GET /health`
Returns service status and available features.

### `POST /api/scan-value`
Request:

```json
{
  "imageLabels": ["golfball", "titleist"],
  "typedHint": "used golf balls",
  "includeSecondHand": true,
  "region": "us"
}
```

### `POST /api/project-plan`
Request:

```json
{
  "goal": "Build a compact chemistry demo bench"
}
```

### `POST /api/experiment`
Request:

```json
{
  "userGoal": "Run safe neutralization simulation",
  "domain": "chemistry",
  "reactants": ["HCl", "NaOH"],
  "conditions": {"temperature_c": 25.0}
}
```

### `POST /api/daily-refresh`
Request:

```json
{
  "force": false
}
```
