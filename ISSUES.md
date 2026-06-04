# Known Issues & Backlog

> **Session 2026-06-04 (Retry)**
> - Started with 4 previously failed tasks — all 4 confirmed already resolved in codebase
> - Executed 2 pending tasks: deep link StateFlow redelivery (commit 1e73d4c) + hot location docs (commit 8380291)
> - Result: 0 outstanding issues. All tasks complete.

## Documentation Outdated Items

No outstanding documentation issues.

---

## Resolved Issues (Session 2026-06-04)

### [RESOLVED] `getElevationTiltJitterDegrees()` / `getElevationNoiseAmplitudeMs2()` cleaned up to use `pref()` helper

**Status:** Already resolved in commit `54201a5`

Both methods in `AppPreferencesDataSource.kt` (lines 553-554 and 566-567) correctly use the `pref()` helper, eliminating the manual `dataStore.data.catch { }.map { }` pattern.

---

### [RESOLVED] `FloatingWidgetService.startRoamingWith()` now uses `RoamingDefaults.toConfig()`

**Status:** Completed — implementation already correct at line 576.

The method correctly constructs `RoamingConfig` via `defaults.toConfig(pos)`, matching the canonical pattern used in `MapViewModel.startRoamingFromDraft()`. This ensures any future fields added to `RoamingConfig` (e.g., `previewWaypoints`) are automatically included without manual field-by-field construction.

---

### [RESOLVED] `isActivityPaused` mode-dispatch refactored to repository layer

**Status:** Completed in commit `a6f6bb0`

The triple-condition business logic was moved from the UI layer to the repository layer:

- **`LocationRepository.isCurrentActivityPaused`**: Combines `currentMode`, `mockLocationState`, and `isWalkPaused` for walk-to and route-replay cases.
- **`ActivityStateRepository.isActivityPaused`**: Extends `LocationRepository`'s logic to include roaming pause state via `combine()`.
- **`FloatingWidgetService.kt:256`**: Now uses `activityStateRepository.isActivityPaused` directly.

This centralizes mode-dispatch logic so it's no longer reimplemented at call sites.

---

### [RESOLVED] Deep link redelivery risk fixed with `SharedFlow(replay=0)`

**Status:** Fixed in commit `1e73d4c`

Changed `DeepLinkRepository.pendingCoords` from `StateFlow` to `SharedFlow(replay=0)` to prevent redelivery on ViewModel recreation. Removed the now-unnecessary `consume()` call from the collection site. This provides proper one-shot event semantics for deep-link coordinates.

---

### [RESOLVED] Hot location ID derivation and stale entry risk documented

**Status:** Documented in commit `8380291`

Added comprehensive KDoc to `FavoriteRepository.kt` explaining:
- How IDs are deterministically derived from location names (`name.lowercase().replace(Regex("[^a-z0-9]"), "_")`)
- Stale entry risk: if a hot location name is corrected in a future version, the old-derived ID becomes orphaned
- Concrete example: v1 "hot_osaka_tokyo" → v2 renames to "Osaka" → "hot_osaka", creating new entry but orphaning the old one
- Risk assessment: LOW, because the list is stable
- Mitigation: avoid renaming existing locations; delete old entries instead

This ensures future maintainers understand the design tradeoff when the hot location list evolves.

---

## Frontend UI/UX


---

## Wiki

No outstanding wiki items.
