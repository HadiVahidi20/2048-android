# NumberMerge2048 (Rebuild)

Minimal Android rebuild for package `com.hadify.NumberMerge2048` to unblock urgent Google Play updates.

## Current scope

- Playable 4x4 2048 gameplay
- Swipe controls (up/down/left/right)
- Score + best score (session)
- New game / restart
- Win and game-over states
- Target SDK 35

## Tech stack

- Native Android (Kotlin)
- Jetpack Compose UI
- Android Gradle Plugin 8.5.2
- Gradle Wrapper 8.7

## Build

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
.\gradlew.bat :app:bundleRelease
```

Artifacts:

- AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK: `app/build/outputs/apk/release/app-release.apk`

## Signing setup

1. Copy `keystore.properties.example` to `keystore.properties`.
2. Fill in your keystore path and passwords.
3. Build release artifacts.

`keystore.properties` is ignored by git.

## Upload key reset flow (Play Console)

1. Generate upload key + export certificate (`.pem`).
2. In Play Console App Integrity, request upload key reset with new cert.
3. Wait for Google approval email.
4. Upload the new signed AAB to internal testing first.
5. Promote to production after confirmation.

## Notes

- Package/application ID is fixed to `com.hadify.NumberMerge2048`.
- Increase `versionCode` in `app/build.gradle.kts` for each new upload.
