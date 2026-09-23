# Visual Improvements Implementation Plan

This plan outlines the changes to make the app's visuals more eye-friendly, introduce varied typography (avoiding identical font styling across all elements), and ensure all screen titles are centered on the screen.

## User Review Required

> [!NOTE]
> - **Theme & Colors**: Introducing an eye-friendly Material 3 color palette (Indigo and Teal with warm slate surfaces) supporting both light and dark modes.
> - **Typography**: Utilizing varied font weights (Bold, SemiBold, Medium, Regular) and hierarchical text styles to eliminate the uniform look.
> - **Screen-Centered Titles**: Updating all screen headers (including Contacts and Notes) so titles are strictly centered on the screen.
> - **Icons & Shapes**: Adding intuitive icons and soft rounded corners (`RoundedCornerShape(16.dp)` / `24.dp`) across cards, buttons, and text fields for a modern, polished aesthetic.

## Proposed Changes

### [Dependencies & Theme]
#### [MODIFY] [build.gradle.kts](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/build.gradle.kts)
- Add `androidx.compose.material:material-icons-extended` for rich iconography.

#### [NEW] [Theme.kt](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/src/main/java/com/example/proyectopasantia/ui/theme/Theme.kt)
- Define custom Material 3 Light and Dark color schemes (Indigo/Teal/Slate) and shapes.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/src/main/java/com/example/proyectopasantia/MainActivity.kt)
- Wrap `AppNavigation()` in `AppTheme`.

---

### [Screens & Visuals]
#### [MODIFY] [HomeScreen.kt](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/src/main/java/com/example/proyectopasantia/ui/screens/HomeScreen.kt)
- Screen-centered title ("Inicio") with bold typography and a header icon.
- Eye-friendly buttons with icons and rounded shapes.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/src/main/java/com/example/proyectopasantia/ui/screens/LoginScreen.kt)
- Screen-centered title and subtitle with distinct weights.
- Text fields with leading icons (Email, Lock) and rounded borders.

#### [MODIFY] [RegisterScreen.kt](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/src/main/java/com/example/proyectopasantia/ui/screens/RegisterScreen.kt)
- Screen-centered title ("Crear cuenta").
- Consistent iconography, rounded text fields, and refined button styling.

#### [MODIFY] [ContactsScreen.kt](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/src/main/java/com/example/proyectopasantia/ui/screens/ContactsScreen.kt)
- Screen-centered title ("Mis contactos") using a centered top bar / header layout.
- Eye-friendly contact cards with rounded corners, icons (phone, email, person), and distinct typography.

#### [MODIFY] [NotesScreen.kt](file:///C:/Users/Estudiante/StudioProjects/Proyecto-Pasantia/app/src/main/java/com/example/proyectopasantia/ui/screens/NotesScreen.kt)
- Screen-centered title ("Mis notas") using a centered header layout.
- Eye-friendly note cards with rounded corners, category badges, icons, and distinct typography.

## Verification Plan

### Automated Tests
- Build the app with `gradle_build("app:assembleDebug")` to ensure compilation success.

### Manual Verification
- Deploy and verify UI on the device/emulator:
  - Check that all titles ("Inicio", "Librería de notas y contactos", "Crear cuenta", "Mis contactos", "Mis notas") are perfectly screen-centered.
  - Verify eye-friendly colors, typography differentiation, rounded cards, and icons.
