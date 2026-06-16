# Documentación del Proyecto: Smart Health Monitor

## 1. Arquitectura y Diseño Estructural

**Smart Health Monitor** es una aplicación de monitoreo de salud con arquitectura **cliente-servidor emparejado** entre un dispositivo móvil (teléfono) y un reloj inteligente (Wear OS). Sigue el patrón **MVVM** en ambos módulos, utiliza **Kotlin** y **Jetpack Compose** para la UI, **Room** para persistencia local, y la **Wearable Data Layer API** de Google Play Services para la comunicación inter-dispositivos.

### 1.1 Estructura de Módulos

El proyecto está dividido en dos módulos Gradle independientes que comparten el mismo `applicationId`:

```
smarthealthmonitor/
├── app/                          # Módulo teléfono (Android)
│   └── src/main/java/mx/utng/cala/smarthealthmonitor/
│       ├── MainActivity.kt
│       ├── LoginScreen.kt
│       ├── navigation/
│       │   ├── Screen.kt
│       │   └── NavGraph.kt
│       ├── ui/
│       │   ├── theme/            # Color.kt, Theme.kt, Type.kt
│       │   ├── components/       # TarjetaDato.kt, FilaHistorial.kt
│       │   ├── screens/          # DashboardScreen.kt, HistorialScreen.kt, AlertaScreen.kt
│       │   └── viewmodel/        # DashboardViewModel.kt
│       └── data/models/
│           ├── SmartHealthData.kt
│           ├── SmartHealthRepository.kt
│           ├── WearListenerService.kt
│           └── db/               # SmartHealthDB.kt, LecturaFC.kt, LecturaFCDao.kt
│
└── wear/                         # Módulo smartwatch (Wear OS)
    └── src/main/java/mx/utng/cala/wear/
        ├── data/
        │   └── SmartHealthRepository.kt
        └── presentation/
            ├── WearMainActivity.kt
            ├── WearDashboardViewModel.kt
            ├── WearDashboardScreen.kt
            ├── theme/            # WearTheme.kt, Theme.kt
            ├── components/       # WearFCCard.kt
            └── data/models/      # WearDataSender.kt, HealthDataService.kt
```

**Configuración técnica común:**

| Parámetro | `app` | `wear` |
|---|---|---|
| `applicationId` | `mx.utng.cala.smarthealthmonitor` | `mx.utng.cala.smarthealthmonitor` |
| `compileSdk` | 35 | 35 |
| `minSdk` | 26 | 30 |
| `targetSdk` | 35 | 35 |
| `kotlinOptions.jvmTarget` | 11 | 11 |

### 1.2 Capas Arquitectónicas

Cada módulo sigue una arquitectura en 3 capas:

```
┌─────────────────────────────────────────────────────────────┐
│                  CAPA DE PRESENTACIÓN (UI)                  │
│  Compose Screens → ViewModels (StateFlow) → Componentes UI │
├─────────────────────────────────────────────────────────────┤
│                  CAPA DE DATOS (Data Layer)                 │
│  Repositorios → DAOs / Services → Wearable API / Room      │
├─────────────────────────────────────────────────────────────┤
│                  CAPA DE INFRAESTRUCTURA                    │
│  Wearable Data Layer (Google Play Services) / Health       │
│  Services API / Room Database                               │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Flujo de Datos y Comunicación

### 2.1 Comunicación Reloj → Teléfono

```
[Wear OS]
HealthDataService (PassiveListenerService)
    ↓ Recibe HEART_RATE_BPM del sensor
WearDataSender (MessageClient.sendMessage)
    ↓ Envía por Wearable Data Layer
    ├── PATH: /smarthealthmonitor/fc
    └── PATH: /smarthealthmonitor/pasos
         ↓
[Teléfono]
WearListenerService (WearableListenerService)
    ↓ onMessageReceived()
SmartHealthRepository (app)
    ↓ actualizarFC() / actualizarPasos()
    ├── StateFlow (fcFlow / pasosFlow) → DashboardViewModel → UI
    └── Room DAO (LecturaFCDao.insertar) → SQLite persistente
```

### 2.2 Flujo Interno en el Módulo App

```
LoginScreen
    ↓ onLoginSuccess
