param(
    [string]$ProjectRoot = ".",
    [string]$OutFolder = "screenshot"
)

$ErrorActionPreference = "Stop"

Set-Location (Resolve-Path $ProjectRoot)

$sdkLine = Select-String -Path "local.properties" -Pattern "^sdk.dir=" | Select-Object -First 1
if (-not $sdkLine) {
    throw "sdk.dir not found in local.properties"
}

$sdk = $sdkLine.Line.Split("=")[1].Replace("/", "\")
$adb = Join-Path $sdk "platform-tools\adb.exe"

$device = & $adb devices | Select-String "\tdevice$"
if (-not $device) {
    throw "No connected Android device in device state."
}

$outDir = Join-Path (Get-Location) $OutFolder
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$tmpUi = Join-Path (Get-Location) "tmp_ui_dump.xml"

function Save-Shot([string]$name) {
    $remote = "/sdcard/Pictures/$name"
    & $adb shell screencap -p $remote | Out-Null
    & $adb pull $remote (Join-Path $outDir $name) | Out-Null
    & $adb shell rm $remote | Out-Null
}

function Get-CenterFromBounds([string]$bounds) {
    if ($bounds -match "^\[(\d+),(\d+)\]\[(\d+),(\d+)\]$") {
        $x1 = [int]$matches[1]
        $y1 = [int]$matches[2]
        $x2 = [int]$matches[3]
        $y2 = [int]$matches[4]
        return @([int](($x1 + $x2) / 2), [int](($y1 + $y2) / 2))
    }
    return $null
}

function Tap-NodeByText([string]$target) {
    & $adb shell uiautomator dump /sdcard/window_dump.xml | Out-Null
    & $adb pull /sdcard/window_dump.xml $tmpUi | Out-Null
    [xml]$xml = Get-Content $tmpUi
    $nodes = @($xml.SelectNodes("//node"))

    $node = $nodes | Where-Object { $_.text -eq $target } | Select-Object -First 1
    if (-not $node) { $node = $nodes | Where-Object { $_."content-desc" -eq $target } | Select-Object -First 1 }
    if (-not $node) { $node = $nodes | Where-Object { $_.text -like "*$target*" } | Select-Object -First 1 }
    if (-not $node) { $node = $nodes | Where-Object { $_."content-desc" -like "*$target*" } | Select-Object -First 1 }
    if (-not $node) { return $false }

    $center = Get-CenterFromBounds $node.bounds
    if (-not $center) { return $false }
    & $adb shell input tap $center[0] $center[1] | Out-Null
    Start-Sleep -Milliseconds 900
    return $true
}

function Go-Back([int]$times = 1) {
    for ($i = 0; $i -lt $times; $i++) {
        & $adb shell input keyevent 4 | Out-Null
        Start-Sleep -Milliseconds 700
    }
}

# Launch app
& $adb shell am start -n com.hadify.NumberMerge2048.dev/com.hadify.NumberMerge2048.MainActivity | Out-Null
Start-Sleep -Seconds 2

Save-Shot "01_home.png"

if (Tap-NodeByText "New") {
    Start-Sleep -Milliseconds 900
    Save-Shot "02_new_game_setup.png"

    if (Tap-NodeByText "Start") {
        Start-Sleep -Milliseconds 1200
        Save-Shot "03_gameplay.png"

        if (Tap-NodeByText "Store") {
            Start-Sleep -Milliseconds 1000
            Save-Shot "04_store.png"
            Go-Back 1
            Start-Sleep -Milliseconds 900
        }

        if (-not (Tap-NodeByText "Home")) {
            Go-Back 1
        }
        Start-Sleep -Milliseconds 1200
    }
}

& $adb shell am start -n com.hadify.NumberMerge2048.dev/com.hadify.NumberMerge2048.MainActivity | Out-Null
Start-Sleep -Seconds 2

if (Tap-NodeByText "Challenges") {
    Start-Sleep -Milliseconds 1000
    Save-Shot "05_daily_challenges.png"
    Go-Back 1
    Start-Sleep -Milliseconds 900
}

if (Tap-NodeByText "How") {
    Start-Sleep -Milliseconds 1000
    Save-Shot "06_how_to_play.png"
    Go-Back 1
    Start-Sleep -Milliseconds 900
}

Save-Shot "07_home_final.png"

if (Test-Path $tmpUi) {
    Remove-Item $tmpUi -Force
}

Get-ChildItem -File $outDir | Select-Object Name, Length, LastWriteTime
