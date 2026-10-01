$coloringsDir = "./../colorings"
$talesDir     = "./../tales"
$talesInfo    = "./../app/src/main/assets/talesInfo.json"
$ffprobe      = "C:/Program Files/ffmpeg/bin/ffprobe.exe"

$coloringIds = @()
if (Test-Path $coloringsDir) {
    $coloringIds = Get-ChildItem -Path $coloringsDir -File | 
        Where-Object { $_.Extension -match '^\.jpe?g$' } | 
        ForEach-Object {
            $id = 0
            if ([int]::TryParse($_.BaseName, [ref]$id)) {
                $id
            }
        }
}

$audioResults = @()

if (Test-Path $talesDir) {
    Get-ChildItem -Path $talesDir -File -Filter "*.mp3" | ForEach-Object {
        $filePath = $_.FullName
        $fileName = $_.Name
        $id = [int]($_.BaseName)

        $durationSeconds = & $ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$filePath" 2>$null
		$seconds = [math]::Truncate([double]$durationSeconds)
		$ts = [timespan]::FromSeconds($seconds)
		$formattedDuration = "{0:D2}:{1:D2}" -f [int]$ts.TotalMinutes, $ts.Seconds

        $hasColoring = $coloringIds -contains $id

        $audioResults += [PSCustomObject]@{
            id          = $id
            duration    = $formattedDuration
            hasColoring = $hasColoring
        }
    }
}

$audioResults | ConvertTo-Json -Depth 2 | Out-File -FilePath $talesInfo -Encoding utf8