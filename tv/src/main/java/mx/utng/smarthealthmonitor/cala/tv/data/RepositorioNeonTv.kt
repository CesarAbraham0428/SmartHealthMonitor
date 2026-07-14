package mx.utng.smarthealthmonitor.cala.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.smarthealthmonitor.cala.tv.data.remote.ClienteNeon
import mx.utng.smarthealthmonitor.cala.tv.data.remote.PeticionNeon
import mx.utng.smarthealthmonitor.cala.tv.data.remote.DtoLecturaFc

class RepositorioNeonTv {

    suspend fun obtenerHistorialCompleto(limite: Int = 50): List<DtoLecturaFc> = withContext(Dispatchers.IO) {
        val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "SELECT id, bpm, estado, dispositivo, hora FROM lecturas_fc ORDER BY created_at DESC LIMIT $1",
                params = listOf(limite.toString())
            )
        )
        respuesta.rows
    }

    suspend fun obtenerEstadisticas(): List<DtoLecturaFc> = withContext(Dispatchers.IO) {
        val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "SELECT 0 AS id, ROUND(AVG(bpm))::int AS bpm, 'Promedio' AS estado, dispositivo, MAX(hora) AS hora FROM lecturas_fc GROUP BY dispositivo"
            )
        )
        respuesta.rows
    }

    /** Alertas de FC fuera de rango normal (últimas 24 horas) */
    suspend fun obtenerAlertasCriticas(): List<DtoLecturaFc> = withContext(Dispatchers.IO) {
        val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "SELECT id, bpm, estado, dispositivo, hora FROM lecturas_fc WHERE (bpm < 60 OR bpm > 100) AND created_at > NOW() - INTERVAL '24 hours' ORDER BY created_at DESC"
            )
        )
        respuesta.rows
    }

    /** Promedio de FC por hora del día */
    suspend fun obtenerPromedioPorHora(): List<DtoLecturaFc> = withContext(Dispatchers.IO) {
        val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "SELECT EXTRACT(HOUR FROM created_at)::int AS id, ROUND(AVG(bpm))::int AS bpm, 'Promedio Hora' AS estado, 'global' AS dispositivo, EXTRACT(HOUR FROM created_at)::text AS hora FROM lecturas_fc GROUP BY EXTRACT(HOUR FROM created_at) ORDER BY id"
            )
        )
        respuesta.rows
    }

    /** Lectura más reciente de cada dispositivo */
    suspend fun obtenerUltimaLecturaPorDispositivo(): List<DtoLecturaFc> = withContext(Dispatchers.IO) {
        val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "SELECT DISTINCT ON (dispositivo) id, bpm, estado, dispositivo, hora FROM lecturas_fc ORDER BY dispositivo, created_at DESC"
            )
        )
        respuesta.rows
    }

    /** Detección de taquicardia sostenida (>100 bpm dentro del rango de 1 hora) */
    suspend fun detectarTaquicardiaSostenida(): List<DtoLecturaFc> = withContext(Dispatchers.IO) {
        val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "SELECT 0 AS id, COUNT(*)::int AS bpm, 'Lecturas Altas' AS estado, 'Alerta' AS dispositivo, MIN(hora) AS hora FROM lecturas_fc WHERE bpm > 100 AND created_at > NOW() - INTERVAL '1 hour'"
            )
        )
        respuesta.rows
    }
}
