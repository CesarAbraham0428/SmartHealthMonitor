package mx.utng.cala.smarthealthmonitor.ui.components

import androidx.compose.runtime.Composable
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue




@Composable
fun FilaHistorial(
    lectura: LecturaFC,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Valor FC con color según si es normal o no
        Text(
            text = "${lectura.valorBpm} bpm",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (lectura.esNormal)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.error
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = lectura.hora,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Icon(
                imageVector = if (lectura.sincronizado)
                    androidx.compose.material.icons.Icons.Default.CloudDone
                else
                    androidx.compose.material.icons.Icons.Default.CloudQueue,
                contentDescription = if (lectura.sincronizado) "Sincronizado" else "Pendiente",
                tint = if (lectura.sincronizado)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
