package mx.utng.cala.smarthealthmonitor.data.models

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFCDao
import mx.utng.cala.smarthealthmonitor.data.models.db.SmartHealthDB

/**
 * Repositorio de datos para el módulo de televisión.
 * Maneja el flujo de frecuencia cardíaca, pasos y acceso a la base de datos local Room.
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow.asStateFlow()

    private var dao: LecturaFCDao? = null

    fun init(context: Context) {
        dao = SmartHealthDB.getDatabase(context).lecturaDao()
        
        // Cargar algunos datos iniciales de simulación si la base de datos está vacía
        CoroutineScope(Dispatchers.IO).launch {
            dao?.let { lecturaDao ->
                if (lecturaDao.contarRegistros() == 0) {
                    // Datos de ejemplo para poblar la pantalla de la TV en el primer inicio
                    lecturaDao.insertar(LecturaFC(bpm = 75, estado = "Normal", dispositivo = "tv", hora = "12:00:00"))
                    lecturaDao.insertar(LecturaFC(bpm = 82, estado = "Normal", dispositivo = "tv", hora = "12:05:00"))
                    lecturaDao.insertar(LecturaFC(bpm = 99, estado = "Normal", dispositivo = "tv", hora = "12:10:00"))
                    lecturaDao.insertar(LecturaFC(bpm = 112, estado = "FC Alta", dispositivo = "tv", hora = "12:15:00"))
                    lecturaDao.insertar(LecturaFC(bpm = 68, estado = "Normal", dispositivo = "tv", hora = "12:20:00"))
                }
            }
        }
    }

    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        val horaActual = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val estadoActual = when {
            bpm < 60 -> "FC Baja"
            bpm > 100 -> "FC Alta"
            else -> "Normal"
        }
        // Guardar la lectura en la base de datos Room
        dao?.insertar(LecturaFC(bpm = bpm, estado = estadoActual, dispositivo = "tv", hora = horaActual))
    }

    fun actualizarPasos(pasos: Int) {
        _pasosFlow.value = pasos
    }

    // Obtener el flujo del historial almacenado
    fun obtenerHistorial(): Flow<List<LecturaFC>> =
        dao?.obtenerUltimas() ?: emptyFlow()
}
