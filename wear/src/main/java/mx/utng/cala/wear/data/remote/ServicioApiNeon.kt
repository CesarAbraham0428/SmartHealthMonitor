package mx.utng.cala.wear.data.remote

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
)

/** Interfaz Retrofit para interactuar con la API HTTP de Neon */
interface ServicioApiNeon {
    @POST("sql")
    suspend fun ejecutarConsulta(
        @Header("Authorization") autorizacion: String,
        @Header("Neon-Connection-String") cadenaConexion: String,
        @Body peticion: PeticionNeon
    ): RespuestaNeon<DtoLecturaFc>
}
