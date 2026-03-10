# Ultimate Virtual Lab + Personal AI Assistant (Offline-First)

This repository contains a **modular foundation** for building a cross-platform Android + Windows application with:

- Virtual chemistry and physics labs
- Local AI assistant (voice/text/image-driven workflows)
- Secure cross-device communication (USB/LAN)
- Offline project planning and guidance
- Privacy-first local storage and logs

The current codebase is intentionally focused on **architecture + core offline engines** so you can iterate safely toward full 3D/AR/avatar experiences.

## Core Principles

1. **Offline-first:** all critical workflows run without cloud access.
2. **Safety-first:** simulations and procedural guidance include explicit hazard checks.
3. **Modular scaling:** each subsystem can be upgraded independently (LLM, vision, simulation fidelity, avatar renderer).
4. **Cross-device security:** authenticated, encrypted local channels only.

## Repository Layout

```text
apps/
  android/                    # Android app integration plan (Kotlin + local runtime)
  windows/                    # Windows desktop integration plan
assistant_core/
  python/
    offline_ai/              # Offline calculators, project planner, protocol + orchestration
    data/                    # Local materials/tools database
    tests/                   # Unit tests for offline core
docs/
  architecture.md            # End-to-end system architecture
  implementation_plan.md     # Practical phased roadmap
```

## What Is Implemented Now

- Shared request/response models for experiment and project workflows
- Chemistry simulation helpers (reaction heuristics + ideal gas outcome estimates)
- Physics simulation helpers (mechanics, electricity, magnetism, aerodynamics, relativity, quantum)
- Local project planner using offline materials/tool database
- Scan alias matcher from image labels/typed hints
- Offline market-worth estimator (includes sports items like golf balls) from local snapshot
- Daily market snapshot refresh pipeline at 12:00 AM (local-device scheduler hooks)
- Safety protocol evaluator for chemistry workflows
- Signed message envelope for secure Android↔Windows command exchange
- Unit tests for deterministic offline behavior
- Android and Windows shell code split into separate feature places
- Professional dashboard UI scaffolds for Android and Windows experiences

## Quick Start (Offline Core)

```bash
cd assistant_core/python
python3 -m unittest discover -s tests -p "test_*.py"
```

Daily refresh command:

```bash
cd assistant_core/python
python3 tools/run_daily_market_refresh.py
```

## Next Build Targets

1. Build Android UI shell (voice, text, camera/image import, avatar canvas).
2. Build Windows control shell (automation bridge + shared protocol client).
3. Integrate local STT/TTS + wake word stack.
4. Add local LLM runtime adapter with tool-calling into `offline_ai`.
5. Add 3D chemistry/physics rendering layer (Unity/Unreal/Godot integration path).

## Constraints and Safety Notes

- This project is educational and planning-oriented; real-world chemistry execution must follow local regulations and professional supervision.
- “D-level” realism should be interpreted as **high-fidelity progressive simulation tiers** with transparent model limitations.