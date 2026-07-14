package mx.utng.cala.smarthealthmonitor.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.*

/** Petición genérica para la API HTTP de Neon */
@Serializable
data class PeticionNeon(
    val query: String,
    val params: List<String> = emptyList()
)

/** Respuesta de la API HTTP de Neon */
@Serializable
data class RespuestaNeon<T>(
    val rows: List<T> = emptyList(),
    val rowCount: Int = 0,
    val command: String = ""
)

/** DTO de lectura de frecuencia cardíaca (mapea la fila exacta de PostgreSQL) */
@Serializable
data class DtoLecturaFc(
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String,
    val hora: String,
    val fecha: String = "",
    val created_at: String = ""
) {
    // Función de conversión al modelo de datos local LecturaFC
    fun aLecturaFc(): mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC {
        return mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC(
            id = this.id,
            bpm = this.bpm,
            estado = this.estado,
            dispositivo = this.dispositivo,
            hora = this.hora,
            sincronizado = true
        )
    }
}

/** Interfaz Retrofit para interactuar con la API HTTP de Neon */
interface ServicioApiNeon {
    @POST("sql")
    suspend fun ejecutarConsulta(
        @Header("Authorization") autorizacion: String,
        @Header("Neon-Connection-String") cadenaConexion: String,
        @Body peticion: PeticionNeon
    ): RespuestaNeon<DtoLecturaFc>
}
