# Shared Contracts

This folder stores cross-platform data contracts used by both Android and Windows shells.

- `contracts/OfflineContracts.kt`
  - Scan + valuation request/response models
  - Identified item and valuation payloads

Keeping contracts here ensures both apps speak the same protocol while staying in separate platform codebases.
