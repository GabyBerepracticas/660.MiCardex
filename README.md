# Mi Cardex (Android · Kotlin · Jetpack Compose · Room)

1. Abre la carpeta `MiCardex` con Android Studio (Koala o más reciente) y espera el Gradle Sync.
2. En `app/src/main/res/values/strings.xml` cambia `000` por los 3 últimos dígitos de tu número de control.
3. Ejecuta en un emulador o dispositivo (Android 8.0 / API 26 o superior).

Estructura:
- data/Materia.kt         -> Entity (tabla única "materias")
- data/MateriaDao.kt      -> Consultas (CRUD, promedio general, promedio por semestre)
- data/CardexDatabase.kt  -> Base de datos Room
- data/PerfilStore.kt     -> SharedPreferences (nombre y número de control)
- CardexViewModel.kt      -> Une Room + SharedPreferences con la interfaz
- ui/                     -> Pantallas Compose y tema morado/azul
