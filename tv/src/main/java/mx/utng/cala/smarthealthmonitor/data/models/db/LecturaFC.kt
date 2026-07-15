package mx.utng.cala.smarthealthmonitor.data.models.db

import androidx.room.*

@Entity(tableName = "lecturas_fc")
data class LecturaFC(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val bpm: Int,
    val estado: String,
    val dispositivo: String = "tv", 
    val hora: String,
    @ColumnInfo(name = "sincronizado")
    val sincronizado: Boolean = false 
) {
    val valorBpm: Int get() = bpm
    val esNormal: Boolean get() = estado == "Normal" || estado.contains("Normal", ignoreCase = true)
}
