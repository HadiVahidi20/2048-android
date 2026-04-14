# NumberMerge2048 Rebuild - Session Memory

Last updated: 2026-04-01
Project path: `c:\MyDev\Hadify\Andriod Apps\2048-android`
Play package name: `com.hadify.NumberMerge2048`

## 1) Goal and context

This project is a rebuild of the original Play Store app after source loss.
The goal is to keep the same package name and ship updates again.

## 2) Current build/config status

- Tech: Native Android, Kotlin, Jetpack Compose (single activity).
- Main app logic is now modularized across:
  - app shell + theme tokens:
    - `app/src/main/java/com/hadify/NumberMerge2048/MainActivity.kt`
  - UI app entry:
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/UiScreens.kt`
  - shared UI components:
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/components/GlassPanel.kt`
  - screen modules:
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/HomeScreen.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/BoardSetupScreen.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/DailyChallengesScreen.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/HowToPlayScreen.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/PowerupStoreScreen.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/GameScreen.kt`
  - core models/session/coordinator/persistence:
    - `app/src/main/java/com/hadify/NumberMerge2048/core/CoreModels.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/core/Persistence.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/core/DailyChallengeFactory.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/core/GameSession.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/core/AppCoordinator.kt`
    - `app/src/main/java/com/hadify/NumberMerge2048/core/StoreCatalog.kt`
- Build config:
  - `compileSdk = 36`
  - `targetSdk = 36`
  - `minSdk = 24`
  - `versionCode = 1000`
  - `versionName = "1.0.0-rebuild"`
- Debug build uses:
  - `applicationIdSuffix = ".dev"`
  - `versionNameSuffix = "-dev"`
  - So debug package is different from Play package.
- Release signing is configured through `keystore.properties` (local, ignored by git).

## 3) Implemented features so far

### Core gameplay

- Playable 2048 engine with swipe gestures.
- Board sizes: 4x4, 5x5, 6x6.
- Score and best score.
- Win and game-over detection.
- Continue latest in-memory run while app process is alive.

### Power-ups

- Implemented powers:
  - Swap
  - Undo
  - Bomb
  - Freeze
- Power usage includes coin costs and remaining charge counts.
- Power icons are custom drawables:
  - `ic_power_swap.xml`
  - `ic_power_undo.xml`
  - `ic_power_bomb.xml`
  - `ic_power_freeze.xml`

### Economy + rewards

- Coin balance integrated into gameplay.
- Daily home reward claim.
- Coin rewards from challenge completion.

### Daily challenges

- Tiered challenge set with generated daily seed.
- Challenge chains (multi-stage objectives).
- Challenge restrictions exist (max moves, max bomb usage, max power usage, no-power mode).
- Claim/reward flow and unlock-next behavior implemented.

### Power-up store (recently completed)

- Store moved from simple buttons to offer-based system.
- Data model:
  - `StoreCategory`
  - `StoreOffer`
  - `powerupStoreOffers`
- Categories in UI:
  - Utility
  - Single Charges
  - Power Packs
- Purchase flow:
  - wallet check
  - active-session requirement check for power-up charges
  - coin deduction/addition
  - immediate application of purchased charges to active run
  - status message feedback

### Audio

- Basic sound feedback added with `ToneGenerator`.
- Sound toggle added.
- Sound on/off icons:
  - `ic_sound_on.xml`
  - `ic_sound_off.xml`

### UI and visual work

- Dark themed visual style with gradient background and glass-like panels.
- Multiple UI revisions applied.
- Global text visibility issue fixed:
  - Explicit `contentColor` at root `Surface`
  - `onBackground` set in theme
  - Header/icon tint fixes on dark backgrounds
- App icon drawable added:
  - `ic_app_icon.xml`
- Manifest icon refs added.
- Screen-level user-facing text has been moved to:
  - `app/src/main/res/values/strings.xml`
- Deprecated back icon usage in screen modules was updated to AutoMirrored icons.

## 4) Persistence (important)

### Persisted currently (SharedPreferences JSON, key `state_v2`)

- `coinBalance`
- `bestScore`
- `homeDailyClaimed`
- `boardSizeSelection`
- `soundEnabled`
- challenge state:
  - unlocked
  - completed
  - claimed
  - bestProgress
  - statusNote
- full session snapshots:
  - per-size regular runs
  - active challenge run (when active)
  - board values
  - frozen-turn map
  - score/best score/coins
  - power charges and selected power mode
  - undo stack snapshots
  - challenge runtime counters (stage index, move/power usage, completion/failure)
- day key for daily challenge cycle

### Not fully persisted yet

- Difficulty metadata is persisted, but legacy runs created before difficulty support may be normalized to Medium on first re-save.

## 4.1) Difficulty system (recently completed)

- Real selectable difficulty added: Easy / Medium / Hard.
- Difficulty is selected in board setup screen and persisted.
- Continue by selection now uses board-size + difficulty slot.
- Gameplay impact implemented:
  - different starting power-up charges per difficulty
  - different tile spawn rates (chance of `4` tile)
  - Easy-only chance for bonus extra tile spawn after a move
  - power-up coin cost delta by difficulty
  - coin reward scaling from merges by difficulty
- Challenge runs currently start on Medium for balance consistency.

## 5) Key files

- App shell/theme:
  - `app/src/main/java/com/hadify/NumberMerge2048/MainActivity.kt`
- UI screens:
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/UiScreens.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/components/GlassPanel.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/HomeScreen.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/BoardSetupScreen.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/DailyChallengesScreen.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/HowToPlayScreen.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/PowerupStoreScreen.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/ui/screens/GameScreen.kt`
- Core gameplay/state:
  - `app/src/main/java/com/hadify/NumberMerge2048/core/CoreModels.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/core/GameSession.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/core/AppCoordinator.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/core/Persistence.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/core/DailyChallengeFactory.kt`
  - `app/src/main/java/com/hadify/NumberMerge2048/core/StoreCatalog.kt`
