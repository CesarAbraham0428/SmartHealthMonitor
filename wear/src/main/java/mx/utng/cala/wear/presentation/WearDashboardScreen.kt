package mx.utng.cala.wear.presentation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Scaffold

@Composable
fun WearDashboardScreen(
    onAlertClick: () -> Unit = {},
    viewModel: WearDashboardViewModel = viewModel()
) {
    val fc by viewModel.fc.collectAsState()
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = {
            // La hora desaparece al hacer scroll
            TimeText(modifier = Modifier.scrollAway(listState))
        },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        ScalingLazyColumn(
            state    = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Item 1: Card de FC
            item {
                WearFCCard(
                    fc = fc,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            // Item 2: Chip de Alerta
            item {
                Chip(
                    label  = { Text("⚠ Alerta") },
                    onClick = onAlertClick,
                    colors = ChipDefaults.primaryChipColors(
                        backgroundColor = MaterialTheme.colors.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

