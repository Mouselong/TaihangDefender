# Fetch a portable CJK font into resources/fonts/ if missing.
# Usage: .\scripts\fetch-font.ps1

$destDir = Join-Path -Path (Get-Location) -ChildPath 'resources\fonts'
if (-not (Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir | Out-Null }

# Candidate URLs for Noto Sans CJK SC
$urls = @(
    'https://github.com/googlefonts/noto-cjk/raw/main/Sans/OTF/NotoSansCJKsc-Regular.otf',
    'https://github.com/googlefonts/noto-cjk/raw/main/Sans/TTF/NotoSansSC-Regular.ttf'
)

$targets = @('NotoSansCJKsc-Regular.otf','NotoSansSC-Regular.ttf')

for ($i=0; $i -lt $urls.Length; $i++) {
    $url = $urls[$i]
    $file = Join-Path $destDir $targets[$i]
    if (Test-Path $file) {
        Write-Output "Font already exists: $file"
        exit 0
    }
    try {
        Write-Output "Downloading font from $url ..."
        Invoke-WebRequest -Uri $url -OutFile $file -UseBasicParsing -ErrorAction Stop
        if ((Get-Item $file).Length -gt 1024) {
            Write-Output "Downloaded $file (size: $((Get-Item $file).Length) bytes)"
            exit 0
        } else {
            Write-Warning "Downloaded file too small, deleting and trying next."
            Remove-Item $file -Force
        }
    } catch {
        Write-Warning ("Failed to download from {0}: {1}" -f $url, $_.Exception.Message)
        if (Test-Path $file) { Remove-Item $file -Force }
    }
}

Write-Error "Unable to fetch a bundled font. Please download Noto Sans CJK SC manually and place it under resources/fonts/"
exit 1

