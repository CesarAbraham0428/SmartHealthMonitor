package mx.utng.cala.smarthealthmonitor.ui.screens

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

import mx.utng.cala.smarthealthmonitor.ui.components.FilaHistorial
import mx.utng.cala.smarthealthmonitor.ui.components.TarjetaDato
import mx.utng.cala.smarthealthmonitor.ui.theme.SmartHealthMonitorTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import mx.utng.cala.smarthealthmonitor.ui.viewmodel.DashboardViewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onHistorialClick: () -> Unit = {},
    onAlertClick: () -> Unit = {}
) {
    val fc       by viewModel.fc.collectAsState()
    val pasos    by viewModel.pasos.collectAsState()
    val historial by viewModel.historial.collectAsState()

    // ── Estado del diálogo y Snackbar ──────────────────────
    var mostrarAlerta by remember { mutableStateOf(false) }
    val snackbarHost  = remember { SnackbarHostState() }
    val scope         = rememberCoroutineScope()

    // ── Diálogo condicional ────────────────────────────────
    if (mostrarAlerta) {
        AlertaScreen(
            fc          = fc,
            onDismiss   = { mostrarAlerta = false },
            onConfirmar = {
                mostrarAlerta = false
                scope.launch {
                    snackbarHost.showSnackbar(
                        message  = "✅ Alerta enviada a tus contactos de emergencia",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        )
    }

    SmartHealthMonitorTheme {
        Scaffold(
            // ── Snackbar host en el Scaffold ───────────────
            snackbarHost = { SnackbarHost(hostState = snackbarHost) },
            topBar  = {
                BarraSuperiorDashboard(titulo = "SmartHealth Monitor")
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick        = { mostrarAlerta = true },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Warning,
                        contentDescription = "Enviar alerta de emergencia",
                        tint = MaterialTheme.colorScheme.onError)
                }
            }

        ) { paddingValues ->
            // ⚠️ paddingValues OBLIGATORIO
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding        = PaddingValues(16.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp)
            ) {
                // ── Tarjeta FC ────────────────────────────
                item {
                    TarjetaDato(
                        valor      = "$fc",
                        unidad     = "bpm",
                        label      = "Frecuencia cardíaca",
                        colorValor = MaterialTheme.colorScheme.error
                    )
                }
                // ── Tarjeta Pasos ─────────────────────────
                item {
                    TarjetaDato(
                        valor      = "%,d".format(pasos),
                        unidad     = "pasos",
                        label      = "Pasos del día",
                        colorValor = MaterialTheme.colorScheme.primary
                    )
                }
                // ── Encabezado historial ──────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Historial reciente",
                            style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = onHistorialClick) {
                            Text("Ver todo")
                        }
                    }
                }
                // ── Lista del historial ───────────────────
                items(historial, key = { it.id }) { lectura ->
                    FilaHistorial(lectura = lectura)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Dashboard - Light",
    showSystemUi = true, device = "id:pixel_6")
@Preview(showBackground = true, name = "Dashboard - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardScreenPreview() {
    SmartHealthMonitorTheme {
        DashboardScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperiorDashboard(titulo: String) {
    // TopBar con CastButton
    TopAppBar(
        title = { Text(titulo) },
        actions = {
            // CastButton: AndroidView que envuelve MediaRouteButton
            AndroidView(
                factory = { contexto ->
                    MediaRouteButton(contexto).apply {
                        CastButtonFactory.setUpMediaRouteButton(contexto, this)
                    }
                },
                modifier = Modifier.size(48.dp)
            )
        }
    )
}
