from __future__ import annotations

import json
import threading
import unittest
from pathlib import Path
from tempfile import TemporaryDirectory
from urllib.request import Request, urlopen

from offline_ai.local_api import create_server


class LocalApiTests(unittest.TestCase):
    def setUp(self) -> None:
        root = Path(__file__).resolve().parents[1]
        self.temp_dir = TemporaryDirectory()
        self.data_root = Path(self.temp_dir.name) / "data"
        (self.data_root / "feeds").mkdir(parents=True, exist_ok=True)

        for name in ("materials.json", "market_values.json", "scan_aliases.json"):
            source = root / "data" / name
            target = self.data_root / name
            target.write_text(source.read_text(encoding="utf-8"), encoding="utf-8")

        feed_source = root / "data" / "feeds" / "daily_market_feed.json"
        feed_target = self.data_root / "feeds" / "daily_market_feed.json"
        feed_target.write_text(feed_source.read_text(encoding="utf-8"), encoding="utf-8")

        self.server = create_server(host="127.0.0.1", port=0, data_root=self.data_root)
        self.port = int(self.server.server_address[1])
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()

    def tearDown(self) -> None:
        self.server.shutdown()
        self.server.server_close()
        self.thread.join(timeout=2)
        self.temp_dir.cleanup()

    def test_health_endpoint(self) -> None:
        payload = self._request("GET", "/health")
        self.assertEqual(payload["status"], "ok")
        self.assertIn("scan_value", payload["features"])

    def test_scan_value_endpoint(self) -> None:
        payload = self._request(
            "POST",
            "/api/scan-value",
            {
                "imageLabels": ["white dimpled golfball", "titleist"],
                "typedHint": "used golf balls",
                "includeSecondHand": True,
                "region": "us",
            },
        )
        self.assertEqual(payload["identifiedItem"]["canonicalName"], "golf ball")
        self.assertEqual(payload["valuation"]["currency"], "USD")

    def test_project_plan_endpoint(self) -> None:
        payload = self._request(
            "POST",
            "/api/project-plan",
            {"goal": "Build a battery powered circuit tester"},
        )
        self.assertTrue(len(payload["recommendedItems"]) > 0)
        self.assertTrue(len(payload["steps"]) > 0)

    def test_experiment_endpoint(self) -> None:
        payload = self._request(
            "POST",
            "/api/experiment",
            {
                "userGoal": "Estimate displacement and drag",
                "domain": "physics",
                "conditions": {
                    "initial_velocity_mps": 5.0,
                    "acceleration_mps2": 2.0,
                    "time_s": 4.0,
                    "voltage_v": 9.0,
                    "resistance_ohm": 3.0,
                },
            },
        )
        self.assertEqual(payload["domain"], "physics")
        self.assertIn("displacement_m", payload["outputs"])

    def test_daily_refresh_force_endpoint(self) -> None:
        payload = self._request("POST", "/api/daily-refresh", {"force": True})
        self.assertTrue(payload["refreshed"])
        self.assertIsNotNone(payload["timestampUnix"])

    def _request(self, method: str, path: str, body: dict | None = None) -> dict:
        url = f"http://127.0.0.1:{self.port}{path}"
        data = None
        headers = {}
        if body is not None:
            data = json.dumps(body).encode("utf-8")
            headers["Content-Type"] = "application/json"
        request = Request(url, data=data, method=method, headers=headers)
        with urlopen(request, timeout=3) as response:
            return json.loads(response.read().decode("utf-8"))


if __name__ == "__main__":
    unittest.main()
