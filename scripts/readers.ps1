$readerImgDir = Join-Path $PSScriptRoot "../app/src/main/res/mipmap"
$readersJson  = Join-Path $PSScriptRoot "../app/src/main/assets/readers.json"

$existingItems = @()
$existingIds = @()
$newReaders = @()

# read current data
$jsonContent = Get-Content -Path $readersJson -Raw -Encoding utf8
if (-not [string]::IsNullOrWhiteSpace($jsonContent)) {
	$existingItems = $jsonContent | ConvertFrom-Json
	$existingIds = $existingItems | ForEach-Object { $_.id }
}

# generate new items
Get-ChildItem -Path $readerImgDir -File -Filter "*.jpg" | ForEach-Object {
	$id = $_.BaseName

	if ($existingIds -contains $id) {
		return
	}

	$newReaders += [PSCustomObject]@{
		id          = $id
		name 		= "???"
		description	= "???"
	}
}

# add new items to the existed ones
$allReaders = @($existingItems) + @($newReaders)
$sortedData = $allReaders | Sort-Object -Property { $_.id }

# save file
$sortedData | ConvertTo-Json -Depth 2 | Out-File -FilePath $readersJson -Encoding utf8

# replace \u0027 with '
(Get-Content $readersJson -Raw -Encoding utf8) `
    -replace '\\u0027', "'" |
    Set-Content $readersJson -Encoding utf8