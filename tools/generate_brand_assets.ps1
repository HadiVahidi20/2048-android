param(
    [string]$ProjectRoot = "."
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

function Get-Color([string]$hex) {
    return [System.Drawing.ColorTranslator]::FromHtml($hex)
}

function New-RoundedRectPath(
    [float]$x,
    [float]$y,
    [float]$width,
    [float]$height,
    [float]$radius
) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $radius * 2
    $arc = New-Object System.Drawing.RectangleF($x, $y, $diameter, $diameter)

    $path.AddArc($arc, 180, 90)
    $arc.X = $x + $width - $diameter
    $path.AddArc($arc, 270, 90)
    $arc.Y = $y + $height - $diameter
    $path.AddArc($arc, 0, 90)
    $arc.X = $x
    $path.AddArc($arc, 90, 90)
    $path.CloseFigure()

    return $path
}

function Save-ScaledIcon(
    [System.Drawing.Bitmap]$source,
    [int]$size,
    [string]$path,
    [bool]$roundMask
) {
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)

    if ($roundMask) {
        $clipPath = New-Object System.Drawing.Drawing2D.GraphicsPath
        $clipPath.AddEllipse(0, 0, $size, $size)
        $g.SetClip($clipPath)
    }

    $g.DrawImage($source, 0, 0, $size, $size)
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)

    $g.Dispose()
    $bmp.Dispose()
}

$root = (Resolve-Path $ProjectRoot).Path
$assetsRoot = Join-Path $root "play-assets\brand-kit"
$resRoot = Join-Path $root "app\src\main\res"

New-Item -ItemType Directory -Force -Path $assetsRoot | Out-Null

$iconSize = 512
$icon = New-Object System.Drawing.Bitmap($iconSize, $iconSize)
$ig = [System.Drawing.Graphics]::FromImage($icon)
$ig.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$ig.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$ig.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$ig.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
$ig.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit
$ig.Clear([System.Drawing.Color]::Transparent)

$outerPath = New-RoundedRectPath -x 16 -y 16 -width 480 -height 480 -radius 104
$mainGradient = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.PointF(0, 0)),
    (New-Object System.Drawing.PointF(512, 512)),
    (Get-Color "#8D5930"),
    (Get-Color "#5F3B95")
)
$blend = New-Object System.Drawing.Drawing2D.ColorBlend
$blend.Positions = [float[]](0.0, 0.52, 1.0)
$blend.Colors = [System.Drawing.Color[]]@(
    (Get-Color "#9E6A34"),
    (Get-Color "#365DAF"),
    (Get-Color "#6F40A5")
)
$mainGradient.InterpolationColors = $blend
$ig.FillPath($mainGradient, $outerPath)

$borderPen = New-Object System.Drawing.Pen((Get-Color "#F2B24B"), 8)
$ig.DrawPath($borderPen, $outerPath)

$shinePath = New-RoundedRectPath -x 38 -y 38 -width 436 -height 200 -radius 70
$shineBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.PointF(0, 0)),
    (New-Object System.Drawing.PointF(0, 220)),
    [System.Drawing.Color]::FromArgb(72, 255, 255, 255),
    [System.Drawing.Color]::FromArgb(0, 255, 255, 255)
)
$ig.FillPath($shineBrush, $shinePath)

$tilePath = New-RoundedRectPath -x 132 -y 150 -width 248 -height 248 -radius 48
$tileBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.PointF(132, 150)),
    (New-Object System.Drawing.PointF(380, 398)),
    (Get-Color "#F8D77A"),
    (Get-Color "#E0B455")
)
$ig.FillPath($tileBrush, $tilePath)
$tileBorder = New-Object System.Drawing.Pen((Get-Color "#FFE7AA"), 4)
$ig.DrawPath($tileBorder, $tilePath)

$txtRect = New-Object System.Drawing.RectangleF(132, 150, 248, 248)
$sf = New-Object System.Drawing.StringFormat
$sf.Alignment = [System.Drawing.StringAlignment]::Center
$sf.LineAlignment = [System.Drawing.StringAlignment]::Center
$fontMain = New-Object System.Drawing.Font("Segoe UI", 74, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$mainTextBrush = New-Object System.Drawing.SolidBrush((Get-Color "#F7F5EF"))
$ig.DrawString("2048", $fontMain, $mainTextBrush, $txtRect, $sf)

$chipBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(34, 22, 45, 80))
$chipTextBrush = New-Object System.Drawing.SolidBrush((Get-Color "#FFF8E8"))
$chipFont = New-Object System.Drawing.Font("Segoe UI", 28, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)

$chipRects = @(
    @{ R = (New-Object System.Drawing.RectangleF(76, 80, 80, 58)); T = "2" },
    @{ R = (New-Object System.Drawing.RectangleF(356, 92, 80, 58)); T = "4" },
    @{ R = (New-Object System.Drawing.RectangleF(82, 382, 80, 58)); T = "8" },
    @{ R = (New-Object System.Drawing.RectangleF(350, 370, 90, 62)); T = "16" }
)

foreach ($chip in $chipRects) {
    $chipPath = New-RoundedRectPath -x $chip.R.X -y $chip.R.Y -width $chip.R.Width -height $chip.R.Height -radius 16
    $ig.FillPath($chipBrush, $chipPath)
    $ig.DrawString($chip.T, $chipFont, $chipTextBrush, $chip.R, $sf)
    $chipPath.Dispose()
}

