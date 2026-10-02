$coloringsDir = Join-Path $PSScriptRoot "../colorings"
$talesDir     = Join-Path $PSScriptRoot "../tales"
$talesInfo    = Join-Path $PSScriptRoot "../app/src/main/assets/tales.json"
$readersJson  = Join-Path $PSScriptRoot "../app/src/main/assets/readers.json"
$ffprobe      = "C:/Program Files/ffmpeg/bin/ffprobe.exe"

$existingTales = @()
$existingIds = @()
$coloringIds = @()

# get colorings data
$coloringIds = Get-ChildItem -Path $coloringsDir -File | 
	Where-Object { $_.Extension -match '^\.jpe?g$' } | 
	ForEach-Object {
		$id = 0
		if ([int]::TryParse($_.BaseName, [ref]$id)) {
			$id
		}
	}

# read current talesInfo
$jsonContent = Get-Content -Path $talesInfo -Raw -Encoding utf8
$existingTales = $jsonContent | ConvertFrom-Json
$existingIds = $existingTales | ForEach-Object { [int]$_.id }

# generate new items
$newTales = @()
Get-ChildItem -Path $talesDir -File -Filter "*.mp3" | ForEach-Object {
	$filePath = $_.FullName
	$fileName = $_.Name
	$id = [int]($_.BaseName)

	if ($existingIds -contains $id) {
		return
	}

	$durationSeconds = & $ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$filePath" 2>$null
	$seconds = [math]::Truncate([double]$durationSeconds)
	$ts = [timespan]::FromSeconds($seconds)
	$formattedDuration = "{0:D2}:{1:D2}" -f [int]$ts.TotalMinutes, $ts.Seconds

	$hasColoring = $coloringIds -contains $id

	$newTales += [PSCustomObject]@{
		id          = $id
		title 		= "???"
		reader		= "???"
		duration    = $formattedDuration
		intro		= 0
		hasColoring = $hasColoring
	}
}

# check reader's name is correct
$readersJsonContent = Get-Content -Path $readersJson -Raw -Encoding utf8
$existingReaders = $readersJsonContent | ConvertFrom-Json
$existingNames = $existingReaders | ForEach-Object { $_.name }

foreach ($tale in $existingTales) {
    if (-not [string]::IsNullOrWhiteSpace($tale.reader)) {
        if ($tale.reader.StartsWith("???")) {
            continue
        }
        
        if ($existingNames -notcontains $tale.reader) {
            $tale.reader = "??? " + $tale.reader
        }
    }
}

# add new items to the existed ones
$allTales = @($existingTales) + @($newTales)
$sortedTales = $allTales | Sort-Object -Property { [int]$_.id }

# save file
$sortedTales | ConvertTo-Json -Depth 2 | Out-File -FilePath $talesInfo -Encoding utf8

# replace \u0027 with '
(Get-Content $talesInfo -Raw -Encoding utf8) `
    -replace '\\u0027', "'" |
    Set-Content $talesInfo -Encoding utf8