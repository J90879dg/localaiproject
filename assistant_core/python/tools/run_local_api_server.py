from __future__ import annotations

import argparse
from pathlib import Path

from offline_ai.local_api import create_server


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Run local offline API server.")
    parser.add_argument("--host", default="127.0.0.1", help="Host interface to bind.")
    parser.add_argument("--port", type=int, default=8765, help="Port to listen on.")
    parser.add_argument(
        "--data-root",
        default=str(Path(__file__).resolve().parents[1] / "data"),
        help="Path to offline data directory.",
    )
    return parser


def main() -> int:
    args = build_parser().parse_args()
    data_root = Path(args.data_root).resolve()
    server = create_server(host=args.host, port=args.port, data_root=data_root)
    print(f"Offline API server listening on http://{args.host}:{args.port}")
    print(f"Using data root: {data_root}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("Shutting down local API server.")
    finally:
        server.server_close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
