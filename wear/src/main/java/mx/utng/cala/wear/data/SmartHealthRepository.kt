package mx.utng.cala.wear.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Repositorio local para el Wear. 
 * NOTA: Para recibir datos del celular, aquí deberás implementar 
 * el WearableListenerService.
 */
object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow.asStateFlow()

    fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
    }

    fun actualizarPasos(pasos: Int) {
        _pasosFlow.value = pasos
    }

    /**
     * Retorna el historial de lecturas. 
     * Por ahora retorna una lista vacía ya que no hay persistencia local en el Wear.
     */
    fun obtenerHistorial(): Flow<List<LecturaFC>> = flowOf(emptyList())
}