NavGraph → DashboardScreen
    ├── DashboardViewModel (StateFlow)
    │   ├── fc ← SmartHealthRepository.fcFlow ∨ MockData.fcActual
    │   ├── pasos ← SmartHealthRepository.pasosFlow ∨ MockData.pasosActual
    │   └── historial ← SmartHealthRepository.obtenerHistorial() (Room Flow)
    │
    ├── TarjetaDato (frecuencia cardíaca)
    ├── TarjetaDato (pasos)
    ├── FilaHistorial (lecturas recientes)
    ├── AlertaScreen (diálogo de emergencia)
    └── onHistorialClick → HistorialScreen
        └── LazyColumn con FilaHistorial (historial completo desde Room)
```

---

## 3. Componentes del Módulo `app` (Teléfono)

### 3.1 Capa de Presentación

#### 3.1.1 `MainActivity.kt`
**Ruta:** `app/.../MainActivity.kt`  
**Propósito:** Punto de entrada de la aplicación. Configura el tema Material3 y llama a `SmartHealthNavGraph()` como composable raíz. Contiene también `SmartHealthScreen` (vista responsiva legacy con layouts Portrait/Landscape) y previsualizaciones de temas.

#### 3.1.2 Navegación

**`navigation/Screen.kt`** — Define las 4 rutas de navegación como `sealed class`:
- `Screen.Login` → `"login"`
- `Screen.Dashboard` → `"dashboard"`
- `Screen.Historial` → `"historial"`
- `Screen.Alerta` → `"alerta"`

**`navigation/NavGraph.kt`** — Configura `NavHost` con `rememberNavController`:
- `login` → `LoginScreen`; al iniciar sesión navega a `dashboard` eliminando `login` del backstack.
- `dashboard` → `DashboardScreen` con callbacks a `historial` y `alerta`.
- `historial` → `HistorialScreen` con botón de retroceso.
- `alerta` → `PantallaEnConstruccion` (placeholder).

#### 3.1.3 Pantallas

**`LoginScreen.kt`** — Formulario de inicio de sesión con:
- Campo de email con validación de formato (`@` requerido).
- Campo de contraseña con visibilidad toggle y mínimo 6 caracteres.
- Botón "ENTRAR" con estado de carga (`CircularProgressIndicator`).
- Llamada a `onLoginSuccess()` al validar correctamente.

**`ui/screens/DashboardScreen.kt`** — Pantalla principal de monitoreo:
- Recibe `fc`, `pasos`, `historial` desde `DashboardViewModel` mediante `collectAsState()`.
- Muestra dos `TarjetaDato` (FC y pasos) y lista de `FilaHistorial` con las últimas lecturas.
- `FloatingActionButton` rojo para emergencias que despliega `AlertaScreen`.
- Snackbar de confirmación al enviar alerta.

**`ui/screens/HistorialScreen.kt`** — Historial completo de lecturas:
- `TopAppBar` con icono de retroceso.
- Estado vacío: mensaje "No hay lecturas aun. Espera a que el reloj envie datos."
- `LazyColumn` con `FilaHistorial` para cada lectura y contador de total.

**`ui/screens/AlertaScreen.kt`** — Diálogo de confirmación de alerta de emergencia:
- Icono de advertencia, frecuencia cardíaca actual, botones "CONFIRMAR ALERTA" y "Cancelar".
- Estado de carga en botón de confirmación.

#### 3.1.4 Componentes UI Reutilizables

**`ui/components/TarjetaDato.kt`** — Tarjeta tipo `ElevatedCard` que muestra una métrica (valor grande, unidad, etiqueta). Usada para frecuencia cardíaca y pasos. El color del valor se configura por parámetro.

**`ui/components/FilaHistorial.kt`** — Fila de lista con:
- Valor de BPM (color rojo si anormal, normal si está en 60–100).
- Marca de tiempo.
- Divisor entre filas.

#### 3.1.5 Tema

**`ui/theme/Color.kt`** — Paleta de colores personalizada para esquemas claro y oscuro (azul primario, rojo para errores, etc.).

**`ui/theme/Theme.kt`** — `SmartHealthMonitorTheme` que selecciona automáticamente el esquema claro/oscuro según la configuración del sistema y aplica `MaterialTheme` con colores y tipografía personalizados.

**`ui/theme/Type.kt`** — Definición de `Typography` con estilo `bodyLarge` personalizado.

#### 3.1.6 ViewModel

**`ui/viewmodel/DashboardViewModel.kt`** — ViewModel central que:
- Expone `fc: StateFlow<Int>` — frecuencia cardíaca desde `SmartHealthRepository.fcFlow`, con fallback a `MockData.fcActual` (78) si es 0.
- Expone `pasos: StateFlow<Int>` — pasos desde `SmartHealthRepository.pasosFlow`, con fallback a `MockData.pasosActual` (4250) si es 0.
- Expone `historial: StateFlow<List<LecturaFC>>` — historial desde Room vía `SmartHealthRepository.obtenerHistorial()`.
- Usa `SharingStarted.WhileSubscribed(5_000)` y `stateIn`.

### 3.2 Capa de Datos

#### 3.2.1 Modelos

**`data/models/SmartHealthData.kt`** — Define:
- `data class LecturaFC` — Modelo de datos para lectura de frecuencia cardíaca (`valorBpm`, `hora`, `esNormal`).
- `object MockData` — Datos mock para desarrollo: 7 lecturas históricas, `fcActual = 78`, `pasosActual = 4250`.

#### 3.2.2 Repositorio

**`data/models/SmartHealthRepository.kt`** — Singleton repositorio central que:
- Expone `fcFlow` y `pasosFlow` como `StateFlow<Int>`.
- Mantiene referencia al DAO de Room.
- `actualizarFC(bpm)` — Actualiza el flujo y persiste en Room mediante `LecturaFCDao.insertar()`.
- `actualizarPasos(pasos)` — Actualiza el flujo de pasos.
- `obtenerHistorial()` — Retorna `Flow<List<LecturaFC>>` desde Room.
- `class SmartHealthApp : Application()` — Inicializa el repositorio en `onCreate`.

#### 3.2.3 Base de Datos Room

**`data/models/db/SmartHealthDB.kt`** — Base de datos Room versión 1 con entidad `LecturaFC`. Singleton thread-safe mediante `getDatabase(context)`.

**`data/models/db/LecturaFC.kt`** — Entidad Room para tabla `lecturas_fc`:
- `id: Int` (autogenerado, PK).
- `valorBpm: Int`.
- `timestamp: Long` (default `System.currentTimeMillis()`).
- `hora: String` (formateado "HH:mm").
- `esNormal: Boolean` (true si BPM entre 60–100).

**`data/models/db/LecturaFCDao.kt`** — DAO con operaciones:
- `insertar(lectura)` — Insertar con `OnConflictStrategy.REPLACE`.
- `obtenerUltimas(): Flow<List<LecturaFC>>` — Últimas 50 lecturas ordenadas descendente (reactivo).
- `contarRegistros()` — Conteo total.
- `limpiarViejos(limite)` — Eliminar registros anteriores a un timestamp.

#### 3.2.4 Wearable Listener Service

**`data/models/WearListenerService.kt`** — Servicio que extiende `WearableListenerService`:
- Escucha mensajes entrantes del reloj en los paths:
  - `/smarthealthmonitor/fc` — Recibe BPM y llama a `SmartHealthRepository.actualizarFC()`.
  - `/smarthealthmonitor/pasos` — Recibe pasos y llama a `SmartHealthRepository.actualizarPasos()`.
- Usa `CoroutineScope(SupervisorJob() + Dispatchers.Main)` para operaciones asíncronas.

---

## 4. Componentes del Módulo `wear` (Smartwatch)

### 4.1 Capa de Presentación

#### 4.1.1 `WearMainActivity.kt`
**Ruta:** `wear/.../presentation/WearMainActivity.kt`  
**Propósito:** Actividad principal del smartwatch:
- Solicita permisos `BODY_SENSORS` y `ACTIVITY_RECOGNITION` mediante `ActivityResultContracts.RequestMultiplePermissions()`.
- Al conceder permisos, llama a `HealthDataService.registrar(applicationContext)` para iniciar la escucha del sensor cardíaco.
- Renderiza `WearApp("Android")` dentro de `SmartHealthWearTheme`, que muestra un `Scaffold` con `TimeText`, `PositionIndicator` y `ScalingLazyColumn` con chips placeholder.

#### 4.1.2 `WearDashboardViewModel.kt`
Expone `fc: StateFlow<Int>` desde `SmartHealthRepository.fcFlow` del módulo wear, con fallback a 72 si es 0. Utiliza `SharingStarted.WhileSubscribed(5_000)`.

#### 4.1.3 `WearDashboardScreen.kt`
Pantalla principal del smartwatch optimizada para pantallas circulares:
- `Scaffold` con `TimeText` (scroll away), `PositionIndicator`.
- `ScalingLazyColumn` que contiene:
  - `WearFCCard` con la frecuencia cardíaca actual.
  - `Chip` rojo "Alerta" con icono de advertencia.

#### 4.1.4 `WearFCCard.kt`
Componente card reutilizable que muestra:
- Emoji de corazón.
- Valor de BPM en grande (estilo `display3`).
- Etiqueta "bpm" (estilo `caption3`).
- Color primario si FC está en 60–100 (normal), color de error si está fuera de rango.

#### 4.1.5 Tema Wear

**`theme/WearTheme.kt`** — `SmartHealthWearTheme` aplica `MaterialTheme` de Wear Material3.

**`theme/Theme.kt`** — `SmarthealthmonitorTheme` (tema alternativo, también basado en Wear Material3).

### 4.2 Capa de Datos

#### 4.2.1 `SmartHealthRepository.kt` (Wear)
**Ruta:** `wear/.../data/SmartHealthRepository.kt`  
Repositorio local del smartwatch (sin Room):
- Expone `fcFlow` y `pasosFlow` como `StateFlow<Int>`.
- `actualizarFC(bpm)` y `actualizarPasos(pasos)` actualizan los flujos locales.

#### 4.2.2 `HealthDataService.kt`
**Ruta:** `wear/.../presentation/data/models/HealthDataService.kt`  
Servicio que extiende `PassiveListenerService` de Android Health Services:
- `onNewDataPointsReceived(dataPoints)` — Procesa data points de tipo `HEART_RATE_BPM`, extrae el último valor y lo envía al teléfono mediante `WearDataSender.enviarFC(bpm)` usando `runBlocking(Dispatchers.IO)`.
- `registrar(context)` — Método companion que configura el `PassiveListenerConfig` con `DataType.HEART_RATE_BPM` y registra el servicio mediante `passiveMonitoringClient.setPassiveListenerServiceAsync()`.

#### 4.2.3 `WearDataSender.kt`
**Ruta:** `wear/.../presentation/data/models/WearDataSender.kt`  
Clase responsable de la transmisión al teléfono:
- `enviarFC(bpm)` — Envía mensaje por path `/smarthealthmonitor/fc`.
- `enviarPasos(pasos)` — Envía mensaje por path `/smarthealthmonitor/pasos`.
- `enviarMensaje(path, data)` — Método privado que:
  1. Obtiene nodos con capacidad `"health_monitor_receiver"` mediante `CapabilityClient`.
  2. Fallback a nodos conectados si no hay nodos de capacidad.
  3. Envía mensaje mediante `Wearable.getMessageClient(context).sendMessage()`.
  4. Manejo completo de errores con logging.

---

## 5. Ciclo Completo de Funcionamiento

```
1. [Wear OS] Usuario concede permisos BODY_SENSORS y ACTIVITY_RECOGNITION.
2. [Wear OS] HealthDataService se registra como PassiveListenerService.
3. [Wear OS] El sensor de frecuencia cardíaca emite datos → HealthDataService.onNewDataPointsReceived().
4. [Wear OS] WearDataSender.enviarFC(bpm) empaqueta el BPM y lo envía por Wearable Data Layer.
5. [Teléfono] WearListenerService.onMessageReceived() recibe el mensaje en /smarthealthmonitor/fc.
6. [Teléfono] SmartHealthRepository.actualizarFC(bpm) actualiza el StateFlow y persiste en Room.
7. [Teléfono] DashboardViewModel recibe el nuevo valor mediante el StateFlow.
8. [Teléfono] DashboardScreen se recompone automáticamente mostrando el nuevo BPM.
9. [Teléfono] HistorialScreen muestra todas las lecturas almacenadas en Room.
10. [Teléfono] AlertaScreen permite al usuario enviar una alerta de emergencia.
```

---

## 6. Correcciones Aplicadas

Durante el análisis y corrección del proyecto se detectaron y solucionaron los siguientes problemas que impedían la compilación. Ambos módulos compilan exitosamente:

| Módulo | Estado |
|--------|--------|
| `:wear:assembleDebug` | ✅ BUILD SUCCESSFUL |
| `:app:assembleDebug` | ✅ BUILD SUCCESSFUL |

### 6.1. Falta de Configuración de AndroidX en `gradle.properties`
El entorno estaba incluyendo dependencias de AndroidX en el `classpath` de ejecución, pero la bandera global para habilitarlas no estaba encendida.
*   **Detalle del error:** `Configuration :app:debugRuntimeClasspath contains AndroidX dependencies, but the android.useAndroidX property is not enabled...`
*   **Solución (Aplicada):** Se agregó `android.useAndroidX=true` en el archivo `gradle.properties`.

### 6.2. Importaciones Faltantes y Ambiguas en Compose (Módulo Wear)
En el archivo `WearDashboardScreen.kt` existían numerosos errores de resolución de referencias:
*   **Conflicto de Scaffold:** Se importaba el `Scaffold` de Material 3 (`androidx.compose.material3.Scaffold`) junto con el de Wear OS (`androidx.wear.compose.material.Scaffold`), generando ambigüedad.
*   **Referencias no resueltas:** Faltaban imports de `Modifier`, `TimeText`, `PositionIndicator`, `ScalingLazyColumn`, `Text`, `Chip`, `ChipDefaults`, `MaterialTheme`, `fillMaxSize`, `fillMaxWidth`, `scrollAway`.
*   **Problema con `viewModel()` y `collectAsState():** Faltaban `import androidx.compose.runtime.getValue` e `import androidx.lifecycle.viewmodel.compose.viewModel`.
*   **`WearFCCard.kt` estaba casi vacío** — solo contenía `import androidx.compose.runtime.Composable` y usaba símbolos sin importar. Además usaba `MaterialTheme.colors.onSurfaceVariant` que no existe en Wear Material 1.x.
*   **Solución (Aplicada):** Se reescribieron ambos archivos con los imports correctos y se corrigió `onSurfaceVariant` por `onBackground`.

