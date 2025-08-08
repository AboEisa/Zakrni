# Islamic App Bottom Navigation Implementation

## Overview
This implementation adds a functional bottom navigation menu with 6 Islamic sections to the Zakrni app.

## Features Implemented

### 1. Bottom Navigation Menu
- **Location**: `res/menu/middle_navigation.xml`
- **Items**: 6 navigation items in Arabic:
  - أسماء الله الحسنى (Names of Allah)
  - التسبيح الذكر (Dhikr/Remembrance)
  - الدعاء (Supplication)
  - الحديث (Hadith)
  - الذكر (Remembrance)
  - القبلة (Qibla)

### 2. Icons
- Created vector drawable icons for each section in `res/drawable/`
- All icons use brown color tint (#795547) to match app theme

### 3. HomeActivity
- **Location**: `clean/ui/HomeActivity.kt`
- Handles bottom navigation item selection
- Displays fragments based on selected item
- Shows fixed time "21:30" as requested
- Uses brown/beige color scheme

### 4. Fragment System
- Created 6 placeholder fragments for each navigation section
- Each fragment shows:
  - Section icon
  - Arabic title
  - Description in Arabic
  - "قريباً..." (Coming Soon) indicator

### 5. Navigation Flow
1. MainActivity (landing screen) → "ابدأ الآن" button → HomeActivity
2. HomeActivity contains bottom navigation with 6 sections
3. Each section loads corresponding fragment

### 6. Color Scheme
- **Primary**: #795547 (Brown)
- **Background**: #F3E8D5 (Beige)
- **Text**: Brown variations
- Maintains Islamic app aesthetic

### 7. Layout Structure
```
activity_main.xml (Landing screen)
├── Title: "تطبيق العبادات"
├── Description
└── "ابدأ الآن" button → HomeActivity

activity_home.xml
├── Header (App title + Time: "21:30")
├── Fragment container (main content)
└── BottomNavigationView (6 items)

fragment_section.xml (Template for all sections)
├── Section icon
├── Arabic title
├── Description
└── "Coming Soon" indicator
```

## Files Created/Modified

### New Files:
- `res/menu/middle_navigation.xml`
- `res/layout/activity_home.xml`
- `res/layout/fragment_section.xml`
- `res/drawable/ic_names_allah.xml`
- `res/drawable/ic_dhikr.xml`
- `res/drawable/ic_supplication.xml`
- `res/drawable/ic_hadith.xml`
- `res/drawable/ic_remembrance.xml`
- `res/drawable/ic_qibla.xml`
- `clean/ui/HomeActivity.kt`
- `clean/ui/NamesOfAllahFragment.kt`
- `clean/ui/DhikrFragment.kt`
- `clean/ui/SupplicationFragment.kt`
- `clean/ui/HadithFragment.kt`
- `clean/ui/RemembranceFragment.kt`
- `clean/ui/QiblaFragment.kt`

### Modified Files:
- `clean/ui/MainActivity.kt` (added navigation to HomeActivity)
- `AndroidManifest.xml` (registered HomeActivity)
- `res/values/colors.xml` (added brown/beige colors)
- `res/values/themes.xml` (added color theme and navigation style)

## Implementation Details

### Bottom Navigation Setup
```kotlin
binding.bottomNavigation.setOnItemSelectedListener { item ->
    when (item.itemId) {
        R.id.nav_names_of_allah -> loadFragment(NamesOfAllahFragment())
        R.id.nav_dhikr -> loadFragment(DhikrFragment())
        R.id.nav_supplication -> loadFragment(SupplicationFragment())
        R.id.nav_hadith -> loadFragment(HadithFragment())
        R.id.nav_remembrance -> loadFragment(RemembranceFragment())
        R.id.nav_qibla -> loadFragment(QiblaFragment())
        else -> false
    }
}
```

### Time Display
- Fixed time display showing "21:30" as requested in the header
- Styled with brown color to match theme

### Fragment Loading
```kotlin
private fun loadFragment(fragment: Fragment) {
    supportFragmentManager
        .beginTransaction()
        .replace(R.id.fragment_container, fragment)
        .commit()
}
```

## Technical Notes
- Uses Material Design 3 components
- Implements Fragment-based navigation
- Follows Android Architecture best practices
- Supports RTL layout for Arabic text
- Uses ViewBinding for type-safe view access
- Integrates with existing Hilt dependency injection

## Future Enhancements
Each placeholder fragment can be expanded to include:
- Names of Allah with meanings and benefits
- Dhikr counter functionality
- Dua collections with audio
- Hadith collections with authentication
- Qibla compass with location services
- Prayer time notifications