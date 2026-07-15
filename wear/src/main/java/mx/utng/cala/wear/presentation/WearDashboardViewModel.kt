package mx.utng.cala.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.cala.wear.data.LecturaFC
import mx.utng.cala.wear.data.SmartHealthRepository
import mx.utng.cala.wear.data.RepositorioNeonWear
import mx.utng.smarthealthmonitor.wear.mqtt.PublicadorMqttReloj

class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val publicadorMqtt = PublicadorMqttReloj(application)
    private val repositorioNeon = RepositorioNeonWear()

    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) 72 else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 72
        )

    private val _historial = MutableStateFlow<List<LecturaFC>>(emptyList())
    val historial: StateFlow<List<LecturaFC>> = _historial.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                publicadorMqtt.conectar()
            } catch (e: Exception) {
                android.util.Log.e("WEAR_MQTT", "Error al conectar MQTT: ${e.message}")
            }
        }
        cargarHistorial()
        
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { bpm ->
                if (bpm > 0) {
                    val estado = when {
                        bpm < 60 -> "FC Baja"
                        bpm > 100 -> "FC Alta"
                        else -> "Normal"
                    }
                    publicadorMqtt.publicarFrecuenciaCardiaca(bpm, estado)
                    
                    // Publicar también en Neon
                    launch(Dispatchers.IO) {
                        runCatching { repositorioNeon.publicarLectura(bpm, estado) }
                            .onSuccess {
                                // Recargar el historial tras enviar con éxito
                                cargarHistorial()
                            }
                            .onFailure { android.util.Log.w("WEAR", "Sin red al enviar a Neon: ${it.message}") }
                    }
                }
            }
        }
    }

    fun cargarHistorial() {
        viewModelScope.launch {
            runCatching {
                val remotos = repositorioNeon.obtenerUltimasLecturas()
                _historial.value = remotos.map { dto ->
                    LecturaFC(
                        id = dto.id,
                        bpm = dto.bpm,
                        estado = dto.estado,
                        dispositivo = dto.dispositivo,
                        hora = dto.hora
                    )
                }
            }.onFailure {
                android.util.Log.w("WEAR", "Error al cargar historial remoto: ${it.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        publicadorMqtt.desconectar()
    }
}
