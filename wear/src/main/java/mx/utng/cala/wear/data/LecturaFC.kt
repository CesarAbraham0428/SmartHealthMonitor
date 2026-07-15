package mx.utng.cala.wear.data

/**
 * Modelo de datos simple para la frecuencia cardíaca en el Wear.
 */
data class LecturaFC(
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String = "wear",
    val hora: String
) {
    val valorBpm: Int get() = bpm
    val esNormal: Boolean get() = estado == "Normal" || estado.contains("Normal", ignoreCase = true)
}