$iconPath = Join-Path $assetsRoot "play_icon_512.png"
$icon.Save($iconPath, [System.Drawing.Imaging.ImageFormat]::Png)

$densities = @(
    @{ Folder = "mipmap-mdpi"; Size = 48 },
    @{ Folder = "mipmap-hdpi"; Size = 72 },
    @{ Folder = "mipmap-xhdpi"; Size = 96 },
    @{ Folder = "mipmap-xxhdpi"; Size = 144 },
    @{ Folder = "mipmap-xxxhdpi"; Size = 192 }
)

foreach ($d in $densities) {
    $dir = Join-Path $resRoot $d.Folder
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    Save-ScaledIcon -source $icon -size $d.Size -path (Join-Path $dir "ic_launcher.png") -roundMask $false
    Save-ScaledIcon -source $icon -size $d.Size -path (Join-Path $dir "ic_launcher_round.png") -roundMask $true
}

$feature = New-Object System.Drawing.Bitmap(1024, 500)
$fg = [System.Drawing.Graphics]::FromImage($feature)
$fg.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$fg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$fg.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$fg.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
$fg.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit

$bgGradient = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.PointF(0, 0)),
    (New-Object System.Drawing.PointF(1024, 500)),
    (Get-Color "#1D1E32"),
    (Get-Color "#3B2762")
)
$fg.FillRectangle($bgGradient, 0, 0, 1024, 500)

$leftGlow = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    (New-Object System.Drawing.PointF(0, 0)),
    (New-Object System.Drawing.PointF(350, 500)),
    [System.Drawing.Color]::FromArgb(120, 255, 169, 64),
    [System.Drawing.Color]::FromArgb(10, 255, 169, 64)
)
$fg.FillEllipse($leftGlow, -100, -120, 650, 650)

$fg.DrawImage($icon, 86, 74, 260, 260)

$titleFont = New-Object System.Drawing.Font("Segoe UI", 64, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$subtitleFont = New-Object System.Drawing.Font("Segoe UI", 30, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$metaFont = New-Object System.Drawing.Font("Segoe UI", 24, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$titleBrush = New-Object System.Drawing.SolidBrush((Get-Color "#FFF8E8"))
$subBrush = New-Object System.Drawing.SolidBrush((Get-Color "#D9D5F2"))
$metaBrush = New-Object System.Drawing.SolidBrush((Get-Color "#F4B44F"))

$fg.DrawString("NumberMerge2048", $titleFont, $titleBrush, 390, 120)
$fg.DrawString("Swipe, merge, and build the perfect run.", $subtitleFont, $subBrush, 392, 206)
$fg.DrawString("Daily challenges | Power-ups | Smooth gameplay", $metaFont, $metaBrush, 394, 258)

$tilePanelPath = New-RoundedRectPath -x 384 -y 310 -width 560 -height 120 -radius 24
$tilePanelBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(40, 255, 255, 255))
$fg.FillPath($tilePanelBrush, $tilePanelPath)

$miniTileColors = @("#EEE4DA", "#EEC67B", "#E6A55C", "#D88447")
for ($i = 0; $i -lt 4; $i++) {
    $x = 430 + ($i * 128)
    $tile = New-RoundedRectPath -x $x -y 330 -width 96 -height 80 -radius 16
    $tileBrush = New-Object System.Drawing.SolidBrush((Get-Color $miniTileColors[$i]))
    $fg.FillPath($tileBrush, $tile)
    $tileTextFont = New-Object System.Drawing.Font("Segoe UI", 28, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
    $tileTextBrush = New-Object System.Drawing.SolidBrush((Get-Color "#3A2A1E"))
    $tileTextRect = New-Object System.Drawing.RectangleF($x, 330, 96, 80)
    $fg.DrawString([math]::Pow(2, $i + 1).ToString(), $tileTextFont, $tileTextBrush, $tileTextRect, $sf)
    $tile.Dispose()
    $tileBrush.Dispose()
    $tileTextFont.Dispose()
    $tileTextBrush.Dispose()
}

$featurePath = Join-Path $assetsRoot "feature_graphic_1024x500.png"
$feature.Save($featurePath, [System.Drawing.Imaging.ImageFormat]::Png)

$tilePanelPath.Dispose()
$tilePanelBrush.Dispose()
$leftGlow.Dispose()
$bgGradient.Dispose()
$titleFont.Dispose()
$subtitleFont.Dispose()
$metaFont.Dispose()
$titleBrush.Dispose()
$subBrush.Dispose()
$metaBrush.Dispose()
$fg.Dispose()
$feature.Dispose()

$chipBrush.Dispose()
$chipTextBrush.Dispose()
$chipFont.Dispose()
$sf.Dispose()
$fontMain.Dispose()
$mainTextBrush.Dispose()
$tileBorder.Dispose()
$tileBrush.Dispose()
$shineBrush.Dispose()
$borderPen.Dispose()
$mainGradient.Dispose()
$shinePath.Dispose()
$tilePath.Dispose()
$outerPath.Dispose()
$ig.Dispose()
$icon.Dispose()

Write-Output "Generated brand assets in: $assetsRoot"
