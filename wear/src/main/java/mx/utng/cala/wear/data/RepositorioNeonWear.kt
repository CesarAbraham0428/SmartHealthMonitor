package mx.utng.cala.wear.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.utng.cala.wear.data.remote.ClienteNeon
import mx.utng.cala.wear.data.remote.PeticionNeon
import mx.utng.cala.wear.data.remote.DtoLecturaFc
import java.text.SimpleDateFormat
import java.util.*

class RepositorioNeonWear {
    
    suspend fun publicarLectura(bpm: Int, estado: String) = withContext(Dispatchers.IO) {
        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora) VALUES (\$1, \$2, \$3, \$4)",
                params = listOf(bpm.toString(), estado, "wear", hora)
            )
        )
        android.util.Log.d("RELOJ_DB", "Frecuencia cardíaca enviada a Neon: $bpm bpm")
    }

    suspend fun obtenerUltimasLecturas(): List<DtoLecturaFc> = withContext(Dispatchers.IO) {
        val respuesta = ClienteNeon.servicioApi.ejecutarConsulta(
            autorizacion = ClienteNeon.CABECERA_AUTORIZACION,
            cadenaConexion = ClienteNeon.CADENA_CONEXION,
            peticion = PeticionNeon(
                query = "SELECT id, bpm, estado, dispositivo, hora FROM lecturas_fc WHERE dispositivo='wear' ORDER BY created_at DESC LIMIT 5"
            )
        )
        respuesta.rows
    }
}
