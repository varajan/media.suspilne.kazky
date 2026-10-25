$stringsPath = Join-Path $PSScriptRoot "../app/src/debug/res/values/strings.xml"

# Перевіряємо, чи існує файл взагалі
if (-not (Test-Path $stringsPath)) {
    Write-Error "Файл не знайдено за шляхом: $stringsPath"
    exit
}

# Надійне отримання імені поточної гілки
$gitOutput = git branch --show-current
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($gitOutput)) {
    Write-Error "Не вдалося визначити поточну гілку Git. Переконайтеся, що ви перебуваєте в Git-репозиторії."
    exit
}
$currentBranchName = $gitOutput.Trim()
Write-Host "Поточна гілка Git: $currentBranchName" -ForegroundColor Cyan

# Завантажуємо XML-файл
[xml]$xml = Get-Content -Path $stringsPath -Encoding UTF8

# Перевіряємо, чи є взагалі ноди resources та string
if ($null -eq $xml.resources -or $null -eq $xml.resources.string) {
    Write-Error "У файлі strings.xml не знайдено секцію <resources> або теги <string>."
    exit
}

$updatedCount = 0

# Проходимо по всіх нодах <string>
foreach ($node in $xml.resources.string) {
    # Перевіряємо, чи містить текст маркер <branch> (використовуємо .Contains замість -like для точності)
    if ($node.InnerText -and $node.InnerText.Contains("<branch>")) {
        $node.InnerText = $node.InnerText.Replace("<branch>", $currentBranchName)
        $updatedCount++
    }
}

Write-Host "Оновлено рядків: $updatedCount" -ForegroundColor Yellow

# Зберігаємо зміни у файл без BOM (UTF-8)
$settings = New-Object System.Xml.XmlWriterSettings
$settings.Encoding = New-Object System.Text.UTF8Encoding($false)
$settings.Indent = $true
$settings.NewLineChars = "`n"

$writer = [System.Xml.XmlWriter]::Create($stringsPath, $settings)
$xml.Save($writer)
$writer.Close()

Write-Host "strings.xml успішно збережено!" -ForegroundColor Green