# Build and package the project into TaihangDefender.jar, attempting to fetch a portable CJK font first.
# Usage: .\scripts\build-and-package.ps1

$ErrorActionPreference = 'Stop'
$root = Get-Location
Write-Output "Working dir: $root"

# 1) Try to fetch font (non-fatal)
Write-Output "Attempting to fetch bundled font (if missing) ..."
try {
    & powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\fetch-font.ps1
    Write-Output "fetch-font finished"
} catch {
    Write-Warning "fetch-font failed (continuing without embedded font): $_"
}

# 2) compile sources
$out = Join-Path $root 'out'
if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Path $out | Out-Null

Write-Output "Compiling Java sources..."
$files = Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d $out $files

# 3) copy resources (recursive copy to include subdirectories like pests/)
if (Test-Path resources) {
    Write-Output "Copying resources/ -> out/"
    Copy-Item -Path resources -Destination $out -Recurse -Force
} else {
    Write-Warning "No resources/ directory found, skipping copy"
}

# 4) package jar
$jarName = "TaihangDefender.jar"
if (Test-Path $jarName) { Remove-Item $jarName -Force }
Write-Output "Creating jar $jarName (may take a moment) ..."
Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.IO.Compression
$manifestText = Get-Content -Path .\manifest.txt -Raw
$jarFullPath = Join-Path $root $jarName
$zip = [System.IO.Compression.ZipFile]::Open($jarFullPath, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    $manifestEntry = $zip.CreateEntry('META-INF/MANIFEST.MF')
    $manifestStream = $manifestEntry.Open()
    try {
        $manifestBytes = [System.Text.Encoding]::UTF8.GetBytes(($manifestText.TrimEnd() + "`r`n"))
        $manifestStream.Write($manifestBytes, 0, $manifestBytes.Length)
    } finally {
        $manifestStream.Dispose()
    }

    Get-ChildItem -Path $out -Recurse -File | ForEach-Object {
        $relative = $_.FullName.Substring($out.Length + 1).Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $_.FullName, $relative) | Out-Null
    }
} finally {
    $zip.Dispose()
}

# 5) report jar contents, check for embedded fonts
Write-Output "Jar created: $jarName"
Write-Output "Checking for pests images in jar:"
$jarEntries = [System.IO.Compression.ZipFile]::OpenRead($jarFullPath).Entries
$pestMatches = $jarEntries | Where-Object { $_.FullName -like '*pests*' }
if ($pestMatches) {
    Write-Output "Found pests resources in jar:"
    $pestMatches | ForEach-Object { Write-Output "  $($_.FullName)" }
} else {
    Write-Warning "No pests images found in jar!"
}

Write-Output "Listing fonts inside jar (resources/fonts):"
$fontMatches = $jarEntries | Where-Object { $_.FullName -like 'resources/fonts/*' }
if ($fontMatches) {
    Write-Output "Found embedded font files:"
    $fontMatches | ForEach-Object { Write-Output $_.FullName }
} else {
    Write-Warning "No embedded font files found in jar. The jar may still work if the target system has Chinese fonts."
}

# 6) run smoke test from the jar
Write-Output "Running SmokeTest from the jar to sanity check..."
try {
    java -cp $jarName com.hbau.taihang.SmokeTest
    Write-Output "SmokeTest ran successfully from jar."
} catch {
    Write-Warning "SmokeTest failed when run from jar: $_"
}

Write-Output "Build-and-package done. Output jar: $jarName"
