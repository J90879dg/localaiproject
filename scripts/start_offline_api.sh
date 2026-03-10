#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PYTHON_BIN="${PYTHON_BIN:-python3}"
HOST="${HOST:-127.0.0.1}"
PORT="${PORT:-8765}"

cd "${ROOT_DIR}/assistant_core/python"
exec "${PYTHON_BIN}" tools/run_local_api_server.py --host "${HOST}" --port "${PORT}"
