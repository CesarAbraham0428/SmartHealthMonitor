package mx.utng.cala.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.utng.cala.wear.data.LecturaFC
import mx.utng.cala.wear.data.SmartHealthRepository
import mx.utng.smarthealthmonitor.wear.mqtt.PublicadorMqttReloj

class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val publicadorMqtt = PublicadorMqttReloj(application)

    // Ahora utiliza el SmartHealthRepository local del módulo Wear
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) 72 else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 72
        )
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList())

    init {
        publicadorMqtt.conectar()
        
        // Observar los cambios de ritmo cardíaco del repositorio local y publicarlos
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { bpm ->
                if (bpm > 0) {
                    val estado = when {
                        bpm < 60 -> "FC Baja"
                        bpm > 100 -> "FC Alta"
                        else -> "Normal"
                    }
                    publicadorMqtt.publicarFrecuenciaCardiaca(bpm, estado)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        publicadorMqtt.desconectar()
    }
}
