package mx.utng.smarthealthmonitor.cala.tv

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.cala.smarthealthmonitor.data.models.SmartHealthRepository
import mx.utng.smarthealthmonitor.tv.mqtt.SuscriptorMqttTv

/**
 * VistaModelo para la interfaz de televisión (Android TV) usando Jetpack Compose.
 * Expone un flujo de estado unificado que reacciona a los cambios en el historial local de Room
 * y a los datos de la frecuencia cardíaca en tiempo real.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class TvVistaModelo(application: Application) : AndroidViewModel(application) {

    private val repositorioNeon = mx.utng.smarthealthmonitor.cala.tv.data.RepositorioNeonTv()
    private val _estado = MutableStateFlow(EstadoUiTv())
    val estado: StateFlow<EstadoUiTv> = _estado.asStateFlow()

    private val suscriptorMqtt = SuscriptorMqttTv(application) { mensajeTv ->
        // 1. Guardar la lectura recibida vía MQTT en el repositorio local de la TV (Room)
        viewModelScope.launch {
            SmartHealthRepository.actualizarFC(mensajeTv.bpm)
        }

        // 2. Actualizar el estado de la UI reactiva con los valores recibidos
        _estado.update {
            it.copy(
                frecuenciaCardiacaActual = mensajeTv.bpm,
                frecuenciaCardiacaEstado = mensajeTv.estado,
                ultimaHoraFC = mensajeTv.hora
            )
        }
    }

    init {
        cargarDatos()
        observarFrecuenciaCardiacaActual()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { suscriptorMqtt.conectar() }
                .onFailure { android.util.Log.w("TV_MQTT", "No se pudo conectar MQTT: ${it.message}") }
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _estado.update { it.copy(estaCargando = true) }
            try {
                val remotosHistorial = repositorioNeon.obtenerHistorialCompleto(50)
                val remotosStats = repositorioNeon.obtenerEstadisticas()
                val remotosAlertas = repositorioNeon.obtenerAlertasCriticas()
                
                _estado.update {
                    it.copy(
                        lecturas = remotosHistorial.map { dto -> dto.aLecturaFc() },
                        estadisticas = remotosStats.map { dto -> dto.aLecturaFc() },
                        alertas = remotosAlertas.map { dto -> dto.aLecturaFc() },
                        estaCargando = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _estado.update { it.copy(error = e.message, estaCargando = false) }
            }
        }
    }

    fun refrescar() = cargarDatos()

    private fun observarFrecuenciaCardiacaActual() {
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { valorBpm ->
                _estado.update {
                    it.copy(frecuenciaCardiacaActual = valorBpm)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        suscriptorMqtt.desconectar()
    }
}
