package mx.utng.smarthealthmonitor.cala.tv

import android.app.Application
import mx.utng.cala.smarthealthmonitor.data.models.SmartHealthRepository

/**
 * Clase de aplicación personalizada para el módulo de Android TV.
 * Inicializa el repositorio central para acceder a los datos de salud.
 */
class AplicacionSmartHealthTV : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar el repositorio singleton con el contexto de la aplicación
        SmartHealthRepository.init(this)
    }
}
