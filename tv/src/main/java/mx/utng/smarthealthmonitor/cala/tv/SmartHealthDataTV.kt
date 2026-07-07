package mx.utng.smarthealthmonitor.cala.tv

import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC

/**
 * Datos simulados para desarrollo en la TV sin requerir conexión en tiempo real.
 * Traducido al español de acuerdo con las reglas del usuario.
 */
object DatosSimulados {
    val historialFC = listOf(
        LecturaFC(1, 78, esNormal = true),
        LecturaFC(2, 82, esNormal = true),
        LecturaFC(3, 76, esNormal = true),
        LecturaFC(4, 95, esNormal = false),  // fuera de rango
        LecturaFC(5, 71, esNormal = true),
        LecturaFC(6, 80, esNormal = true),
        LecturaFC(7, 74, esNormal = true)
    )
    var fcActual = 78
    var pasosActual = 4250
}
