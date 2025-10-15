#!/usr/bin/env python3

import os
import re
import glob

# Define color mappings from hardcoded hex colors to semantic Material 3 colors
COLOR_MAPPINGS = {
    # Common color mappings
    'Color(0xFF4CAF50)': 'MaterialTheme.colorScheme.primary', # Green primary
    'Color(0xFF2196F3)': 'MaterialTheme.colorScheme.primary', # Blue primary  
    'Color(0xFFFF9800)': 'LocalPremiumColors.current.warning', # Orange warning
    'Color(0xFFFF5722)': 'LocalPremiumColors.current.warning', # Red-orange warning
    'Color(0xFFD32F2F)': 'MaterialTheme.colorScheme.error', # Red error
    'Color(0xFFE57373)': 'LocalPremiumColors.current.batteryRed', # Light red
    'Color(0xFFFFB74D)': 'LocalPremiumColors.current.warning', # Light orange
    'Color(0xFF81C784)': 'LocalPremiumColors.current.success', # Light green
    'Color(0xFF666666)': 'MaterialTheme.colorScheme.onSurfaceVariant', # Gray text
    'Color(0xFF757575)': 'MaterialTheme.colorScheme.outline', # Darker gray
    'Color(0xFFF0F0F0)': 'MaterialTheme.colorScheme.surfaceVariant', # Light background
    'Color(0xFFFFFFFF)': 'MaterialTheme.colorScheme.surface', # White
    'Color(0xFF000000)': 'MaterialTheme.colorScheme.onSurface', # Black
    'Color.Black': 'MaterialTheme.colorScheme.onSurface',
    'Color.White': 'MaterialTheme.colorScheme.surface',
    'Color.Gray': 'MaterialTheme.colorScheme.onSurfaceVariant',
    
    # Battery specific colors
    'Color(0xFF00E676)': 'LocalPremiumColors.current.batteryGreen',
    'Color(0xFF76FF03)': 'LocalPremiumColors.current.batteryGreen',
    'Color(0xFFFFEB3B)': 'LocalPremiumColors.current.batteryYellow',
    'Color(0xFFF44336)': 'LocalPremiumColors.current.batteryRed',
    
    # Premium/accent colors
    'Color(0xFF6A1B9A)': 'LocalPremiumColors.current.premium',
    'Color(0xFF673AB7)': 'MaterialTheme.colorScheme.secondary',
    'Color(0xFF9C27B0)': 'MaterialTheme.colorScheme.tertiary',
    'Color(0xFF3F51B5)': 'MaterialTheme.colorScheme.primary',
    'Color(0xFF00BCD4)': 'MaterialTheme.colorScheme.secondary',
    'Color(0xFF009688)': 'LocalPremiumColors.current.success',
    'Color(0xFF795548)': 'MaterialTheme.colorScheme.outline',
    'Color(0xFF607D8B)': 'MaterialTheme.colorScheme.onSurfaceVariant',
    
    # Additional commonly used colors
    'Color(0xFFE8F5E8)': 'LocalPremiumColors.current.batteryGreen.copy(alpha = 0.2f)',
    'Color(0xFFFFD700)': 'LocalPremiumColors.current.warning',
    'Color(0xFFB8860B)': 'LocalPremiumColors.current.warning',
    'Color(0xFF10B981)': 'LocalPremiumColors.current.success',
    'Color(0xFFEF4444)': 'MaterialTheme.colorScheme.error',
    'Color(0xFF6366F1)': 'MaterialTheme.colorScheme.primary',
}

def replace_colors_in_file(file_path):
    """Replace hardcoded colors in a single file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        replacements_made = 0
        
        # Replace each hardcoded color with its semantic equivalent
        for hardcoded_color, semantic_color in COLOR_MAPPINGS.items():
            if hardcoded_color in content:
                content = content.replace(hardcoded_color, semantic_color)
                replacements_made += 1
        
        # Only write if changes were made
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"✅ Updated {file_path} - {replacements_made} color replacements")
            return True
        else:
            print(f"⚪ No changes needed in {file_path}")
            return False
            
    except Exception as e:
        print(f"❌ Error processing {file_path}: {e}")
        return False

def add_imports_to_file(file_path):
    """Add necessary imports to files that use semantic colors"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Check if LocalPremiumColors is used but not imported
        if 'LocalPremiumColors.current' in content and 'import com.voltai.smartbatteryguardian.ui.theme.LocalPremiumColors' not in content:
            # Find the import section and add the import
            import_pattern = r'(import com\.voltai\.smartbatteryguardian\.ui\.theme\.[^\\n]+)'
            existing_theme_import = re.search(import_pattern, content)
            
            if existing_theme_import:
                # Add after existing theme import
                new_import = existing_theme_import.group(1) + '\\nimport com.voltai.smartbatteryguardian.ui.theme.LocalPremiumColors'
                content = content.replace(existing_theme_import.group(1), new_import)
            else:
                # Add after other imports
                package_pattern = r'(package [^\\n]+\\n)'
                package_match = re.search(package_pattern, content)
                if package_match:
                    imports_start = content.find('import', package_match.end())
                    if imports_start != -1:
                        # Find end of imports
                        lines = content[imports_start:].split('\\n')
                        import_end_idx = 0
                        for i, line in enumerate(lines):
                            if line.strip().startswith('import'):
                                import_end_idx = i
                        
                        # Insert the new import
                        if import_end_idx > 0:
                            insertion_point = imports_start + len('\\n'.join(lines[:import_end_idx + 1]))
                            content = content[:insertion_point] + '\\nimport com.voltai.smartbatteryguardian.ui.theme.LocalPremiumColors' + content[insertion_point:]
            
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"📝 Added LocalPremiumColors import to {file_path}")
            
    except Exception as e:
        print(f"❌ Error adding imports to {file_path}: {e}")

def main():
    """Main function to process all Kotlin files in the project"""
    # Define directories to process
    directories_to_process = [
        'app/src/main/java/com/voltai/smartbatteryguardian/ui/',
        'app/src/main/java/com/voltai/smartbatteryguardian/screens/',
        'app/src/main/java/com/voltai/smartbatteryguardian/utils/',
    ]
    
    total_files_processed = 0
    total_files_changed = 0
    
    for directory in directories_to_process:
        if os.path.exists(directory):
            # Find all Kotlin files in the directory
            kotlin_files = glob.glob(os.path.join(directory, '**/*.kt'), recursive=True)
            
            print(f"\\n🔍 Processing directory: {directory}")
            print(f"Found {len(kotlin_files)} Kotlin files")
            
            for file_path in kotlin_files:
                total_files_processed += 1
                if replace_colors_in_file(file_path):
                    total_files_changed += 1
                    add_imports_to_file(file_path)
        else:
            print(f"❌ Directory not found: {directory}")
    
    print(f"\\n📊 Summary:")
    print(f"Total files processed: {total_files_processed}")
    print(f"Files with changes: {total_files_changed}")
    print(f"Completion: {(total_files_changed / max(total_files_processed, 1)) * 100:.1f}%")

if __name__ == '__main__':
    main()
