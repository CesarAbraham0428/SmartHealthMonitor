# Sesión 11 — Android TV: Leanback Library

> Rama: `feature/s11-android-tv-leanback` → PR → merge → tag `v2.0.0`

## Inicio obligatorio

```bash
git checkout main && git pull origin main && git checkout -b feature/s11-android-tv-leanback
```

> ⚠️ **Verifica antes de continuar:** el tag `v1.2.0` debe existir en tu repositorio.

---

## Ejercicio 01 — Módulo TV + Leanback Setup + MainActivity

### Paso 1 — Crear el módulo `tv`
1. `File → New → New Module → Android TV Module` (o Empty Android Module).
2. Module name: `tv`. Package: `mx.utng.smarthealthmonitor.tv`. Min SDK: API 21.
3. Finish. Verifica que `settings.gradle.kts` incluye `":tv"`.

### Paso 2 — `tv/build.gradle.kts`

```kotlin
// tv/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}
android {
    compileSdk = 35
    defaultConfig {
        applicationId = "mx.utng.smarthealthmonitor.tv"
        minSdk = 21
        targetSdk = 35
    }
}
dependencies {
    // Leanback Library — el estándar de Android TV
    implementation("androidx.leanback:leanback:1.2.0")
    // Glide para cargar imágenes en las cards
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Compartir Room + Repository con módulo app
    implementation(project(":app"))
    // ViewModel + Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}
```

### Paso 3 — Tema Leanback

`tv/src/main/res/values/themes.xml`:

```xml
<!-- tv/src/main/res/values/themes.xml -->
<resources>
    <!-- Tema base para Android TV —
         Theme.Leanback hereda de AppCompat y agrega los
         estilos de navegación D-pad automáticamente -->
    <style name="Theme.SmartHealthTV"
           parent="Theme.Leanback">
        <item name="colorPrimary">@color/sh_primary</item>
        <item name="colorAccent">@color/sh_amber</item>
        <item name="android:colorBackground">@color/sh_dark</item>
    </style>
</resources>
```

### Paso 4 — `MainActivity.kt`

```kotlin
// tv/src/main/java/.../tv/MainActivity.kt
package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * MainActivity para Android TV.
 * Es solo el contenedor: carga MainFragment.
 * TODA la lógica de UI va en el Fragment.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commit()
        }
    }
}
```

### Paso 5 — `activity_main.xml`

```xml
<!-- tv/src/main/res/layout/activity_main.xml -->
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/main_browse_fragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### Paso 6 — `AndroidManifest.xml` del módulo `tv`

```xml
<!-- tv/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Declarar que es una app de TV -->
    <uses-feature android:name="android.software.leanback"
        android:required="true" />
    <!-- No requiere pantalla táctil -->
    <uses-feature android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:theme="@style/Theme.SmartHealthTV"
        android:banner="@drawable/banner_tv"> <!-- 320x180dp -->
        <activity android:name=".MainActivity"
            android:exported="true"
            android:label="SmartHealth TV">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <!-- Categoría LEANBACK_LAUNCHER: aparece en launcher TV -->
                <category android:name=
                    "android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

4. Crea `tv/src/main/res/drawable/banner_tv.png`: imagen de 320×180dp (banner del launcher de Android TV).
5. Ejecuta el módulo `tv` en el AVD Android TV. Debe aparecer la pantalla en blanco con el tema oscuro.

> 🔔 **Acción requerida:** haz commit de este ejercicio:
> ```bash
> git add . && git commit -m 'chore: add Android TV module with Leanback setup, MainActivity and Theme'
> ```

### ⭐ Reto adicional (opcional)

