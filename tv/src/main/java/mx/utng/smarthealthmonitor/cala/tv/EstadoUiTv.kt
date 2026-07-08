package mx.utng.smarthealthmonitor.cala.tv

import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC

/**
 * Estado de la UI para la pantalla de televisión (Compose for TV).
 * Mantiene de manera inmutable los datos de lecturas, frecuencia cardíaca actual,
 * estados de carga y posibles errores.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
data class EstadoUiTv(
    val lecturas: List<LecturaFC> = emptyList(),
    val frecuenciaCardiacaActual: Int = 0,
    val estaCargando: Boolean = true,
    val error: String? = null
)
