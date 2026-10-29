$stringsPath = Join-Path $PSScriptRoot "../app/src/debug/res/values/strings.xml"

$gitOutput = git branch --show-current
$currentBranchName = $gitOutput.Trim()
[xml]$xml = Get-Content -Path $stringsPath -Encoding UTF8

foreach ($node in $xml.resources.string) {
    if ($node.InnerText -and $node.InnerText.Contains("[branch]")) {
        $node.InnerText = $node.InnerText.Replace("[branch]", $currentBranchName)
    }
}

$settings = New-Object System.Xml.XmlWriterSettings
$settings.Encoding = New-Object System.Text.UTF8Encoding($false)
$settings.Indent = $true
$settings.NewLineChars = "`n"

$writer = [System.Xml.XmlWriter]::Create($stringsPath, $settings)
$xml.Save($writer)
$writer.Close()
