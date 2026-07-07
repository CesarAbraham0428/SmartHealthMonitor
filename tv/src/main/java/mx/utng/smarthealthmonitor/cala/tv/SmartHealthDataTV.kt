package mx.utng.smarthealthmonitor.cala.tv

/**
 * Representa una lectura de frecuencia cardíaca (FC) para la aplicación de TV.
 * Traducido al español de acuerdo con las reglas del usuario.
 */
data class LecturaFC(
    val id: Int,
    val valorBpm: Int,
    val hora: String,
    val esNormal: Boolean = valorBpm in 60..100
)

/**
 * Datos simulados para desarrollo en la TV sin requerir conexión en tiempo real.
 * Traducido al español de acuerdo con las reglas del usuario.
 */
object DatosSimulados {
    val historialFC = listOf(
        LecturaFC(1, 78, "11:00"),
        LecturaFC(2, 82, "10:30"),
        LecturaFC(3, 76, "10:00"),
        LecturaFC(4, 95, "09:30", false),  // fuera de rango
        LecturaFC(5, 71, "09:00"),
        LecturaFC(6, 80, "08:30"),
        LecturaFC(7, 74, "08:00")
    )
    var fcActual = 78
    var pasosActual = 4250
}