### 6.3. Desajuste en el Application ID (Problema de Sincronización)
Error crítico de configuración que impedía la comunicación entre el reloj y el móvil:
*   En `app/build.gradle.kts`, el `namespace` y `applicationId` es `"mx.utng.cala.smarthealthmonitor"`.
*   En `wear/build.gradle.kts`, el `applicationId` estaba configurado como `"mx.utng.cala.smarthealth"` (le faltaba "monitor").
*   **Consecuencia:** La API de *Wearable Data Layer* requiere el mismo `applicationId` en ambos módulos.
*   **Solución (Aplicada):** Se cambió `applicationId` en `wear/build.gradle.kts` a `"mx.utng.cala.smarthealthmonitor"`.

### 6.4. Versión Inestable de Room
La dependencia `roomVersion = "2.7.0-alpha11"` presentaba bugs con KSP y la generación de código del DAO `LecturaFCDao`.
*   **Solución (Aplicada):** Se cambió a `val roomVersion = "2.6.1"` (versión estable).

### 6.5. Versiones Incorrectas de Wear Compose y Horologist
Las versiones declaradas en `gradle/libs.versions.toml` causaban errores de artefactos inexistentes.
*   **Problema:** El artefacto `compose-ui-tooling-preview` no existe en `1.5.0-alpha09`, y `horologist 0.7.15` era incompatible.
*   **Solución (Aplicada):**
    *   `wearCompose`: `1.5.0-alpha09` → `1.4.1`
    *   `wearComposeMaterial3`: `1.0.0-alpha32` → `1.0.0-alpha30`
    *   `horologist`: `0.7.15` → `0.6.17`
    *   Se eliminó la entrada `androidx-wear-compose-ui-tooling-preview` (no existe en v1.4.x).

### 6.6. Inconsistencia de JVM Target
Con Kotlin 2.1.0, el compilador selecciona JVM 21 por defecto, pero `compileOptions` tenía Java 11. AGP 8.x detecta esta inconsistencia como error fatal.
*   **Solución (Aplicada):** Se agregó `kotlinOptions { jvmTarget = "11" }` en `app/build.gradle.kts` y `wear/build.gradle.kts`.

### 6.7. JDK sin `jlink` en `gradle.properties`
Gradle usaba el JRE del plugin de VS Code (sin `jlink.exe`). AGP 8.x necesita `jlink` para construir la imagen JDK del sistema durante la compilación.
*   **Solución (Aplicada):** Se configuró `org.gradle.java.home=C:/Program Files/Android/Android Studio/jbr` en `gradle.properties`.

---

## 7. Estado Actual

El proyecto compila exitosamente en ambos módulos. Los únicos aspectos a considerar a futuro (no críticos):
1.  `kotlinOptions` está marcado como deprecated → migración futura a `compilerOptions` (no urgente).
2.  `SDK XML version 4` → diferencia de versiones entre Android Studio y herramientas de línea de comandos (cosmético).