Agrega colores personalizados en `tv/res/values/colors.xml` que coincidan con la paleta MD3 de S2: `sh_primary` (#1B4F8A), `sh_amber` (#D4860A), `sh_dark` (#0D1117), `sh_error` (#B3261E). Úsalos en el tema Leanback.

> 🔔 **Acción requerida:** haz commit del reto:
> ```bash
> git commit -m 'chore: add SmartHealth TV color palette'
> ```

---

## Ejercicio 02 — MainFragment + FCCardPresenter + 2 filas

### Paso 1 — `FCCardPresenter.kt`

```kotlin
// tv/src/main/java/.../tv/FCCardPresenter.kt
package mx.utng.smarthealthmonitor.tv

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter

class FCCardPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            // CRÍTICO: sin estas dos líneas,
            // el D-pad no puede navegar a este card
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(240, 180)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val card = viewHolder.view as ImageCardView
        val lectura = item as LecturaFC
        card.titleText = "${lectura.valorBpm} bpm"
        card.contentText = lectura.hora

        // Color de fondo según si FC es normal
        val bgColor = if (lectura.esNormal) {
            Color.parseColor("#1B4F8A") // primary
        } else {
            Color.parseColor("#B3261E") // error
        }
        card.setBackgroundColor(bgColor)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        (viewHolder.view as ImageCardView).mainImage = null
    }
}
```

### Paso 2 — `MainFragment.kt`

```kotlin
// tv/src/main/java/.../tv/MainFragment.kt
package mx.utng.smarthealthmonitor.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import mx.utng.smarthealthmonitor.data.MockData

class MainFragment : BrowseSupportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del BrowseFragment
        title = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Color de la marca en el sidebar
        brandColor = resources.getColor(R.color.sh_primary, null)

        cargarFilas()
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // ── Fila 1: Estado actual (FC + Pasos) ───────────
        val estadoAdapter = ArrayObjectAdapter(FCCardPresenter())
        // Datos simulados — en Ej.03 vendrán de Room
        estadoAdapter.add(LecturaFC(id=0, valorBpm=88, hora="Ahora"))
        estadoAdapter.add(LecturaFC(id=1, valorBpm=4250, hora="Pasos"))
        rowsAdapter.add(ListRow(HeaderItem("Estado actual"), estadoAdapter))

        // ── Fila 2: Historial de FC ────────────────────
        val histAdapter = ArrayObjectAdapter(FCCardPresenter())
        MockData.historialFC.forEach { histAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem("Historial FC"), histAdapter))

        this.adapter = rowsAdapter
    }
}
```

### Paso 3 — Verificar navegación D-pad en emulador TV

1. Ejecuta el módulo `tv` en el AVD Android TV.
2. La pantalla debe mostrar el `BrowseSupportFragment` con dos filas.
3. Usa las **teclas de flecha** del teclado para navegar entre cards.
4. Verifica que al presionar `↑↓` el foco cambia entre filas.
5. Verifica que al presionar `←→` el foco cambia entre cards de la misma fila.
6. La card con foco debe verse diferente (Leanback aplica elevación automáticamente).

**Checklist de pruebas D-pad:**

| Prueba D-pad | Tecla emulador | ¿Funciona? |
|---|---|---|
| Moverse entre filas (arriba/abajo) | ↑ / ↓ | ☐ Sí ☐ No |
| Moverse entre cards (izquierda/derecha) | ← / → | ☐ Sí ☐ No |
| Seleccionar card (OK) | Enter | ☐ Sí ☐ No |
| Regresar (Back) | Backspace / Esc | ☐ Sí ☐ No |
| Las cards fuera de rango muestran color rojo | FC > 100 | ☐ Sí ☐ No |

> 🔔 **Acción requerida:** haz commit de este ejercicio:
> ```bash
> git add . && git commit -m 'feat: add MainFragment (BrowseSupportFragment) with FCCardPresenter and 2 rows'
> ```

### ⭐ Reto adicional (opcional)

Agrega una tercera fila "Alertas recientes" usando el mismo `FCCardPresenter` pero con datos de alertas (lista simulada de 3 alertas con hora y tipo).

> 🔔 **Acción requerida:** haz commit del reto:
> ```bash
> git commit -m 'feat: add alerts row to SmartHealth TV BrowseFragment'
> ```

---

## Ejercicio 03 — TvViewModel + Room + PR + Tag v2.0.0

### Paso 1 — `TvViewModel.kt`

```kotlin
// tv/src/main/java/.../tv/TvViewModel.kt
package mx.utng.smarthealthmonitor.tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.smarthealthmonitor.data.db.LecturaFC

class TvViewModel : ViewModel() {
    // FC actual del wearable (o 0 si no hay dato)
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .stateIn(viewModelScope,
            SharingStarted.WhileSubscribed(5_000), 0)

    // Historial de lecturas desde Room DAO
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList())
}
```

### Paso 2 — Actualizar `MainFragment` para usar `TvViewModel`

```kotlin
// MainFragment.kt — agregar ViewModel y observar datos
class MainFragment : BrowseSupportFragment() {

    private val viewModel: TvViewModel by viewModels()
    private lateinit var histAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ... configuración de título y colores (sin cambios) ...
        cargarFilas()
        observarDatos()
    }

    private fun observarDatos() {
        // Observar historial de Room y actualizar la fila
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historial.collect { lecturas ->
                    histAdapter.clear()
                    lecturas.forEach { histAdapter.add(it) }
                }
            }
        }
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // Fila historial con adapter reactivo
        histAdapter = ArrayObjectAdapter(FCCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Historial FC"), histAdapter))

        this.adapter = rowsAdapter
    }
}
```

### Paso 3 — Verificar con datos reales

1. Asegura que `SmartHealthRepository.init(context)` se llama en la `Application` del módulo `tv`.
2. Simula FC desde el emulador Wear OS (Health Services) o desde el botón de simulación del módulo `app`.
3. Verifica que las cards en la TV se actualizan cuando cambian los datos del Repository.

> 🔔 **Acción requerida:** haz commit de este ejercicio:
> ```bash
> git add . && git commit -m 'feat: connect TvViewModel to Room DAO and StateFlow for reactive TV UI'
> ```

> 🔔 **Acción requerida:** sube la rama al repositorio remoto:
> ```bash
> git push origin feature/s11-android-tv-leanback
> ```

---

## Pull Request y Tag v2.0.0

> 🔔 **Acción requerida — Pull Request:**
> 1. En GitHub: `Compare & pull request`.
> 2. Título: `feat: Android TV Leanback — BrowseFragment + Cards + Room — S11 Unidad III`.
> 3. `Merge pull request → Confirm merge`.

> 🔔 **Acción requerida:** actualiza `main` después del merge:
> ```bash
> git checkout main && git pull origin main
> ```

> 🔔 **Acción requerida — crear Tag:**
> ```bash
> git tag -a v2.0.0 -m 'feat: SmartHealth TV — Android TV Leanback Library — Unidad III S11'
> ```

> 🔔 **Acción requerida:** subir el tag a GitHub:
> ```bash
> git push origin v2.0.0
> ```

---

## URLs de evidencia para Moodle

| Elemento | Cómo obtenerlo |
|---|---|
| URL repositorio | `github.com/[usuario]/SmartHealthMonitor` |
| URL commit Ej.01 (setup TV) | Code → Commits → commit `'chore: add Android TV module'` |
| URL commit Ej.02 (MainFragment + Cards) | Code → Commits → commit `'feat: add MainFragment'` |
| URL commit Ej.03 (TvViewModel + Room) | Code → Commits → commit `'feat: connect TvViewModel'` |
| URL PR S11 | Pull requests → PR cerrado S11 → copiar URL |
| URL Release v2.0.0 | Releases → v2.0.0 → copiar URL |
| Captura BrowseFragment en emulador TV | Pantalla TV mostrando filas de cards con datos de FC |
| Captura foco D-pad visible | Card seleccionado mostrando indicador de foco (elevación/borde) |
