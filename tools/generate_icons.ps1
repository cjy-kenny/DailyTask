Add-Type -AssemblyName System.Drawing
$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$resDir = Join-Path $projectRoot 'app\src\main\res'

function New-RoundedRectPath {
    param(
        [single]$X,
        [single]$Y,
        [single]$Width,
        [single]$Height,
        [single]$Radius
    )
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $diameter = $Radius * 2
    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

function Draw-Icon {
    param([bool]$Round)

    $bitmap = [System.Drawing.Bitmap]::new(512, 512)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.Color]::Transparent)

    $rect = [System.Drawing.RectangleF]::new(0, 0, 512, 512)
    $startColor = [System.Drawing.Color]::FromArgb(255, 91, 155, 255)
    $endColor = [System.Drawing.Color]::FromArgb(255, 36, 87, 197)
    $backgroundBrush = [System.Drawing.Drawing2D.LinearGradientBrush]::new($rect, $startColor, $endColor, [single]45)

    if ($Round) {
        $graphics.FillEllipse($backgroundBrush, 0, 0, 512, 512)
    } else {
        $backgroundPath = New-RoundedRectPath -X 0 -Y 0 -Width 512 -Height 512 -Radius 108
        $graphics.FillPath($backgroundBrush, $backgroundPath)
        $backgroundPath.Dispose()
    }

    # 卡片投影
    $shadowBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(32, 4, 20, 50))
    $shadowPath = New-RoundedRectPath -X 140 -Y 112 -Width 248 -Height 300 -Radius 42
    $graphics.FillPath($shadowBrush, $shadowPath)

    # 白色任务卡片
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
    $cardPath = New-RoundedRectPath -X 132 -Y 100 -Width 248 -Height 300 -Radius 42
    $graphics.FillPath($cardBrush, $cardPath)

    # 三条任务行
    $lineBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 165, 194, 238))
    for ($i = 0; $i -lt 3; $i++) {
        $linePath = New-RoundedRectPath -X 196 -Y (166 + $i * 56) -Width 130 -Height 18 -Radius 9
        $graphics.FillPath($lineBrush, $linePath)
        $linePath.Dispose()
    }

    # 绿色完成徽标
    $badgeBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255, 34, 160, 107))
    $graphics.FillEllipse($badgeBrush, 262, 278, 164, 164)

    $checkPen = [System.Drawing.Pen]::new([System.Drawing.Color]::White, [single]17)
    $checkPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $checkPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $graphics.DrawLine($checkPen, 310, 362, 336, 388)
    $graphics.DrawLine($checkPen, 336, 388, 382, 334)

    return $bitmap
}

$variants = @(
    @{ Folder = 'mipmap-mdpi'; Size = 48 },
    @{ Folder = 'mipmap-hdpi'; Size = 72 },
    @{ Folder = 'mipmap-xhdpi'; Size = 96 },
    @{ Folder = 'mipmap-xxhdpi'; Size = 144 },
    @{ Folder = 'mipmap-xxxhdpi'; Size = 192 }
)

$squareIcon = Draw-Icon -Round $false
$roundIcon = Draw-Icon -Round $true

foreach ($variant in $variants) {
    $targetDir = Join-Path $resDir $variant.Folder
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

    foreach ($pair in @(
        @{ Source = $squareIcon; Name = 'ic_launcher.png' },
        @{ Source = $roundIcon; Name = 'ic_launcher_round.png' }
    )) {
        $targetSize = $variant.Size
        $scaled = [System.Drawing.Bitmap]::new($targetSize, $targetSize)
        $scaledGraphics = [System.Drawing.Graphics]::FromImage($scaled)
        $scaledGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $scaledGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $scaledGraphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $scaledGraphics.DrawImage($pair.Source, 0, 0, $targetSize, $targetSize)
        $scaledGraphics.Dispose()

        $outputPath = Join-Path $targetDir $pair.Name
        $scaled.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $scaled.Dispose()
        Write-Output "Generated $outputPath"
    }
}

$squareIcon.Dispose()
$roundIcon.Dispose()
