# Documentación del Proyecto: Smart Health Monitor

## 1. Arquitectura y Funcionamiento del Proyecto

El proyecto **Smart Health Monitor** es una aplicación de salud diseñada para funcionar de forma complementaria entre un dispositivo móvil (App) y un reloj inteligente (Wear OS). Utiliza una arquitectura moderna basada en Kotlin y Jetpack Compose.

### Estructura de Módulos
El proyecto está dividido en dos módulos principales integrados mediante Gradle (`settings.gradle.kts`):

*   **Módulo `app` (Dispositivo Móvil):**
    *   **UI:** Desarrollada completamente con Jetpack Compose. Cuenta con varias pantallas (`DashboardScreen`, `HistorialScreen`, `LoginScreen`, `AlertaScreen`) gestionadas a través de un sistema de navegación (`NavGraph.kt`).
    *   **Base de Datos (Local):** Utiliza **Room** para persistir el historial de lecturas de la frecuencia cardíaca (`LecturaFC`, `LecturaFCDao`, `SmartHealthDB`).
    *   **Arquitectura:** Sigue el patrón MVVM. Los ViewModels (`DashboardViewModel`) exponen el estado a la interfaz de usuario mediante `StateFlow`.
    *   **Sincronización:** Emplea la API `Wearable Data Layer` de Google Play Services (`WearListenerService`) para recibir los datos transmitidos desde el smartwatch.

*   **Módulo `wear` (Smartwatch):**
    *   **UI:** Construida con Wear OS Compose y componentes de Horologist (`WearDashboardScreen`, `WearFCCard`).
    *   **Captura de Datos:** Utiliza la API de `Health Services` (`androidx.health:health-services-client`) para leer la frecuencia cardíaca del sensor del reloj.
    *   **Transmisión:** Los datos capturados se empaquetan y envían al dispositivo móvil asociado mediante el `DataClient` o `MessageClient` del Wearable API.

---

## 2. Razones de los Errores de Compilación

Durante el análisis del código fuente y los archivos de configuración de Gradle, se han detectado varios problemas que causan fallos de compilación. A continuación, se detallan las razones:

### 2.1. Falta de Configuración de AndroidX en `gradle.properties`
El entorno estaba incluyendo dependencias de AndroidX en el `classpath` de ejecución, pero la bandera global para habilitarlas no estaba encendida.
*   **Detalle del error:** `Configuration :app:debugRuntimeClasspath contains AndroidX dependencies, but the android.useAndroidX property is not enabled...`
*   **Solución (Aplicada):** Se agregó `android.useAndroidX=true` en el archivo `gradle.properties`.

### 2.2. Importaciones Faltantes y Ambiguas en Compose (Módulo Wear)
En el archivo `WearDashboardScreen.kt`, existen numerosos errores de resolución de referencias:
*   **Conflicto de Scaffold:** Se está importando el `Scaffold` tradicional de Material 3 (`androidx.compose.material3.Scaffold`) junto con el `Scaffold` específico de Wear OS (`androidx.wear.compose.material.Scaffold`), lo que genera ambigüedad.
*   **Referencias no resueltas:** Funciones y modificadores fundamentales como `Modifier`, `TimeText`, `PositionIndicator`, `ScalingLazyColumn`, `Text` y `Chip` no tienen su respectivo `import`.
*   **Problema con `viewModel()` y `collectAsState()`:** Falta la importación del delegado `getValue` (`import androidx.compose.runtime.getValue`) para desenvolver el estado, y falta importar `androidx.lifecycle.viewmodel.compose.viewModel`.

### 2.3. Desajuste en el Application ID (Problema de Sincronización)
Aunque no sea estrictamente un error de sintaxis que frene la compilación del código Kotlin, es un error crítico de configuración que impide la comunicación entre el reloj y el móvil:
*   En `app/build.gradle.kts`, el `namespace` y `applicationId` es `"mx.utng.cala.smarthealthmonitor"`.
*   En `wear/build.gradle.kts`, el `applicationId` está configurado como `"mx.utng.cala.smarthealth"` (le falta "monitor").
*   **Consecuencia:** La API de *Wearable Data Layer* requiere que ambos módulos (app y wear) tengan el **mismo `applicationId`** y estén firmados con la misma clave para poder comunicarse. Al ser diferentes, la sincronización de datos nunca funcionará.

### 2.4. Posibles Incompatibilidades de Versiones (Room / KSP)
El proyecto utiliza Kotlin con versiones recientes. Sin embargo, en el bloque de dependencias se declara `roomVersion = "2.7.0-alpha11"`. Las versiones "alpha" pueden presentar bugs internos con KSP y la generación de código (por ejemplo, con el DAO `LecturaFCDao`), lo cual detiene el proceso de construcción en la etapa de anotaciones si las dependencias del compilador no empatan perfectamente con la versión del plugin Kotlin.

---

## 3. Próximos Pasos Recomendados

Para lograr compilar y ejecutar el proyecto con éxito, se recomienda:
1.  **Corregir los imports** en `WearDashboardScreen.kt` y otras pantallas UI asegurándose de usar los componentes de la librería correcta (Wear Material vs Material 3).
2.  **Igualar el `applicationId`** en `wear/build.gradle.kts` para que coincida exactamente con el de la app (`mx.utng.cala.smarthealthmonitor`).
3.  Revisar que todas las declaraciones de estado `by viewmodel.state.collectAsState()` tengan `import androidx.compose.runtime.getValue`.
