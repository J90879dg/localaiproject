# System Architecture (Offline Android + Windows)

## 1) High-Level Components

```text
┌───────────────────────────────────────────────────────────────────────┐
│                         User Interaction Layer                        │
│  Voice I/O  |  Text UI  |  Image Input  |  Avatar UI  |  3D Lab UI   │
└───────────────┬───────────────────────────────────────────────┬───────┘
                │                                               │
                v                                               v
┌──────────────────────────┐                        ┌────────────────────┐
│  Assistant Orchestrator  │<────tool calls───────>│ Simulation Engines │
│  (local policy + planner)│                        │ Chemistry + Physics│
└───────────────┬──────────┘                        └─────────┬──────────┘
                │                                             │
                v                                             v
┌───────────────────────────────────────────────────────────────────────┐
│                          Local Intelligence Layer                     │
│  On-device LLM runtime | Retrieval index | Rules engine | Safety AI   │
└───────────────┬───────────────────────────────────────────────┬───────┘
                │                                               │
                v                                               v
┌──────────────────────────┐                        ┌────────────────────┐
│ Local Persistence        │                        │ Device Link Bridge │
│ SQLite + vector index    │                        │ USB / local Wi-Fi  │
│ encrypted logs + profile │                        │ signed envelopes   │
└──────────────────────────┘                        └────────────────────┘
```

## 2) Runtime Targets

- **Android app**
  - Primary interaction endpoint (voice/text/image, avatar view, lab UI)
  - Can run self-contained offline mode
  - Optionally links to Windows node for heavier simulation workloads

- **Windows app**
  - Extended compute node for larger simulations and automation tools
  - Can mirror assistant state and execute approved cross-device actions

## 3) Core Subsystems

### A. Input Stack
- Voice wake word + offline STT
- Text command channel
- Image ingestion with on-device CV model hooks
- Session context packager (goal + constraints + history + modality)

### B. Assistant Core
- Intent classification: `project_plan`, `chem_lab`, `physics_lab`, `automation`, `general`
- Safety gate before experiment guidance
- Tool invocation layer for simulation and project planning
- Response composer for voice/text/avatar output

### C. Simulation Layer
- **Chemistry engine**
  - Stoichiometry, gas-law estimation, reaction balancing, concentration tracking
  - Safety and incompatibility checks
- **Physics engine**
  - Mechanics, electric circuits, magnetic field estimates, drag/lift approximations
  - Relativity and quantum “learning mode” formula tools

### D. Knowledge + Planning
- Local catalog of materials, tools, and build parts
- Local market snapshot for item worth estimation from scans/text (e.g., golf balls)
- Goal decomposition into steps, dependencies, and risk warnings
- Predictive outcomes with confidence and assumptions

### E. Cross-Device Bridge
- Local-only discovery (USB or LAN)
- Signed/verified command envelopes
- Capability-scoped permissions (typing/clicking/app control optional)

### F. Security + Privacy
- Encrypted local databases and logs
- Voice authentication hook points
- Tamper-evident activity logs

### G. Daily Update Service
- Local feed import + snapshot merge pipeline
- Runs every day at **12:00 AM local time** via platform scheduler
- Stores last-success timestamp to prevent duplicate same-day refreshes

## 4) Safety and Governance

For any chemistry/physics workflow:
1. Detect hazards from material + condition combinations.
2. Require confirmation for medium/high risk actions.
3. Prefer simulated alternatives before real-world execution.
4. Emit “limitations and assumptions” in every predictive output.

## 5) Scalability Model

- Keep simulation APIs stable while replacing internals with higher fidelity models.
- Separate UI rendering from assistant and simulation logic.
- Use versioned protocol envelopes for backward-compatible Android/Windows interop.

## 6) Suggested Tech Stack

- **Android:** Kotlin + Jetpack Compose + local runtime bindings
- **Windows:** Kotlin Compose Desktop or .NET desktop host
- **Core Engine:** Python module (current baseline) callable from both platforms
- **3D/AR/VR:** Unity or Godot as pluggable renderer tier
- **Data:** SQLite + encrypted file storage
