package mx.utng.smarthealthmonitor.cala.tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.cala.smarthealthmonitor.data.models.SmartHealthRepository

/**
 * VistaModelo para la interfaz de televisión (Android TV) usando Jetpack Compose.
 * Expone un flujo de estado unificado que reacciona a los cambios en el historial local de Room
 * y a los datos de la frecuencia cardíaca en tiempo real.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class TvVistaModelo : ViewModel() {

    private val _estado = MutableStateFlow(EstadoUiTv())
    val estado: StateFlow<EstadoUiTv> = _estado.asStateFlow()

    init {
        observarHistorial()
        observarFrecuenciaCardiacaActual()
    }

    private fun observarHistorial() {
        viewModelScope.launch {
            SmartHealthRepository.obtenerHistorial()
                .catch { errorCapturado ->
                    _estado.update {
                        it.copy(
                            error = errorCapturado.message,
                            estaCargando = false
                        )
                    }
                }
                .collect { listaLecturas ->
                    _estado.update {
                        it.copy(
                            lecturas = listaLecturas,
                            estaCargando = false
                        )
                    }
                }
        }
    }

    private fun observarFrecuenciaCardiacaActual() {
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { valorBpm ->
                _estado.update {
                    it.copy(frecuenciaCardiacaActual = valorBpm)
                }
            }
        }
    }
}
