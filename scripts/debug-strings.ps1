$stringsPath = Join-Path $PSScriptRoot "../app/src/debug/res/values/strings.xml"

[xml]$xml = Get-Content -Path $stringsPath -Encoding UTF8
$currentBranchName = (git branch --show-current).Trim()

$stringNodes = $xml.resources.string

foreach ($node in $stringNodes) {
    if ($node.InnerText -like "*<branch>*") {
        $node.InnerText = $node.InnerText -replace "<branch>", $currentBranchName
    }
}

$settings = New-Object System.Xml.XmlWriterSettings
$settings.Encoding = New-Object System.Text.UTF8Encoding($false)
$settings.Indent = $true

$writer = [System.Xml.XmlWriter]::Create($stringsPath, $settings)
$xml.Save($writer)
$writer.Close()