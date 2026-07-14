package mx.utng.cala.smarthealthmonitor.data.models


import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFCDao
import mx.utng.cala.smarthealthmonitor.data.models.db.SmartHealthDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.mqtt.ServicioMqttAplicacion


/**
 * Repositorio singleton que centraliza los datos de salud.
 * El WearListenerService escribe aquí.
 * El ViewModel lee de aquí.
 */

object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow.asStateFlow()

    private var dao: LecturaFCDao? = null

    fun init(context: Context) {
        dao = SmartHealthDB.getDatabase(context).lecturaDao()
    }

    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        val horaActual = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val estadoActual = when {
            bpm < 60 -> "FC Baja"
            bpm > 100 -> "FC Alta"
            else -> "Normal"
        }
        // Persistir en Room automáticamente
        dao?.insertar(
            LecturaFC(
                bpm = bpm,
                estado = estadoActual,
                dispositivo = "app",
                hora = horaActual,
                sincronizado = false
            )
        )
    }

    fun actualizarPasos(pasos: Int) {
        _pasosFlow.value = pasos
    }

    // Flow del historial desde Room
    fun obtenerHistorial(): Flow<List<LecturaFC>> =
        dao?.obtenerUltimas() ?: emptyFlow()
}
// En Application.kt (crear si no existe):
class SmartHealthApp : Application() {
    lateinit var servicioMqtt: ServicioMqttAplicacion

    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)  // inicializar Room
        
        // Programar sincronización periódica con Neon PostgreSQL
        mx.utng.cala.smarthealthmonitor.data.sync.TrabajadorSincronizacionNeon.programar(this)

        servicioMqtt = ServicioMqttAplicacion(this) { bpm ->
            CoroutineScope(Dispatchers.IO).launch {
                SmartHealthRepository.actualizarFC(bpm)
            }
        }
        // Conectar MQTT en hilo IO para no bloquear el hilo principal (evita ANR)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                servicioMqtt.conectar()
            } catch (e: Exception) {
                android.util.Log.e("SmartHealthApp", "Error al conectar MQTT: ${e.message}")
            }
        }
    }
}
