# Visual Improvements Walkthrough

Successfully updated the application's UI/UX to be more eye-friendly, introduced varied typography hierarchy, and ensured all screen titles are centered on the screen.

## Changes Made

### 1. Custom Eye-Friendly Theme & Typography (`AppTheme.kt`)
- Created a custom Material 3 color scheme utilizing soothing Indigo and Teal primary/secondary tones with light and dark mode support.
- Configured surface colors (`#F8FAFC` background) to reduce eye strain.
- Applied varied font weights (`FontWeight.Bold`, `FontWeight.SemiBold`, `FontWeight.Medium`, `FontWeight.Normal`) across elements to eliminate the uniform font look.

### 2. Screen-Centered Titles Across All Screens
- **HomeScreen**: Title ("Inicio") and subtitle centered on the screen with a header icon and soft rounded action buttons.
- **LoginScreen & RegisterScreen**: Titles ("Librería de notas y contactos", "Crear cuenta") and descriptions centered horizontally with icons and rounded text fields (`RoundedCornerShape(14.dp)`).
- **ContactsScreen & NotesScreen**: Headers featuring screen-centered titles ("Mis contactos", "Mis notas") using a centered title layout with back navigation on the left.

### 3. Modern Iconography & Eye-Friendly Cards
- Added Material Icons Extended (`androidx.compose.material:material-icons-extended`) across actions, text fields, and cards.
- Designed polished, elevated cards with rounded corners (`RoundedCornerShape(16.dp)`), category badges, and distinct edit/delete action buttons.

## Validation Results

### Automated Build
- Executed `gradle_build("app:assembleDebug")`: **Build finished successfully.**
