from __future__ import annotations

import time
import unittest
from datetime import datetime, timezone
from pathlib import Path

from offline_ai.chemistry import estimate_gas_pressure_atm, predict_reaction
from offline_ai.models import ExperimentRequest, ScanRequest
from offline_ai.orchestrator import OfflineAssistantOrchestrator
from offline_ai.physics import electric_current_amps, lorentz_factor
from offline_ai.protocol import CommandEnvelope, sign_envelope, verify_envelope
from offline_ai.scanner import ScanAliasMatcher, load_scan_aliases
from offline_ai.safety import evaluate_chemistry_safety
from offline_ai.update_scheduler import NightlyUpdatePolicy
from offline_ai.valuation import estimate_item_worth, load_market_snapshot


class OfflineAiTests(unittest.TestCase):
    def setUp(self) -> None:
        root = Path(__file__).resolve().parents[1]
        self.catalog_path = root / "data" / "materials.json"
        self.market_snapshot_path = root / "data" / "market_values.json"
        self.scan_aliases_path = root / "data" / "scan_aliases.json"

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


if __name__ == "__main__":
    unittest.main()
