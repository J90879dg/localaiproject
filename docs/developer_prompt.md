# Developer Prompt: Ultimate Virtual Lab + Personal AI Assistant

Use this prompt when tasking engineering agents:

---
Build a modular, offline-first cross-platform system for Android and Windows named **Ultimate Virtual Lab + Personal AI Assistant**.

## Product Requirements

1. **Virtual Chemistry Lab**
   - High-fidelity simulation tiers for reactions under variable temperature, pressure, and concentration.
   - Input supports project goal text, chemical names, and image-derived material hints.
   - 3D interactive lab workflows with equipment interaction abstractions.
   - Mandatory safety warnings, protocol guidance, and predictive outcome suggestions.

2. **Physics Simulator**
   - Mechanics, electricity, magnetism, aerodynamics, relativity, and quantum calculation modules.
   - Explain assumptions and confidence for each simulation result.
   - Suggest purchasable real-world items for educational builds.

3. **AI Personal Assistant**
   - Voice + text + image input.
   - Visual avatar output channel.
   - Custom wake word and optional voice authentication.
   - Local problem-solving and creative ideation with no cloud dependency.

4. **Cross-Device Communication**
   - Android↔Windows local communication over USB or trusted LAN.
   - Signed, authenticated command protocol with capability-scoped permissions.
   - Optional remote control actions (typing/clicking/app automation) behind explicit consent.

5. **Security and Privacy**
   - Encrypt local data at rest.
   - Store logs locally with tamper-evident integrity checks.
   - Provide user-controlled privacy settings and audit export.

## Engineering Constraints

- Offline core path must function without internet.
- Modular architecture for future robotics integration.
- Performance-conscious design for mobile and desktop hardware differences.
- Versioned internal APIs and message schemas for long-term maintainability.

## Deliverables

1. End-to-end architecture and data flow documentation.
2. Implemented offline core modules (simulation, planner, safety, orchestration, protocol).
3. Unit tests for deterministic outputs and security checks.
4. Android and Windows integration skeletons aligned to shared contracts.
5. Roadmap for 3D/AR/avatar and adaptive local learning upgrades.
---
