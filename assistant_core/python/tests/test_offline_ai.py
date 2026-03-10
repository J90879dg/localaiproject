from __future__ import annotations

import json
import time
import unittest
from datetime import datetime, timezone
from pathlib import Path
from tempfile import TemporaryDirectory

from offline_ai.chemistry import estimate_gas_pressure_atm, predict_reaction
from offline_ai.daily_update_service import run_daily_update_if_due
from offline_ai.market_updater import refresh_market_snapshot
from offline_ai.models import ExperimentRequest, ScanRequest
from offline_ai.orchestrator import OfflineAssistantOrchestrator
from offline_ai.physics import electric_current_amps, lorentz_factor
from offline_ai.protocol import CommandEnvelope, sign_envelope, verify_envelope
from offline_ai.scanner import ScanAliasMatcher, load_scan_aliases
from offline_ai.safety import evaluate_chemistry_safety
from offline_ai.update_scheduler import DailyMidnightUpdatePolicy, NightlyUpdatePolicy
from offline_ai.valuation import estimate_item_worth, load_market_snapshot


class OfflineAiTests(unittest.TestCase):
    def setUp(self) -> None:
        root = Path(__file__).resolve().parents[1]
        self.catalog_path = root / "data" / "materials.json"
        self.market_snapshot_path = root / "data" / "market_values.json"
        self.scan_aliases_path = root / "data" / "scan_aliases.json"
        self.daily_feed_path = root / "data" / "feeds" / "daily_market_feed.json"

    def test_ideal_gas_pressure(self) -> None:
        pressure = estimate_gas_pressure_atm(moles=1.0, temperature_k=273.15, volume_l=22.414)
        self.assertAlmostEqual(pressure, 1.0, places=1)

    def test_known_reaction_prediction(self) -> None:
        result = predict_reaction(["HCl", "NaOH"])
        self.assertEqual(result["category"], "neutralization")
        self.assertIn("NaCl", result["products"])

    def test_safety_flag_for_flammable_inputs(self) -> None:
        safety = evaluate_chemistry_safety(["H2", "O2"], {"temperature_c": 120})
        self.assertIn(safety["hazard_level"], {"medium", "high"})
        self.assertTrue(any("Flammable" in warning for warning in safety["warnings"]))

    def test_physics_ohms_law(self) -> None:
        current, power = electric_current_amps(voltage_v=9.0, resistance_ohm=3.0)
        self.assertAlmostEqual(current, 3.0)
        self.assertAlmostEqual(power, 27.0)

    def test_lorentz_factor(self) -> None:
        gamma = lorentz_factor(100_000_000.0)
        self.assertGreater(gamma, 1.0)

    def test_command_signature_verification(self) -> None:
        envelope = CommandEnvelope(
            sender_device_id="android-1",
            target_device_id="windows-1",
            command_type="automation.intent",
            payload={"action": "open_lab_view"},
            timestamp_unix=int(time.time()),
            nonce="abc123",
        )
        sign_envelope(envelope, shared_secret="secret")
        self.assertTrue(verify_envelope(envelope, shared_secret="secret"))
        self.assertFalse(verify_envelope(envelope, shared_secret="wrong-secret"))

    def test_orchestrator_chemistry(self) -> None:
        orchestrator = OfflineAssistantOrchestrator(catalog_path=self.catalog_path)
        result = orchestrator.run_experiment(
            ExperimentRequest(
                user_goal="Neutralize an acid safely",
                domain="chemistry",
                reactants=["HCl", "NaOH"],
                conditions={"temperature_c": 25.0, "moles": 1.0, "temperature_k": 298.15, "volume_l": 24.0},
            )
        )
        self.assertEqual(result.domain, "chemistry")
        self.assertIn("reaction", result.outputs)
        self.assertIn("safety", result.outputs)

    def test_scan_alias_matcher(self) -> None:
        aliases = load_scan_aliases(self.scan_aliases_path)
        matcher = ScanAliasMatcher(aliases=aliases)
        identified = matcher.identify(
            ScanRequest(
                image_labels=["white dimpled golfball", "titleist logo"],
                typed_hint="used golf balls",
            )
        )
        self.assertIsNotNone(identified)
        assert identified is not None
        self.assertEqual(identified.canonical_name, "golf ball")
        self.assertGreater(identified.confidence, 0.5)

    def test_offline_valuation_for_golf_ball(self) -> None:
        snapshot = load_market_snapshot(self.market_snapshot_path)
        result = estimate_item_worth(
            item_name="golf ball",
            snapshot=snapshot,
            region="us",
            include_second_hand=True,
        )
        self.assertEqual(result.currency, "USD")
        self.assertGreater(result.mid_estimate, 0.0)
        self.assertLess(result.low_estimate, result.high_estimate)

    def test_orchestrator_scan_and_value(self) -> None:
        orchestrator = OfflineAssistantOrchestrator(
            catalog_path=self.catalog_path,
            market_snapshot_path=self.market_snapshot_path,
            scan_aliases_path=self.scan_aliases_path,
        )
        result = orchestrator.scan_and_value(
            ScanRequest(
                image_labels=["golfball", "titleist"],
                typed_hint="value this used golf ball",
                include_second_hand=True,
                region="us",
            )
        )
        self.assertIsNotNone(result["identified_item"])
        self.assertIsNotNone(result["valuation"])

    def test_nightly_scheduler(self) -> None:
        policy = NightlyUpdatePolicy(hour_utc=0, minute_utc=0)
        current = datetime(2026, 3, 10, 23, 0, tzinfo=timezone.utc)
        next_run = policy.next_run_after(current)
        self.assertEqual(next_run.hour, 0)
        self.assertEqual(next_run.minute, 0)

    def test_daily_policy_due_at_midnight(self) -> None:
        policy = DailyMidnightUpdatePolicy(hour=0, minute=0, tz=timezone.utc)
        current = datetime(2026, 3, 10, 0, 30, tzinfo=timezone.utc)
        last_run = datetime(2026, 3, 9, 23, 0, tzinfo=timezone.utc)
        self.assertTrue(policy.is_due(last_run_at=last_run, current_time=current))

    def test_market_refresh_updates_golf_ball_price(self) -> None:
        with TemporaryDirectory() as temp_dir:
            temp_root = Path(temp_dir)
            snapshot_copy = temp_root / "market_values.json"
            feed_copy = temp_root / "daily_market_feed.json"

            snapshot_copy.write_text(self.market_snapshot_path.read_text(encoding="utf-8"), encoding="utf-8")
            feed_copy.write_text(self.daily_feed_path.read_text(encoding="utf-8"), encoding="utf-8")

            refresh_market_snapshot(
                snapshot_path=snapshot_copy,
                feed_paths=[feed_copy],
                output_path=snapshot_copy,
                now=datetime(2026, 3, 11, 0, 0, tzinfo=timezone.utc),
            )
            updated = json.loads(snapshot_copy.read_text(encoding="utf-8"))
            golf_ball = updated["regions"]["us"]["items"]["golf ball"]
            self.assertEqual(golf_ball["new_mid"], 2.35)

    def test_run_daily_update_if_due_runs_once(self) -> None:
        with TemporaryDirectory() as temp_dir:
            temp_root = Path(temp_dir)
            snapshot_copy = temp_root / "market_values.json"
            feed_copy = temp_root / "daily_market_feed.json"
            state_path = temp_root / "daily_update_state.json"

            snapshot_copy.write_text(self.market_snapshot_path.read_text(encoding="utf-8"), encoding="utf-8")
            feed_copy.write_text(self.daily_feed_path.read_text(encoding="utf-8"), encoding="utf-8")

            policy = DailyMidnightUpdatePolicy(hour=0, minute=0, tz=timezone.utc)
            now = datetime(2026, 3, 11, 0, 5, tzinfo=timezone.utc)

            first = run_daily_update_if_due(
                policy=policy,
                state_path=state_path,
                snapshot_path=snapshot_copy,
                feed_paths=[feed_copy],
                now=now,
            )
            second = run_daily_update_if_due(
                policy=policy,
                state_path=state_path,
                snapshot_path=snapshot_copy,
                feed_paths=[feed_copy],
                now=now,
            )
            self.assertIsNotNone(first)
            self.assertIsNone(second)


if __name__ == "__main__":
    unittest.main()
