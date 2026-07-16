# Speed Profiles

Five presets: Slow Walk, Walk, Run, Bike, Drive. All user-editable. Applies to joystick, route replay, and roaming.

Key files: `:feature:settings:impl/SettingsScreen.kt`, `:core:data/SettingsRepository.kt`

## Presets

| Profile | Constant |
|---------|----------|
| Slow Walk | `AppConstants.ProfileConstants.SLOW_WALK_SPEED_MPS` |
| Walk | `AppConstants.ProfileConstants.WALK_SPEED_MPS` |
| Run | `AppConstants.ProfileConstants.RUN_SPEED_MPS` |
| Bike | `AppConstants.ProfileConstants.BIKE_SPEED_MPS` |
| Drive | `AppConstants.ProfileConstants.DRIVE_SPEED_MPS` |

## Behaviour

- Stored in DataStore.
- UI: scrollable segmented control (roaming default) and individually labeled speed inputs (Settings screen).
- Changes take effect immediately on the next tick.
- Widget's Speed Cycle feature cycles through all five presets in order via `SettingsRepository.getSpeedProfiles()`.

## Constraints

- Speed clamped to `AppConstants.ProfileConstants.MIN_SPEED_MS`–`AppConstants.ProfileConstants.MAX_SPEED_MS`.
- Inline warning shown below speed input when speed exceeds `AppConstants.ProfileConstants.ANTI_CHEAT_WARNING_THRESHOLD_MS`. Warning uses generic language — no specific game names. Drive's default speed exceeds this threshold by design.
