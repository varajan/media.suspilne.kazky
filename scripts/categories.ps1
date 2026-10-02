$talesJson      = Join-Path $PSScriptRoot "../app/src/main/assets/tales.json"
$categoriesJson = Join-Path $PSScriptRoot "../app/src/main/assets/categories.json"

# Read current talesJson and get all IDs
$talesContent = Get-Content -Path $talesJson -Raw -Encoding utf8
$existingIds = ($talesContent | ConvertFrom-Json).id | ForEach-Object { [int]$_ }

# Read current categoriesJson
$categoriesContent = Get-Content -Path $categoriesJson -Raw -Encoding utf8
$categories = $categoriesContent | ConvertFrom-Json

# Extract all IDs currently assigned to any category
$assignedIds = $categories | ForEach-Object { $_.taleIds } | ForEach-Object { [int]$_ }

# Find IDs that exist in tales.json but are missing in categories.json
$lostTaleIds = $existingIds | Where-Object { $_ -notin $assignedIds }

# If there are unassigned IDs, add them under a "???" category
if ($lostTaleIds.Count -gt 0) {
    $categories += [PSCustomObject]@{
        taleIds = @($lostTaleIds)
        title   = "???"
    }
}

# Convert to JSON
$json = $categories | ConvertTo-Json -Depth 5
$pattern = '(?s)\[\r?\n\s*([\d,\s]+?)\r?\n\s*\]'

# Write taleIds in one line
$compactJson = [regex]::Replace($json, $pattern, {
    param($match)
    '[' + ($match.Groups[1].Value -replace '\s+', ' ').Trim() + ']'
})

# Save to file
$compactJson | Out-File -FilePath $categoriesJson -Encoding utf8
