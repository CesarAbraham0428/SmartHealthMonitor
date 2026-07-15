package mx.utng.smarthealthmonitor.mqtt

import kotlinx.serialization.Serializable

@Serializable
data class MensajeFc(
    val bpm: Int,
    val estado: String,
    val marcaTiempo: Long = System.currentTimeMillis()
)

@Serializable
data class MensajeTv(
    val bpm: Int,
    val estado: String,
    val hora: String
)

@Serializable
data class MensajeAlerta(
    val tipo: String,
    val bpm: Int,
    val mensaje: String
)
