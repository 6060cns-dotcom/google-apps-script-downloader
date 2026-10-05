# Music Radio - Compose Multiplatform Application

## Project Structure

```
├── shared/            # ← ALL: business logic, UI, utilities, data
└── *App/              # ← thin wrapper: entry point
```

**Rule:** All project code - business logic, UI, navigation, data, utilities - goes in the `shared` module.
Platform modules (`androidApp`, `desktopApp`, `webApp`) are thin wrappers containing only an entry point (`main()` / `Activity`).

## Build & Run

| Target | Command |
|--------|---------|
| **Desktop** (run) | `./gradlew :desktopApp:run` |
| **Desktop** (hot reload) | `./gradlew :desktopApp:hotRun --auto` |
| **Android** (debug APK) | `./gradlew :androidApp:assembleDebug` |
| **Web** (Wasm) | `./gradlew :webApp:wasmJsBrowserDevelopmentRun` |
| **Web** (JS, older browsers) | `./gradlew :webApp:jsBrowserDevelopmentRun` |
| **Web** (PWA dist) | `./gradlew :webApp:wasmJsBrowserDistribution` |
| **GitHub Pages** | `website/` at `/`, Wasm app at `/live` (`.github/workflows/deploy-pages.yml`) |
| **Compose stability check** | `./gradlew :shared:compileKotlinJvm :shared:stabilityCheck` |
| **Lint** | `./gradlew detekt ktlintCheck` |

## Build Verification Order

When checking correctness, compile in this order:

1. **JVM** - fastest feedback loop
2. **Android**
3. **Wasm** - `:shared:compileKotlinWasmJs` then `:webApp:compileKotlinWasmJs`

## Architecture

Single-state MVI. There is one `AppState`, one `AppViewModel`, and one `onIntent` entry point;
every screen is a pure function of that state.

- `domain/` - the station catalog and the settings model. No Compose, no I/O.
- `data/` - the `key=value` snapshot on disk (`AppStore`, `SnapshotCodec`).
- `player/` - `RadioPlayer`, the app's view of playback, over Compose Media Player's audio module.
- `app/` - `AppState` / `AppIntent` / `AppViewModel` / `RootScreen`, plus Navigation 3's back stack.
- `main/`, `ui/` - Compose screens and widgets.
- `platform/` - `expect`/`actual` for the handful of things the JVM, Android and the browser split on.
- `di/` - Metro graph.

**Adding a station:** edit `domain/Stations.kt`, drop the artwork in
`shared/src/commonMain/composeResources/drawable/`, and add the name to all three `strings.xml`.

**Adding a setting:** field on `UserSettings` → key in `SnapshotCodec` → intent → row in
`SettingsScreen`. Every step is needed; a setting that skips `SnapshotCodec` silently forgets itself.

## Dependency Management

- Use **version catalog** (`gradle/libs.versions.toml`) for all dependencies.
- Compose, Kotlin, coroutines and `composemediaplayer-audio` are pinned to one train (Kotlin 2.4.10 /
  Compose 1.11.1). Nucleus is built against Compose 1.11; bumping material3 past 1.11 drags
  `compose.ui` to 1.12 and breaks the decorated window.

## Library Selection

When searching for a Kotlin Multiplatform library to solve a task:

1. Check **[kmp-awesome](https://raw.githubusercontent.com/terrakok/kmp-awesome/refs/heads/master/README.MD)** for curated recommendations.
2. Query **[klibs.io MCP](https://api.klibs.io/mcp)** to verify supported targets, coordinates, latest
   version, license, and maintenance signals.

## Platform-Specific Code

1. **First, try to find a ready-made library** with KMP support.
2. If no ready solution exists, use **`expect` / `actual`**:
   - Interfaces and `expect` declarations - in `commonMain`
   - `actual` implementations - in `androidMain`, `jvmMain`, `webMain` (js + wasmJs)
   - Browser interop that cannot be shared goes in `jsMain` / `wasmJsMain`

## General Guidelines

1. **Kotlin Multiplatform First** - avoid platform-specific code in `commonMain`.
2. **Compose Multiplatform** - one UI for every platform.
3. **Coroutines & Flow** - for asynchronous work and reactive data.
4. **Keep it Simple** - choose the simplest working approach.
5. **Ask Before Complex Changes** - significant architectural decisions go to the user first.
