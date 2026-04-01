# NumberMerge2048 Rebuild - Session Memory

Last updated: 2026-04-01
Project path: `c:\MyDev\Hadify\Andriod Apps\2048-android`
Play package name: `com.hadify.NumberMerge2048`

## 1) Goal and context

This project is a rebuild of the original Play Store app after source loss.
The goal is to keep the same package name and ship updates again.

## 2) Current build/config status

- Tech: Native Android, Kotlin, Jetpack Compose (single activity).
- Main app logic is currently centralized in:
  - `app/src/main/java/com/hadify/NumberMerge2048/MainActivity.kt`
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

## 4) Persistence (important)

### Persisted currently (SharedPreferences JSON, key `state_v2`)

- `coinBalance`
- `bestScore`
- `homeDailyClaimed`
- challenge state:
  - unlocked
  - completed
  - claimed
  - bestProgress
  - statusNote
- day key for daily challenge cycle

### Not fully persisted yet

- Active run full state after app kill/restart (board tiles, frozen tiles, undo stack, exact session resume) is not fully restored from disk.
- Difficulty system (true Easy/Medium/Hard with spawn tuning/power tuning) is not implemented as a dedicated selectable system yet.

## 5) Key files

- Main logic/UI:
  - `app/src/main/java/com/hadify/NumberMerge2048/MainActivity.kt`
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

1. Implement full persistent active-run restore (board/score/moves/power state).
2. Add real selectable difficulty (Easy/Medium/Hard) with gameplay impact.
3. Split `MainActivity.kt` (currently very large) into modules/files:
   - engine
   - coordinator/state
   - screens/components
4. Move user strings to `strings.xml`.
5. Finalize UI polish + animation pass.
6. Prepare signed release AAB with final upload key credentials and bump `versionCode`.

