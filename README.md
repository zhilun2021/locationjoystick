# locationjoystick

![Build](https://img.shields.io/github/actions/workflow/status/locationjoystick/locationjoystick/release.yml?label=Build&style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)
![minSdk](https://img.shields.io/badge/minSdk-31%20(Android%2012)-green?style=flat-square)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple?style=flat-square)

No-root mock location app for Android. Spoof GPS anywhere using floating joystick, saved routes, and OSM-powered roaming — without touching system partition.

---

## What is locationjoystick?

GPS spoofing app built on Android's official mock location API. No root, no Xposed, no system mods. Enable Developer Options, pick locationjoystick as mock location provider → device believes it's wherever you say.

Primary use case: location-based games like Pokémon GO. Walk saved routes, roam a neighborhood automatically, or nudge position with floating joystick while game runs in foreground. App keeps spoofing in background — no need to switch away.

Also useful for: privacy (mask real location from demanding apps), QA testing (simulate movement at desk), development (test geofences, location triggers, map features against controlled GPS feed).

---

## Features

### Map
- OpenStreetMap base layer via MapLibre (GPU-accelerated, offline-capable)
- Tap any map point to walk or teleport there instantly
- Spoofed position shown as live marker

### Joystick
- Floating joystick overlay stays on top of any app
- Move in any direction at chosen speed
- Draggable — position anywhere on screen
- Persists while app is minimized

### Movement Speeds
- Three configurable speeds: Walk, Run, Bike
- Each speed user-editable (custom m/s values)
- Speed selector accessible from floating widget

### Routes
- Create routes by placing waypoints on map
- Save, rename, edit, delete routes
- Replay any saved route from start to finish
- Loop routes continuously
- Record route in real time — press record, move, press stop, save

### Roaming
- Set radius (e.g. 2 km) and duration (e.g. 30 min)
- App walks position around that area automatically
- Default: smooth random movement within radius
- Road-following mode: routes along real OSM roads via OSRM

### Favorite Locations
- Save any map position as favorite
- Rename and delete favorites
- Instantly teleport to any saved favorite

### Floating Widget
- Configurable quick-access panel floats over other apps
- Choose which controls appear: speed selector, route controls, roaming toggle, favorites
- Collapse when not needed

### Background Behavior
- Continues spoofing while app minimized or screen off
- Runs as foreground service with persistent notification
- Low-priority notification channel — no sound, minimal battery impact

### Import / Export
- Export all settings to JSON: routes, favorites, speed presets, widget config
- Import from JSON to restore or share setup across devices

---

## Screenshots

> Screenshots coming soon. Build the app and explore!

---

## Download

Pre-built APKs on [Releases page](https://github.com/locationjoystick/locationjoystick/releases).

Download latest `locationjoystick-vX.X.X.apk` and sideload:

```bash
adb install locationjoystick-vX.X.X.apk
```

Or transfer APK to device and open with file manager (allow installs from unknown sources).

---

## Setup Guide

Uses Android's built-in mock location system. No root required, but Developer Options must be enabled once.

### Step 1 — Enable Developer Options

1. Open **Settings** on your Android device
2. Go to **About phone**
3. Tap **Build number** seven times
4. Developer Options now unlocked under **Settings > System > Developer Options**

### Step 2 — Select Mock Location App

1. Open **Settings > System > Developer Options**
2. Scroll to **Select mock location app**
3. Choose **locationjoystick**

### Step 3 — Grant Overlay Permission

locationjoystick needs "Display over other apps" permission for the floating joystick.

1. Open locationjoystick
2. When prompted, tap **Grant Permission**
3. Find locationjoystick in list, enable toggle
4. Return to app

### Step 4 — Start Spoofing

1. Open locationjoystick
2. Tap anywhere on map to teleport, or use joystick to move
3. Open target app (e.g. Pokémon GO) — it sees spoofed location
4. locationjoystick keeps running in background

> **Note:** Some apps detect mock locations. Check the app's community for current workarounds.

---

## Directory Structure

```
locationjoystick/
├── app/                          # Application module (entry point)
│   ├── src/main/
│   │   ├── kotlin/com/locationjoystick/app/
│   │   │   ├── MainActivity.kt   # Single-activity host
│   │   │   └── LocationJoystickApp.kt  # Application class, Hilt entry point
│   │   ├── AndroidManifest.xml
│   │   └── res/
│   └── build.gradle.kts
│
├── core/                         # Shared library modules
│   ├── model/                    # Pure Kotlin data models (no Android deps)
│   │   └── src/main/kotlin/com/locationjoystick/core/model/
│   │       ├── Location.kt       # LatLng wrapper
│   │       ├── Route.kt          # Route + waypoints
│   │       ├── FavoriteLocation.kt
│   │       ├── SpeedPreset.kt    # Walk/Run/Bike speed config
│   │       └── RoamingConfig.kt  # Radius + duration settings
│   │
│   ├── data/                     # Repository implementations
│   │   └── src/main/kotlin/com/locationjoystick/core/data/
│   │       ├── RouteRepository.kt
│   │       ├── FavoritesRepository.kt
│   │       └── SettingsRepository.kt
│   │
│   ├── database/                 # Room database
│   │   └── src/main/kotlin/com/locationjoystick/core/database/
│   │       ├── LocationJoystickDatabase.kt
│   │       ├── dao/RouteDao.kt
│   │       ├── dao/FavoriteDao.kt
│   │       └── entities/         # Room entity classes
│   │
│   ├── datastore/                # DataStore preferences
│   │   └── src/main/kotlin/com/locationjoystick/core/datastore/
│   │       └── UserPreferencesDataSource.kt
│   │
│   ├── location/                 # Mock location engine
│   │   └── src/main/kotlin/com/locationjoystick/core/location/
│   │       ├── MockLocationService.kt   # ForegroundService, pushes GPS at 1Hz
│   │       ├── MockLocationProvider.kt  # Wraps LocationManager.addTestProvider()
│   │       └── MovementEngine.kt        # Interpolates positions for smooth movement
│   │
│   ├── routing/                  # OSRM road-following integration
│   │   └── src/main/kotlin/com/locationjoystick/core/routing/
│   │       ├── OsrmClient.kt     # HTTP client for router.project-osrm.org
│   │       └── RouteInterpolator.kt
│   │
│   ├── ui/                       # Shared Compose components + theme
│   │   └── src/main/kotlin/com/locationjoystick/core/ui/
│   │       ├── theme/            # Material3 color scheme, typography
│   │       └── components/       # Reusable composables
│   │
│   └── common/                   # Utilities, extensions, constants
│       └── src/main/kotlin/com/locationjoystick/core/common/
│
├── feature/                      # Feature modules (each owns its UI + ViewModel)
│   ├── map/                      # Main map screen
│   │   └── src/main/kotlin/com/locationjoystick/feature/map/
│   │       ├── MapScreen.kt
│   │       └── MapViewModel.kt
│   │
│   ├── joystick/                 # Floating joystick overlay
│   │   └── src/main/kotlin/com/locationjoystick/feature/joystick/
│   │       ├── JoystickOverlayService.kt
│   │       └── JoystickView.kt
│   │
│   ├── routes/                   # Route list, editor, recorder
│   │   └── src/main/kotlin/com/locationjoystick/feature/routes/
│   │       ├── RouteListScreen.kt
│   │       ├── RouteEditorScreen.kt
│   │       └── RouteRecorderScreen.kt
│   │
│   ├── favorites/                # Favorite locations
│   │   └── src/main/kotlin/com/locationjoystick/feature/favorites/
│   │       ├── FavoritesScreen.kt
│   │       └── FavoritesViewModel.kt
│   │
│   ├── roaming/                  # Roaming configuration + control
│   │   └── src/main/kotlin/com/locationjoystick/feature/roaming/
│   │       ├── RoamingScreen.kt
│   │       └── RoamingViewModel.kt
│   │
│   ├── widget/                   # Floating widget overlay
│   │   └── src/main/kotlin/com/locationjoystick/feature/widget/
│   │       ├── FloatingWidgetService.kt
│   │       └── WidgetConfigScreen.kt
│   │
│   └── settings/                 # Speed presets, import/export, permissions
│       └── src/main/kotlin/com/locationjoystick/feature/settings/
│           ├── SettingsScreen.kt
│           └── SettingsViewModel.kt
│
├── .github/
│   └── workflows/
│       └── release.yml           # Builds APK and publishes to GitHub Releases on tag push
│
├── gradle/
│   └── libs.versions.toml        # Version catalog (single source of truth for deps)
│
├── build.gradle.kts              # Root build script
├── settings.gradle.kts           # Module declarations
├── AGENTS.md                     # Code standards and architecture guide for contributors
└── README.md
```

---

## Architecture

Follows [NowInAndroid](https://github.com/android/nowinandroid) multi-module architecture. Each layer has clear responsibility, depends only on layers below.

```
┌─────────────────────────────────────────┐
│              feature/*                  │  UI + ViewModels
│  map  joystick  routes  favorites  ...  │  (Compose screens, no business logic)
└────────────────┬────────────────────────┘
                 │ depends on
┌────────────────▼────────────────────────┐
│              core/data                  │  Repositories
│   RouteRepository  FavoritesRepository  │  (single source of truth)
└──────┬──────────────────────┬───────────┘
       │                      │
┌──────▼──────┐    ┌──────────▼──────────┐
│ core/database│    │  core/datastore     │
│   (Room)    │    │  (DataStore Prefs)  │
└─────────────┘    └─────────────────────┘

┌─────────────────────────────────────────┐
│           core/location                 │  Mock GPS engine (ForegroundService)
│   MockLocationService  MovementEngine   │  independent of UI layer
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│            core/model                   │  Pure Kotlin data classes
│   Route  FavoriteLocation  SpeedPreset  │  no Android dependencies
└─────────────────────────────────────────┘
```

Hilt provides DI across all modules. Navigation handled by single `NavHost` in `MainActivity` with type-safe routes.

---

## Tech Stack

| Component | Library / Technology |
|-----------|---------------------|
| Language | Kotlin 2.x |
| UI | Jetpack Compose + Material3 |
| Map | MapLibre Android SDK 12.x |
| DI | Hilt (Dagger) |
| Database | Room |
| Preferences | DataStore (Proto) |
| Routing | OSRM (router.project-osrm.org) |
| Serialization | kotlinx-serialization (JSON) |
| Async | Kotlin Coroutines + Flow |
| Build | Gradle with Version Catalog (libs.versions.toml) |
| CI | GitHub Actions |
| Min SDK | API 31 (Android 12) |

---

## Building

### Prerequisites

- Android Studio Hedgehog or newer (or JDK + Android SDK command-line tools)
- Java 17
- Android SDK with API 31+

### Clone and build

```bash
git clone https://github.com/locationjoystick/locationjoystick.git
cd locationjoystick
./gradlew assembleDebug
```

Debug APK at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install directly:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release build

Release APKs built automatically by GitHub Actions on tag push:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Workflow builds, signs (if keys configured), uploads APK to GitHub Releases.

---

## Contributing

PRs welcome. Before opening one:

1. Read [AGENTS.md](AGENTS.md) — covers code standards, module conventions, architecture rules
2. Keep changes focused. One feature or fix per PR
3. `./gradlew build` must pass before submitting

Adding new feature → open issue first to discuss approach. Keeps architecture consistent, avoids duplicate work.

---

## License

MIT License. See [LICENSE](LICENSE) for full text.
