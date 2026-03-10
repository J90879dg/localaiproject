# Implementation Plan (Practical, Offline-First)

## Phase 0 — Foundation (done in this repo baseline)
- Define module boundaries and data contracts
- Add deterministic offline chemistry/physics helper engines
- Add local project planner with materials/tool database
- Add signed cross-device message envelope format
- Add tests to lock expected core behavior

## Phase 1 — Usable MVP
1. **Android shell**
   - Compose screens: chat, project planner, lab dashboard
   - Local voice/text input adapters
2. **Windows shell**
   - Desktop panel for linked device status and automation control
3. **Assistant Orchestration**
   - Connect UI requests to `offline_ai` APIs
   - Add session memory persistence
4. **Safety UX**
   - Hazard banners, protocol prompts, user acknowledgment flow

Exit criterion: User can submit goals/chemicals and receive offline plans + simulated outcomes.

## Phase 2 — Simulation Depth
1. Expand chemistry models:
   - Better balancing coverage
   - Thermodynamics and kinetics approximations
2. Expand physics models:
   - Circuit solver and field visualizer
   - Aerodynamics model presets
3. Add confidence scoring and assumptions trace output

Exit criterion: User can run multi-step virtual experiments with interpretable outputs.

## Phase 3 — Visual + Avatar Layer
1. Integrate 2D/3D avatar with response events
2. Introduce 3D lab scene interaction hooks
3. Add AR/VR compatibility layer (optional build flavor)

Exit criterion: End-to-end avatar + interactive virtual lab demo.

## Phase 4 — Cross-Device Automation
1. Add secure pairing + key management
2. Implement capability-scoped remote actions
3. Add signed command queue and audit logs

Exit criterion: Android↔Windows command relay with explicit permissions.

## Phase 5 — Adaptive Local Intelligence
1. Local model update scheduler (nightly job)
2. Personalization policy with privacy controls
3. Offline learning from approved user corrections

Exit criterion: Adaptive behavior without cloud dependency.

## Non-Functional Requirements
- Startup latency targets for low-end Android devices
- Deterministic fallback when no GPU/accelerator is available
- Comprehensive local logging and export controls
- Strictly offline core path always available

## Risks and Mitigations
- **Risk:** Overpromised simulation fidelity
  - **Mitigation:** Tiered fidelity labels and explicit assumptions.
- **Risk:** Unsafe real-world execution
  - **Mitigation:** Safety gate + warnings + confirmation checkpoints.
- **Risk:** Device-bridge misuse
  - **Mitigation:** Strong auth, signed envelopes, permission scopes, local-only links.