- Build config:
  - `app/build.gradle.kts`
- Manifest:
  - `app/src/main/AndroidManifest.xml`
- Drawables:
  - `app/src/main/res/drawable/`
- UI audit/research document:
  - `docs/ui-audit/UI_AUDIT_AND_RESEARCH_2026-04-01.md`
- Store screenshot references folder:
  - `screeshots/` (folder name is currently misspelled in repo)

## 6) Generated artifacts currently available

- Debug APK:
  - `app/build/outputs/apk/debug/app-debug.apk`
- Release APK:
  - `app/build/outputs/apk/release/app-release.apk`
- Release AAB:
  - `app/build/outputs/bundle/release/app-release.aab`
- Local upload key materials:
  - `keystore/upload-key.jks`
  - `keystore/upload_certificate.pem`

Note: keep keystore secrets out of git and chat logs.

## 7) Useful commands

Build debug:

```powershell
$env:GRADLE_OPTS='-Xmx2048m'
.\gradlew.bat assembleDebug --no-daemon
```

Build release APK:

```powershell
.\gradlew.bat assembleRelease --no-daemon
```

Build release AAB:

```powershell
.\gradlew.bat bundleRelease --no-daemon
```

Install debug on connected device:

```powershell
adb devices
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

## 8) Git working tree status at handoff

- Modified:
  - `app/build.gradle.kts`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/com/hadify/NumberMerge2048/MainActivity.kt`
- Untracked:
  - `app/src/main/res/drawable/`
  - `docs/`

## 9) Recommended next tasks in new chat

1. Finalize UI polish + animation pass.
2. Optionally migrate remaining core/domain text (challenge/store definitions and runtime info messages) into resources.
3. Prepare signed release AAB with final upload key credentials and bump `versionCode`.

## 10) Latest verification snapshot (2026-04-01)

- Build-level validation completed successfully:
  - `./gradlew :app:compileDebugKotlin`
  - `./gradlew :app:assembleDebug`
  - `./gradlew :app:testDebugUnitTest`
- Debug APK generated at:
  - `app/build/outputs/apk/debug/app-debug.apk`
- Runtime smoke test validated on connected physical device:
  - `adb devices` detected device in `device` state
  - debug package validated as `com.hadify.NumberMerge2048.dev` (debug `applicationIdSuffix`)
  - package had to be enabled for active user with `pm install-existing --user 0 com.hadify.NumberMerge2048.dev` before launch commands
  - `adb install -r app/build/outputs/apk/debug/app-debug.apk` returned `Success`
  - app launched and tested via package `com.hadify.NumberMerge2048.dev`
  - 300 in-app monkey events completed without app crash
  - `adb logcat -d AndroidRuntime:E ActivityManager:E *:S` returned no fatal runtime output
  - app process remained alive and resumed on `com.hadify.NumberMerge2048.dev/com.hadify.NumberMerge2048.MainActivity`
- Note:
  - a MIUI `theme_config` `FileNotFoundException` appeared during monkey execution; this came from monkey/device theme internals and did not crash the app

- Corrected rerun (after package mismatch report) completed:
  - explicit launch succeeded: `com.hadify.NumberMerge2048.dev/com.hadify.NumberMerge2048.MainActivity`
  - 300 monkey events executed against package `com.hadify.NumberMerge2048.dev`
  - no AndroidRuntime/ActivityManager fatal crash output was observed
  - app process remained alive and resumed on the same `.dev` MainActivity

- Deterministic path-based smoke test completed on physical device:
  - exact route validated: Home -> New Game -> Start New Game -> Game -> Store -> Home -> Daily Challenges -> Home -> How To Play
  - each transition was asserted from live UI dump text (not random monkey only)
  - app remained alive (`pidof com.hadify.NumberMerge2048.dev` returned active process)
  - resumed activity stayed on `com.hadify.NumberMerge2048.dev/com.hadify.NumberMerge2048.MainActivity`
  - no fatal `AndroidRuntime` / `ActivityManager` crash output observed after the route

- Store logic and UX improvements completed (2026-04-01):
  - removed the free coin exploit offer and replaced it with a priced utility offer (`Tune-Up Kit`, 22 coins, +1 Swap, +1 Undo)
  - added coordinator-side guardrails:
    - reject invalid/empty offers
    - require active run for charge-based items
    - block charge purchases when active run is already game-over
    - improve purchase feedback message with clear cost/net coin impact
  - improved store UI state handling:
    - explicit message for run-ended state
    - card-level warning text for run-ended charge purchases
    - top-positioned status/result panel so purchase feedback is visible immediately
  - runtime verification on physical device passed:
    - no-run state: `Start Run` CTA shown and charge offers blocked
    - active-run state: `Buy 22` shown for `Tune-Up Kit`
    - purchase succeeded and wallet changed from 97 to 75
    - purchase feedback text visible immediately after buying

- UI audit + de-verbosity pass completed (2026-04-01):
  - audited high-density copy areas in Home, Board Setup, Daily Challenges, How To Play, Store, and Game challenge panel
  - shortened primary user-facing strings to reduce reading load and improve scan speed
  - added compact challenge metadata string (`Chain: N stages`) to replace long chain titles in card view
  - added line clamps/ellipsis on secondary text in Home action cards, Daily Challenges cards, Store cards, and Game challenge details
  - runtime sanity check confirmed updated concise copy is visible on device (e.g., Home "Today" card + shorter action subtitles)

