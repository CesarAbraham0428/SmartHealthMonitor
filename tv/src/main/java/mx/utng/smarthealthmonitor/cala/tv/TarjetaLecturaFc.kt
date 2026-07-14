package mx.utng.smarthealthmonitor.cala.tv

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC

/**
 * Composable que representa una tarjeta de lectura de Frecuencia Cardíaca para Android TV.
 * Utiliza la superficie interactiva (Surface) de Compose for TV, la cual maneja
 * de forma nativa el foco del D-pad del control remoto mediante cambios de color.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
@Composable
fun TarjetaLecturaFc(
    lectura: LecturaFC,
    alHacerClic: () -> Unit,
    modificador: Modifier = Modifier
) {
    // Si la lectura no es normal, usaremos un color rojo de advertencia cuando tenga foco,
    // y un tono rojo/borra de vino cuando no tenga foco para indicar anomalías.
    val esNormal = lectura.estado == "Normal" || lectura.estado.contains("Normal", ignoreCase = true)
    val colorSinFoco = if (esNormal) Color(0xFF1565C0) else Color(0xFF8B0000)
    val colorConFoco = if (esNormal) Color(0xFF42A5F5) else Color(0xFFEF5350)
    val colorPresionado = if (esNormal) Color(0xFF0D47A1) else Color(0xFFB71C1C)

    Surface(
        onClick = alHacerClic,
        modifier = modificador.width(200.dp).height(120.dp),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = colorSinFoco,
            focusedContainerColor = colorConFoco,
            pressedContainerColor = colorPresionado
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${lectura.bpm} bpm",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Column {
                Text(
                    text = lectura.estado,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = lectura.hora,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
