# NumberMerge2048 Brand Kit

This folder contains the generated Google Play visual assets and app launcher icons.

## Generated files

- `play_icon_512.png`
  - Upload in Play Console as **App icon**.
  - Spec: 512x512 PNG.

- `feature_graphic_1024x500.png`
  - Upload in Play Console as **Feature graphic**.
  - Spec: 1024x500 PNG/JPG.

## Launcher icons in app resources

The script also writes launcher icons to:

- `app/src/main/res/mipmap-mdpi/ic_launcher.png`
- `app/src/main/res/mipmap-hdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- round variants: `ic_launcher_round.png` in the same folders.

## Regenerate assets

From project root:

```powershell
.\tools\generate_brand_assets.ps1
```
