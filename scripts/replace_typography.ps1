# Material Design 3 Typography Replacement Script
# Replaces hardcoded fontSize, fontWeight patterns with Material 3 typography tokens

Write-Host "🎨 Material Design 3 Typography Migration" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

$projectRoot = "C:\Users\Diganta1\AndroidStudioProjects\VoltAI-SmartBatteryGuardian"
$sourceDirectories = @(
    "$projectRoot\app\src\main\java\com\voltai\smartbatteryguardian\ui\home",
    "$projectRoot\app\src\main\java\com\voltai\smartbatteryguardian\ui\components"
)

# Typography mapping rules
$typographyMappings = @{
    # Large display text (32sp+) -> headlineLarge/headlineMedium
    'fontSize = 32\.sp,\s*fontWeight = FontWeight\.(Bold|ExtraBold)' = 'style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.$1)'
    'fontSize = 28\.sp,\s*fontWeight = FontWeight\.(Bold|Medium)' = 'style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.$1)'
    'fontSize = 24\.sp,\s*fontWeight = FontWeight\.(Bold|Medium)' = 'style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.$1)'
    
    # Title text (16sp-22sp) -> titleLarge/titleMedium/titleSmall  
    'fontSize = 22\.sp,\s*fontWeight = FontWeight\.(Bold|Medium)' = 'style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.$1)'
    'fontSize = 18\.sp,\s*fontWeight = FontWeight\.(Bold|Medium)' = 'style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.$1)'
    'fontSize = 16\.sp,\s*fontWeight = FontWeight\.(Bold|Medium)' = 'style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.$1)'
    'fontSize = 14\.sp,\s*fontWeight = FontWeight\.(Bold|Medium)' = 'style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.$1)'
    
    # Body text (12sp-16sp) -> bodyLarge/bodyMedium/bodySmall
    'fontSize = 16\.sp,\s*fontWeight = FontWeight\.Normal' = 'style = MaterialTheme.typography.bodyLarge'
    'fontSize = 14\.sp,\s*fontWeight = FontWeight\.Normal' = 'style = MaterialTheme.typography.bodyMedium'
    'fontSize = 12\.sp,\s*fontWeight = FontWeight\.(Normal|Medium)' = 'style = MaterialTheme.typography.bodySmall'
    
    # Label text (9sp-14sp) -> labelLarge/labelMedium/labelSmall
    'fontSize = 14\.sp,\s*fontWeight = FontWeight\.Medium' = 'style = MaterialTheme.typography.labelLarge'
    'fontSize = 12\.sp,\s*fontWeight = FontWeight\.Medium' = 'style = MaterialTheme.typography.labelMedium'  
    'fontSize = 11\.sp,\s*fontWeight = FontWeight\.Medium' = 'style = MaterialTheme.typography.labelMedium'
    'fontSize = 10\.sp,\s*fontWeight = FontWeight\.Medium' = 'style = MaterialTheme.typography.labelSmall'
    'fontSize = 9\.sp,\s*fontWeight = FontWeight\.Medium' = 'style = MaterialTheme.typography.labelSmall'
    
    # Simple fontSize only patterns
    'fontSize = 32\.sp' = 'style = MaterialTheme.typography.headlineLarge'
    'fontSize = 28\.sp' = 'style = MaterialTheme.typography.headlineMedium'
    'fontSize = 24\.sp' = 'style = MaterialTheme.typography.headlineSmall'
    'fontSize = 22\.sp' = 'style = MaterialTheme.typography.titleLarge'
    'fontSize = 18\.sp' = 'style = MaterialTheme.typography.titleLarge'
    'fontSize = 16\.sp' = 'style = MaterialTheme.typography.bodyLarge'
    'fontSize = 14\.sp' = 'style = MaterialTheme.typography.bodyMedium'
    'fontSize = 12\.sp' = 'style = MaterialTheme.typography.bodySmall'
    'fontSize = 11\.sp' = 'style = MaterialTheme.typography.labelMedium'
    'fontSize = 10\.sp' = 'style = MaterialTheme.typography.labelSmall'
    'fontSize = 9\.sp' = 'style = MaterialTheme.typography.labelSmall'
}

$totalReplacements = 0
$filesProcessed = 0

foreach ($directory in $sourceDirectories) {
    if (Test-Path $directory) {
        Write-Host "`nProcessing $directory..." -ForegroundColor Yellow
        
        $ktFiles = Get-ChildItem -Path $directory -Filter "*.kt" -Recurse
        
        foreach ($file in $ktFiles) {
            $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
            $originalContent = $content
            $fileReplacements = 0
            
            foreach ($pattern in $typographyMappings.Keys) {
                $replacement = $typographyMappings[$pattern]
                if ($content -match $pattern) {
                    $content = $content -replace $pattern, $replacement
                    $fileReplacements++
                }
            }
            
            if ($fileReplacements -gt 0) {
                Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
                $relativePath = $file.FullName.Replace("$projectRoot\", "")
                Write-Host "✓ $relativePath : $fileReplacements replacements" -ForegroundColor Green
                $totalReplacements += $fileReplacements
            }
            $filesProcessed++
        }
    }
}

Write-Host "`n" + ("=" * 50) -ForegroundColor Cyan
Write-Host "📊 Typography Migration Summary:" -ForegroundColor Cyan
Write-Host "   Files processed: $filesProcessed" -ForegroundColor White
Write-Host "   Total replacements: $totalReplacements" -ForegroundColor White

if ($totalReplacements -gt 0) {
    Write-Host "`n✨ Typography migration completed!" -ForegroundColor Green
    Write-Host "   Remember to:" -ForegroundColor Yellow
    Write-Host "   • Build the project to check for compilation errors" -ForegroundColor White
    Write-Host "   • Test visual consistency across different screen sizes" -ForegroundColor White
    Write-Host "   • Verify text scaling works properly for accessibility" -ForegroundColor White
} else {
    Write-Host "`n📝 No typography patterns found to replace." -ForegroundColor White
}
