from __future__ import annotations

import json
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Any
from urllib.parse import urlparse

from .daily_update_service import DailyUpdateState, save_state
from .market_updater import refresh_market_snapshot
from .models import ExperimentRequest, ScanRequest
from .orchestrator import OfflineAssistantOrchestrator
from .valuation import load_market_snapshot


class OfflineApiApp:
    def __init__(self, data_root: Path):
        self.data_root = data_root
        self.catalog_path = data_root / "materials.json"
        self.snapshot_path = data_root / "market_values.json"
        self.aliases_path = data_root / "scan_aliases.json"
        self.state_path = data_root / "daily_update_state.json"
        self.feed_paths = [data_root / "feeds" / "daily_market_feed.json"]
        self.orchestrator = OfflineAssistantOrchestrator(
            catalog_path=self.catalog_path,
            market_snapshot_path=self.snapshot_path,
            scan_aliases_path=self.aliases_path,
        )

    def health(self) -> dict[str, Any]:
        return {
            "status": "ok",
            "mode": "offline",
            "timeUnix": int(datetime.now(tz=timezone.utc).timestamp()),
            "features": [
                "scan_value",
                "project_plan",
                "experiment_simulation",
                "daily_refresh",
            ],
        }

    def scan_value(self, payload: dict[str, Any]) -> dict[str, Any]:
        request = ScanRequest(
            image_labels=_to_str_list(payload.get("imageLabels", [])),
            typed_hint=str(payload.get("typedHint", "")),
            include_second_hand=bool(payload.get("includeSecondHand", True)),
            region=str(payload.get("region", "us")),
        )
        result = self.orchestrator.scan_and_value(request)
        identified = result.get("identified_item")
        valuation = result.get("valuation")

        return {
            "identifiedItem": _to_identified_camel(identified),
            "valuation": _to_valuation_camel(valuation),
            "message": str(result.get("message", "")),
        }

    def project_plan(self, payload: dict[str, Any]) -> dict[str, Any]:
        goal = str(payload.get("goal", "")).strip()
        plan = self.orchestrator.plan_project(goal)
        return {
            "goal": plan["goal"],
            "recommendedItems": plan["recommended_items"],
            "steps": plan["steps"],
            "cautions": plan["cautions"],
        }

    def run_experiment(self, payload: dict[str, Any]) -> dict[str, Any]:
        request = ExperimentRequest(
            user_goal=str(payload.get("userGoal", "")),
            domain=str(payload.get("domain", "")),
            reactants=_to_str_list(payload.get("reactants", [])),
            chemical_name=_nullable_str(payload.get("chemicalName")),
            image_labels=_to_str_list(payload.get("imageLabels", [])),
            conditions=_to_float_dict(payload.get("conditions", {})),
        )
        result = self.orchestrator.run_experiment(request)
        return {
            "domain": result.domain,
            "summary": result.summary,
            "outputs": _stringify_outputs(result.outputs),
            "assumptions": result.assumptions,
            "warnings": result.warnings,
        }

    def refresh_daily(self, payload: dict[str, Any]) -> dict[str, Any]:
        force = bool(payload.get("force", False))
        now = datetime.now(tz=timezone.utc)
        if force:
            refreshed = refresh_market_snapshot(
                snapshot_path=self.snapshot_path,
                feed_paths=self.feed_paths,
                output_path=self.snapshot_path,
                source="offline_local_market_snapshot_forced_refresh",
                now=now,
            )
            save_state(
                self.state_path,
                DailyUpdateState(last_success_unix=refreshed.timestamp_unix),
            )
            self.orchestrator.market_snapshot = load_market_snapshot(self.snapshot_path)
            return {
                "refreshed": True,
                "message": "Forced refresh completed.",
                "timestampUnix": refreshed.timestamp_unix,
            }

        due_result = self.orchestrator.refresh_market_data_if_due(
            state_path=self.state_path,
            feed_paths=self.feed_paths,
            now=now,
        )
        if due_result is None:
            return {
                "refreshed": False,
                "message": "Refresh not due yet.",
                "timestampUnix": None,
            }
        return {
            "refreshed": True,
            "message": "Scheduled refresh completed.",
            "timestampUnix": due_result.timestamp_unix,
        }


