# Script de instalación de temas IGN para Karoo
# KarooTopoMaps — github.com/DAVOE75/KarooTopoMaps
# Uso: .\scripts\install_themes.ps1

Write-Host "=================================" -ForegroundColor Cyan
Write-Host " KarooTopoMaps — Instalador IGN  " -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Verificar ADB
Write-Host "Buscando ADB..." -ForegroundColor Yellow
try {
    $adbVersion = adb version 2>&1
    Write-Host "ADB encontrado: OK" -ForegroundColor Green
} catch {
    Write-Host "ERROR: ADB no encontrado. Instala Android SDK Platform Tools." -ForegroundColor Red
    Write-Host "Descarga: https://developer.android.com/studio/releases/platform-tools" -ForegroundColor Yellow
    exit 1
}

# Verificar dispositivo conectado
Write-Host ""
Write-Host "Buscando Karoo conectado..." -ForegroundColor Yellow
$devices = adb devices 2>&1
if ($devices -notmatch "device$") {
    Write-Host "ERROR: No se detecta ningún Karoo. Comprueba:" -ForegroundColor Red
    Write-Host "  1. Cable USB conectado" -ForegroundColor Yellow
    Write-Host "  2. Depuración USB activada en el Karoo" -ForegroundColor Yellow
    Write-Host "     (Ajustes > Sistema > Acerca del dispositivo > Pulsa 7 veces Numero de compilacion)" -ForegroundColor Yellow
    exit 1
}
Write-Host "Karoo detectado: OK" -ForegroundColor Green

# Determinar carpeta de destino según la versión de OsmAnd
$destDirs = @(
    "/sdcard/Android/data/net.osmand/files/rendering/",
    "/sdcard/Android/data/net.osmand.plus/files/rendering/",
    "/sdcard/osmand/rendering/"
)

Write-Host ""
Write-Host "Instalando temas..." -ForegroundColor Yellow
$themesDir = Join-Path $PSScriptRoot "..\themes"
$themes = Get-ChildItem -Path $themesDir -Filter "*.xml"

if ($themes.Count -eq 0) {
    Write-Host "No se encontraron temas en la carpeta 'themes/'." -ForegroundColor Red
    exit 1
}

foreach ($theme in $themes) {
    Write-Host "  → $($theme.Name)" -ForegroundColor White
    foreach ($dest in $destDirs) {
        adb shell "mkdir -p $dest" 2>&1 | Out-Null
        $result = adb push $theme.FullName "$dest$($theme.Name)" 2>&1
    }
    if ($LASTEXITCODE -eq 0) {
        Write-Host "    ✓ Instalado correctamente" -ForegroundColor Green
    } else {
        Write-Host "    ✗ Error al copiar" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=================================" -ForegroundColor Cyan
Write-Host " Instalación completada!         " -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Siguiente paso en tu Karoo:" -ForegroundColor Yellow
Write-Host "  OsmAnd > Configurar mapa > Estilo de mapa > IGN-España" -ForegroundColor White
Write-Host ""
