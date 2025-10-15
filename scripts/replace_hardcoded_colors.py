#!/usr/bin/env python3
"""
Material Design 3 Color Replacement Script
Replaces hardcoded hex colors with Material 3 semantic color tokens
"""

import os
import re
import sys
from pathlib import Path

# Color mapping from hardcoded hex to Material 3 semantic tokens
COLOR_REPLACEMENTS = {
    # Common grays and neutrals
    'Color(0xFF444444)': 'MaterialTheme.colorScheme.mutedText',
    'Color(0xFF333333)': 'MaterialTheme.colorScheme.mutedText',
    'Color(0xFF666666)': 'MaterialTheme.colorScheme.mutedText',
    'Color(0xFF999999)': 'MaterialTheme.colorScheme.secondaryText',
    'Color(0xFFAAAAAA)': 'MaterialTheme.colorScheme.secondaryText',
    'Color(0xFFCCCCCC)': 'MaterialTheme.colorScheme.secondaryText',
    'Color(0xFFF0F0F0)': 'MaterialTheme.colorScheme.progressBackground',
    'Color(0xFFF8F9FA)': 'MaterialTheme.colorScheme.placeholderBackground',
    'Color(0xFFE8F5E8)': 'MaterialTheme.colorScheme.progressBackground',
    'Color.Black': 'MaterialTheme.colorScheme.onSurface',
    'Color.White': 'MaterialTheme.colorScheme.surface',
    
    # Common green colors (battery/charging indicators)
    'Color(0xFF4CAF50)': 'MaterialTheme.colorScheme.chargingIndicator',
    'Color(0xFF81C784)': 'MaterialTheme.colorScheme.positiveIndicator',
    'Color(0xFF10B981)': 'MaterialTheme.colorScheme.positiveIndicator',
    
    # Warning/error colors
    'Color(0xFFE57373)': 'MaterialTheme.colorScheme.error',
    'Color(0xFFFF5722)': 'MaterialTheme.colorScheme.error',
    'Color(0xFFD32F2F)': 'MaterialTheme.colorScheme.error',
    'Color(0xFFEF4444)': 'MaterialTheme.colorScheme.error',
    'Color(0xFFDC2626)': 'MaterialTheme.colorScheme.error',
    
    # Warning/amber colors
    'Color(0xFFFF9800)': 'MaterialTheme.colorScheme.tertiary',
    'Color(0xFFFFB74D)': 'MaterialTheme.colorScheme.tertiary',
    'Color(0xFFF59E0B)': 'MaterialTheme.colorScheme.tertiary',
    
    # Blue colors (secondary)
    'Color(0xFF2196F3)': 'MaterialTheme.colorScheme.secondary',
    'Color(0xFF0066A8)': 'MaterialTheme.colorScheme.secondary',
    'Color(0xFF1E88E5)': 'MaterialTheme.colorScheme.secondary',
    
    # Purple colors (tertiary)
    'Color(0xFF9C27B0)': 'MaterialTheme.colorScheme.tertiary',
    'Color(0xFF6366F1)': 'MaterialTheme.colorScheme.primary',
    'Color(0xFF8B5CF6)': 'MaterialTheme.colorScheme.tertiary',
    
    # Gray/neutral utility colors
    'Color(0xFF607D8B)': 'MaterialTheme.colorScheme.mutedText',
    'Color(0xFF37474F)': 'MaterialTheme.colorScheme.mutedText',
    'Color(0xFF263238)': 'MaterialTheme.colorScheme.onSurface',
    
    # Background colors
    'Color(0xFFFDFDFD)': 'MaterialTheme.colorScheme.surface',
    'Color(0xFF0F0F0F)': 'MaterialTheme.colorScheme.surface',
    'Color(0xFF1A1A1A)': 'MaterialTheme.colorScheme.surface',
}

# Special replacements for copy() calls
COPY_REPLACEMENTS = {
    'Color.Black.copy(alpha = 0.05f)': 'MaterialTheme.colorScheme.editModeOverlay',
    'Color.Black.copy(alpha = 0.1f)': 'MaterialTheme.colorScheme.scrim.copy(alpha = 0.1f)',
    'Color.Black.copy(alpha = 0.2f)': 'MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f)',
    'Color.Black.copy(alpha = 0.32f)': 'MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)',
    'Color.Black.copy(alpha = 0.5f)': 'MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)',
    'Color.Black.copy(alpha = 0.7f)': 'MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)',
}

def should_process_file(file_path):
    """Check if file should be processed"""
    return (
        file_path.suffix == '.kt' and 
        'src/main' in str(file_path) and
        'test' not in str(file_path.name.lower())
    )

def replace_colors_in_file(file_path):
    """Replace hardcoded colors in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        changes_made = 0
        
        # Replace direct color mappings
        for old_color, new_color in COLOR_REPLACEMENTS.items():
            if old_color in content:
                content = content.replace(old_color, new_color)
                changes_made += content.count(new_color) - original_content.count(new_color)
        
        # Replace copy() variants
        for old_pattern, new_pattern in COPY_REPLACEMENTS.items():
            if old_pattern in content:
                content = content.replace(old_pattern, new_pattern)
                changes_made += 1
        
        # Replace gradient colors with Material tokens
        gradient_pattern = r'Color\(0xFF[A-Fa-f0-9]{6}\)\.copy\(alpha = 0\.\d+f\)'
        content = re.sub(gradient_pattern, lambda m: f"{COLOR_REPLACEMENTS.get(m.group().split('.copy')[0], m.group())}.copy(alpha = {m.group().split('alpha = ')[1].split(')')[0]})", content)
        
        if changes_made > 0:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✓ {file_path.relative_to(Path.cwd())}: {changes_made} replacements")
            return changes_made
        
    except Exception as e:
        print(f"✗ Error processing {file_path}: {e}")
        return 0
    
    return 0

def main():
    """Main function to process all Kotlin files"""
    project_root = Path(__file__).parent.parent
    src_dirs = [
        project_root / "app/src/main/java",
        project_root / "ui/src/main/kotlin", 
        project_root / "features/src/main/kotlin",
    ]
    
    total_changes = 0
    files_processed = 0
    
    print("🎨 Material Design 3 Color Migration")
    print("=" * 50)
    
    for src_dir in src_dirs:
        if src_dir.exists():
            print(f"\nProcessing {src_dir}...")
            for file_path in src_dir.rglob("*.kt"):
                if should_process_file(file_path):
                    changes = replace_colors_in_file(file_path)
                    total_changes += changes
                    files_processed += 1
    
    print("\n" + "=" * 50)
    print(f"📊 Summary:")
    print(f"   Files processed: {files_processed}")
    print(f"   Total replacements: {total_changes}")
    
    if total_changes > 0:
        print(f"\n✨ Color migration completed successfully!")
        print(f"   Remember to:")
        print(f"   • Import MaterialTheme in files that need it")
        print(f"   • Add @Composable annotations where needed")
        print(f"   • Test the app for visual consistency")
    else:
        print(f"\n📝 No hardcoded colors found to replace.")

if __name__ == "__main__":
    main()
