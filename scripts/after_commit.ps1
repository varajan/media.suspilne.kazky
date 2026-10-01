$dirTask1 = "./../colorings"
$dirTask2 = "./../tales"

$outputTask1 = "colorings.txt"
$outputTask2 = "tales_info.txt"

# --------------------------------------------------
# Task 1: Get list of colorings
# --------------------------------------------------
if (Test-Path $dirTask1) {
    Get-ChildItem -Path $dirTask1 -File | Select-Object -ExpandProperty Name | Out-File -FilePath $outputTask1 -Encoding utf8
} else {
    Write-Host "folder not found"
}

# --------------------------------------------------
# Task 2: Get tales' duration
# --------------------------------------------------
if (Test-Path $dirTask2) {$audioResults = @()
	$ffprobe = "C:/Program Files/ffmpeg/bin/ffprobe.exe"
	
    Get-ChildItem -Path $dirTask2 -File | ForEach-Object {
        $filePath =$_.FullName
        $fileName =$_.Name

        $durationSeconds = & $ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$filePath" 2>$null

        if ($durationSeconds -and [double]::TryParse($durationSeconds, [ref]$null)) {
            $ts = [timespan]::FromSeconds([double]$durationSeconds)
            $formattedDuration =$ts.ToString("hh`:mm`:ss")
            $audioResults += "$fileName -$formattedDuration"
            $audioResults += "$fileName -$ts"
        } else {
            $audioResults += "$fileName - $durationSeconds"
        }
    }

    $audioResults | Out-File -FilePath $outputTask2 -Encoding utf8
} else {
    Write-Host "Folder $dirTask2 not found"
}