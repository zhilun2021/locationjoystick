# Floating Widget

Small floating button overlay. Tap to expand a panel with configured quick-access controls. Items configured in Settings.

Key files: `:feature:widget:impl/FloatingWidgetService.kt`, `:feature:settings:impl/SettingsScreen.kt`

## Mechanism

- Same overlay mechanism as the joystick via `:core:overlay`.
- Separate service, toggled independently of the joystick.
- State transitions: collapsed (FAB) ↔ expanded (panel) via `ValueAnimator`.
- Items stored in DataStore as `stringSetPreferencesKey`.

## Service Lifecycle

- Binds to `MockLocationService` in `onStartCommand`.
- Unbinds in `onDestroy`.

## Completion Badge

A red dot appears at the top-right of the widget FAB when a route, walk, or roaming session ends naturally (completion, not user-initiated stop). The badge is driven by `pendingCompletionFlow: MutableStateFlow<Boolean>` in `FloatingWidgetService`, set on `mapController.completionMessages` emission.

- **Trigger**: any natural completion event (route replay end, walk-to arrival, roaming loop end).
- **Cleared**: when the user taps the FAB to expand the panel (`isPanelExpandedFlow` becomes `true`).
- **Does not appear**: when the user manually stops a session.

## Floating Map — Route Controls

When `WidgetFeature.MAP_FLOATING` is enabled, the floating map's FAB column includes a route button:

- **Shown when**: `MapFabFeature.ROUTES` is enabled in Settings **or** a route replay is currently active.
- **Active state**: route icon turns green (`LjSuccess`) during `ROUTE_REPLAY` mode.
- **Expand controls**: tapping the route button expands two inline buttons to the left:
  - **Stop** — ends the replay immediately.
  - **Pause / Resume** — toggles replay pause state.
- **Settings gate**: `enabledMapFabFeatures` flows through `MapSharedState` so the floating map respects the same visibility toggle as the main map screen.

## Edge Cases

- No items configured → show placeholder.
- Clamp panel to screen bounds.
- Re-clamp on `onConfigurationChanged`.

## ELEVATION_CONTROLS Feature

When `ELEVATION_CONTROLS` is enabled in Experimental Settings (requires root), it appears in the widget panel as a 3-button column:

| Button | Icon | ElevationMode |
|--------|------|---------------|
| ↑ | KeyboardArrowUp | `TiltUp` |
| ○ | RadioButtonUnchecked | `Neutral` |
| ↓ | KeyboardArrowDown | `TiltDown` |

The active mode is highlighted with `MaterialTheme.colorScheme.primary`; inactive buttons use `LjInactive`. Tapping a button calls `onElevationModeSelected`, which updates `_elevationMode` in `FloatingWidgetService` and relays the new mode to `MockLocationService.setElevationMode()`.

`ELEVATION_CONTROLS` is filtered out of `getWidgetFeatures()` automatically when elevation controls is disabled in settings — it never appears in the widget panel for non-root users.
