from __future__ import annotations

import time
import unittest
from pathlib import Path

from offline_ai.chemistry import estimate_gas_pressure_atm, predict_reaction
from offline_ai.models import ExperimentRequest
from offline_ai.orchestrator import OfflineAssistantOrchestrator
from offline_ai.physics import electric_current_amps, lorentz_factor
from offline_ai.protocol import CommandEnvelope, sign_envelope, verify_envelope
from offline_ai.safety import evaluate_chemistry_safety


class OfflineAiTests(unittest.TestCase):
    def setUp(self) -> None:
        root = Path(__file__).resolve().parents[1]
        self.catalog_path = root / "data" / "materials.json"

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


if __name__ == "__main__":
    unittest.main()
