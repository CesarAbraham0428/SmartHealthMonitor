package mx.utng.cala.wear.data

/**
 * Modelo de datos simple para la frecuencia cardíaca en el Wear.
 */
data class LecturaFC(
    val id: Int = 0,
    val valorBpm: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val hora: String = java.text.SimpleDateFormat(
        "HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date()),
    val esNormal: Boolean = valorBpm in 60..100
)
