from __future__ import annotations

import hashlib
import hmac
import json
import time
from dataclasses import dataclass
from typing import Any


@dataclass(slots=True)
class CommandEnvelope:
    sender_device_id: str
    target_device_id: str
    command_type: str
    payload: dict[str, Any]
    timestamp_unix: int
    nonce: str
    signature_hex: str = ""

    def to_signable_payload(self) -> bytes:
        message = {
            "sender_device_id": self.sender_device_id,
            "target_device_id": self.target_device_id,
            "command_type": self.command_type,
            "payload": self.payload,
            "timestamp_unix": self.timestamp_unix,
            "nonce": self.nonce,
        }
        return json.dumps(message, sort_keys=True, separators=(",", ":")).encode("utf-8")


def sign_envelope(envelope: CommandEnvelope, shared_secret: str) -> CommandEnvelope:
    digest = hmac.new(
        key=shared_secret.encode("utf-8"),
        msg=envelope.to_signable_payload(),
        digestmod=hashlib.sha256,
    ).hexdigest()
    envelope.signature_hex = digest
    return envelope


def verify_envelope(
    envelope: CommandEnvelope,
    shared_secret: str,
    max_clock_skew_seconds: int = 120,
) -> bool:
    now = int(time.time())
    if abs(now - envelope.timestamp_unix) > max_clock_skew_seconds:
        return False
    expected = hmac.new(
        key=shared_secret.encode("utf-8"),
        msg=envelope.to_signable_payload(),
        digestmod=hashlib.sha256,
    ).hexdigest()
    return hmac.compare_digest(expected, envelope.signature_hex)
