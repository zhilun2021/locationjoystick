# Known Issues & Backlog

## Documentation Outdated Items

No outstanding documentation issues.

---

## Bugs

No outstanding bugs.

---

## Technical Debt (pre-1.1)

- **FloatingWidgetService decomposition** — 923 lines, approaching 1k limit. Extract floating-view builders into `WidgetPanelPresenter` collaborator; extract 3 `ServiceConnection`s into `WidgetServiceBinder` holder. Target: <500 lines of pure lifecycle/window plumbing. No unit tests exist for this class.
- **RealismSettingsState extraction** — `MockLocationService` holds 25 `@Volatile` realism fields + 15 `observeSetting()` wirings inline. Extract into a `RealismSettingsState` class that owns those fields and exposes `observe(scope, repo)` + `captureInto(snapshot)`.
- **Position writeback race** — `locationRepository.currentPosition.collect` writes `currentLat/currentLon` on `Dispatchers.Default`; tick loop reads them separately. Funnel all position writes through a single entry point to eliminate the TOCTOU window.

---

## Frontend UI/UX

No outstanding UI/UX items.

---

## Wiki

No outstanding wiki items.
