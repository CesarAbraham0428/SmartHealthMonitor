package mx.utng.smarthealthmonitor.cala.tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.cala.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC

/**
 * VistaModelo para la interfaz de televisión (Android TV).
 * Expone flujos de estado para la frecuencia cardíaca en tiempo real,
 * los pasos acumulados y el historial almacenado en la base de datos de Room.
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class TvVistaModelo : ViewModel() {
    
    // Frecuencia cardíaca actual del wearable (o 0 si no hay datos)
    val frecuenciaCardiaca: StateFlow<Int> = SmartHealthRepository.fcFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Pasos acumulados actuales (o 0 si no hay datos)
    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Historial de lecturas de frecuencia cardíaca desde Room DAO
    val historial: StateFlow<List<LecturaFC>> = SmartHealthRepository.obtenerHistorial()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
