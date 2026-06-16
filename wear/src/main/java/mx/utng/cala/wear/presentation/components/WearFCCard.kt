package mx.utng.cala.wear.presentation.components

import androidx.compose.runtime.Composable

@Composable
fun WearFCCard(
    fc: Int,
    modifier: Modifier = Modifier
) {
    val colorFC = if (fc in 60..100)
        MaterialTheme.colors.primary
    else
        MaterialTheme.colors.error

    Card(
        onClick = { },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❤",
                fontSize = 20.sp
            )
            Text(
                text = "$fc",
                style = MaterialTheme.typography.display3,
                color = colorFC
            )
            Text(
                text = "bpm",
                style = MaterialTheme.typography.caption3,
                color = MaterialTheme.colors.onSurfaceVariant
            )
        }
    }
}
