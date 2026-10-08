$talesDir    = Join-Path $PSScriptRoot "../tales"
$stringsPath = Join-Path $PSScriptRoot "../app/src/main/res/values/strings.xml"

$folderSizeMB = [math]::Round((Get-ChildItem -Path $talesDir -Recurse -File -ErrorAction SilentlyContinue | 
    Measure-Object -Property Length -Sum).Sum / 1MB)

[xml]$xml = Get-Content -Path $stringsPath -Encoding UTF8

$node = $xml.SelectSingleNode("//string[@name='requiredSpace']")
if ($node) {
    $node.InnerText = $folderSizeMB.ToString()
    $xml.Save($stringsPath)
}
