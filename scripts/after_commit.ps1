$dirTask1 = "colorings"
$dirTask2 = "tales"

$outputTask1 = "colorings.txt"
$outputTask2 = "tales_info.txt"

# --------------------------------------------------
# Task 1: Get list of colorings
# --------------------------------------------------
if (Test-Path $dirTask1) {
    Get-ChildItem -Path $dirTask1 -File | Select-Object -ExpandProperty Name | Out-File -FilePath $outputTask1 -Encoding utf8
}

# --------------------------------------------------
# Task 2: Get tales' duration
# --------------------------------------------------
if (Test-Path $dirTask2) {
    $shell = New-Object -ComObject Shell.Application
    $folder = $shell.NameSpace((Get-Item $dirTask2).FullName)
    
    $durationIndex = -1
    for ($i = 0; $i -lt 300; $i++) {
        $header = $folder.GetDetailsOf($null, $i)
        if ($header -eq "Duration" -or $header -eq "Тривалість") {
            $durationIndex = $i
            break
        }
    }

    $audioResults = @()

    if ($durationIndex -ne -1) {
        Get-ChildItem -Path $dirTask2 -File | ForEach-Object {
            $item = $folder.ParseName($_.Name)
            $duration = $folder.GetDetailsOf($item, $durationIndex)
            if ([string]::IsNullOrWhiteSpace($duration)) { $duration = "Н/Д" }
            
            $audioResults += "$($_.Name) - $duration"
        }
    }

    $audioResults | Out-File -FilePath $outputTask2 -Encoding utf8
}