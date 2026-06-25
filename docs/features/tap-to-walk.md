# Tap to Walk

Two shortcuts for triggering walk-to without a confirmation sheet, useful when you want to act quickly inside another app.

Key files: `:feature:widget:impl/MapFloatingView.kt`, `:feature:widget:impl/TapToWalkOverlay.kt`, `:feature:widget:impl/FloatingWidgetService.kt`, `:feature:settings:impl/SettingsScreen.kt`

## Settings

Both features live in Settings → Menus → Tap to Walk.

| DataStore key | Type | Default | Description |
|---|---|---|---|
| `FLOATING_MAP_QUICK_WALK` | Boolean | `false` | Skip confirmation sheet on floating map taps |
| `TAP_TO_WALK_OVERLAY_ENABLED` | Boolean | `false` | Show crosshair button in widget panel |
| `TAP_TO_WALK_SCALE_MPX` | Double | `10.0` | Meters per pixel for pixel→GPS conversion |

Scale is clamped to `AppConstants.TapToWalkConstants.MIN_SCALE_MPX`–`MAX_SCALE_MPX` (1–50 m/px) in `applySnapshot()`.

## Tier 1 — Floating Map Quick Walk

`MapFloatingView` exposes a `quickWalk: Boolean` parameter. When `true`, `addOnMapClickListener` calls `onWalkTo(pos)` directly instead of setting `pendingTap` to open `TapActionPanel`.

`WidgetPanelPresenter.showMapFloatingView()` reads `settingsRepository.getFloatingMapQuickWalk()` as a composable state and passes it to `MapFloatingView`.

Uses `rememberUpdatedState` for both `quickWalk` and `onWalkTo` so the `AndroidView.factory` callback (which runs once) always captures the latest values.

## Tier 2 — Screen Tap-Intercept Overlay

`TapToWalkOverlay` adds a `TYPE_APPLICATION_OVERLAY` window with `FLAG_NOT_FOCUSABLE` but **without** `FLAG_NOT_TOUCH_MODAL`, so it intercepts all touches on the screen.

`FloatingWidgetService` shows a crosshair button in `WidgetPanel` when `isTapToWalkEnabled` (driven by `getTapToWalkOverlayEnabled()`). Tapping the button calls `onTapToWalkClicked()`, which creates and shows a `TapToWalkOverlay`.

The overlay shows:
- Semi-transparent background (5% black) that captures all taps
- Hint text at top center
- Cancel button (close icon, bottom-right) that dismisses the overlay

### Pixel → GPS Formula

```
dx_m = (tapX - screenW/2) * metersPerPixel
dy_m = -(tapY - screenH/2) * metersPerPixel
newLat = currentLat + (dy_m / 6_371_000) * (180/π)
newLon = currentLon + (dx_m / 6_371_000) * (180/π) / cos(currentLat * π/180)
```

**North-up assumption**: the formula assumes the game map is north-up. Accuracy depends on the scale setting matching the game's actual zoom level.

`TapToWalkOverlay.computeWalkTarget(...)` is a `companion object` pure function — no Android deps, testable directly.

## Warning Dialog

Enabling the overlay shows an `AlertDialog` with three caveats before activating:
1. Some apps detect mock location activity and may penalise the account.
2. Accuracy depends on the scale setting matching the game's zoom level.
3. Zoom out for better precision; zoomed-in maps amplify offset errors.

User must confirm "Enable anyway" or cancel. Cancel leaves the toggle off. State is local (`rememberSaveable`) so the dialog re-shows if the user disables and re-enables.

## Anti-Patterns to Avoid

- Do not add `FLAG_NOT_TOUCH_MODAL` to the overlay — this breaks interception.
- Do not re-read position inside the tap callback — capture it once via `getPosition()` at tap time.
- Do not persist `isTapToWalkActive` in DataStore — it is transient session state in `MutableStateFlow`.