def create_server(host: str, port: int, data_root: Path) -> ThreadingHTTPServer:
    app = OfflineApiApp(data_root=data_root)

    class RequestHandler(BaseHTTPRequestHandler):
        def _write_json(self, status_code: int, payload: dict[str, Any]) -> None:
            encoded = json.dumps(payload, ensure_ascii=True).encode("utf-8")
            self.send_response(status_code)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(encoded)))
            self.end_headers()
            self.wfile.write(encoded)

        def _read_json(self) -> dict[str, Any]:
            content_length = int(self.headers.get("Content-Length", "0"))
            if content_length <= 0:
                return {}
            body = self.rfile.read(content_length).decode("utf-8")
            return json.loads(body) if body else {}

        def do_GET(self) -> None:  # noqa: N802
            path = urlparse(self.path).path
            if path == "/health":
                self._write_json(200, app.health())
                return
            if path == "/api/capabilities":
                self._write_json(200, {"capabilities": app.health()["features"]})
                return
            self._write_json(404, {"error": "Not found"})

        def do_POST(self) -> None:  # noqa: N802
            path = urlparse(self.path).path
            try:
                payload = self._read_json()
            except json.JSONDecodeError:
                self._write_json(400, {"error": "Invalid JSON"})
                return

            try:
                if path == "/api/scan-value":
                    self._write_json(200, app.scan_value(payload))
                    return
                if path == "/api/project-plan":
                    self._write_json(200, app.project_plan(payload))
                    return
                if path == "/api/experiment":
                    self._write_json(200, app.run_experiment(payload))
                    return
                if path == "/api/daily-refresh":
                    self._write_json(200, app.refresh_daily(payload))
                    return
            except ValueError as error:
                self._write_json(400, {"error": str(error)})
                return
            except Exception as error:  # pragma: no cover
                self._write_json(500, {"error": f"Internal server error: {error}"})
                return

            self._write_json(404, {"error": "Not found"})

        def log_message(self, format: str, *args: Any) -> None:  # noqa: A003
            return

    return ThreadingHTTPServer((host, port), RequestHandler)


def _to_str_list(values: Any) -> list[str]:
    if not isinstance(values, list):
        return []
    return [str(value) for value in values]


def _nullable_str(value: Any) -> str | None:
    if value is None:
        return None
    text = str(value).strip()
    return text or None


def _to_float_dict(value: Any) -> dict[str, float]:
    if not isinstance(value, dict):
        return {}
    result: dict[str, float] = {}
    for key, raw in value.items():
        try:
            result[str(key)] = float(raw)
        except (TypeError, ValueError):
            continue
    return result


def _to_identified_camel(payload: dict[str, Any] | None) -> dict[str, Any] | None:
    if payload is None:
        return None
    return {
        "canonicalName": payload.get("canonical_name"),
        "confidence": payload.get("confidence"),
        "matchedAliases": payload.get("matched_aliases", []),
    }


def _to_valuation_camel(payload: dict[str, Any] | None) -> dict[str, Any] | None:
    if payload is None:
        return None
    return {
        "itemName": payload.get("item_name"),
        "region": payload.get("region"),
        "currency": payload.get("currency"),
        "lowEstimate": payload.get("low_estimate"),
        "midEstimate": payload.get("mid_estimate"),
        "highEstimate": payload.get("high_estimate"),
        "dataTimestampUnix": payload.get("data_timestamp_unix"),
        "source": payload.get("source"),
    }


def _stringify_outputs(outputs: dict[str, Any]) -> dict[str, str]:
    result: dict[str, str] = {}
    for key, value in outputs.items():
        if isinstance(value, (dict, list)):
            result[key] = json.dumps(value, ensure_ascii=True, sort_keys=True)
        else:
            result[key] = str(value)
    return result
