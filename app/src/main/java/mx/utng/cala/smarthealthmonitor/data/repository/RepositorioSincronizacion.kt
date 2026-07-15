package mx.utng.cala.smarthealthmonitor.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFCDao
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC
import mx.utng.cala.smarthealthmonitor.data.remote.ClienteNeon
import mx.utng.cala.smarthealthmonitor.data.remote.PeticionNeon

class RepositorioSincronizacion(private val lectorDao: LecturaFCDao) {

    // --- LECTURA LOCAL (Offline-First) ---
    fun observarHistorial(): Flow<List<LecturaFC>> = lectorDao.obtenerTodas()

    // --- ESCRITURA LOCAL + SYNC ---
    suspend fun insertarLectura(lectura: LecturaFC) {
        val idGenerado = lectorDao.insertar(lectura)
        try {
            sincronizarHaciaNeon(lectura)
            lectorDao.marcarSincronizado(idGenerado)
        } catch (e: Exception) {
            android.util.Log.w("SYNC", "Pendiente de sync: ${e.message}")
        }
    }

    // --- PUSH: Room -> Neon ---
    private suspend fun sincronizarHaciaNeon(lectura: LecturaFC) = withContext(Dispatchers.IO) {
        ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora) VALUES ($1, $2, $3, $4)",
                params = listOf(lectura.bpm.toString(), lectura.estado, lectura.dispositivo, lectura.hora)
            )
        )
    }

    // --- PULL: Neon -> Room ---
    suspend fun sincronizarDesdeNeon(limite: Int = 50) = withContext(Dispatchers.IO) {
        try {
            val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
                autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
                cadenaConexion = ClienteNeon.CADENA_CONEXION,
                peticion = PeticionNeon(
                    query = "SELECT id, bpm, estado, dispositivo, hora FROM lecturas_fc ORDER BY created_at DESC LIMIT $1",
                    params = listOf(limite.toString())
                )
            )
            
            respuesta.rows.forEach { dto ->
                lectorDao.upsert(
                    LecturaFC(
                        id = dto.id,
                        bpm = dto.bpm,
                        estado = dto.estado,
                        dispositivo = dto.dispositivo,
                        hora = dto.hora,
                        sincronizado = true
                    )
                )
            }
            android.util.Log.d("SYNC", "${respuesta.rowCount} registros descargados de Neon")
        } catch (e: Exception) {
            android.util.Log.e("SYNC", "Error al sincronizar desde Neon: ${e.message}")
        }
    }

    suspend fun enviarPendientes() = withContext(Dispatchers.IO) {
        val pendientes = lectorDao.obtainNoSincronizados()
        pendientes.forEach { lectura ->
            try {
                sincronizarHaciaNeon(lectura)
                lectorDao.marcarSincronizado(lectura.id.toLong())
                android.util.Log.d("SYNC", "Sincronizado pendiente id ${lectura.id}")
            } catch (e: Exception) {
                android.util.Log.w("SYNC", "Aún sin internet: ${e.message}")
            }
        }
    }
}
