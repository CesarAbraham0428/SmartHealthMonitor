package mx.utng.smarthealthmonitor.cala.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*

/**
 * Pantalla principal de catálogo para Android TV usando Jetpack Compose for TV.
 * Muestra el estado actual de la frecuencia cardíaca y el historial local de lecturas.
 * Es completamente navegable mediante D-pad del control remoto.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
@Composable
fun PantallaCatalogoTv(
    vistaModelo: TvVistaModelo = viewModel(),
    alSeleccionarLectura: (Int) -> Unit = {}
) {
    val estado by vistaModelo.estado.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B4A))
    ) {
        if (estado.estaCargando) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF42A5F5)
            )
            return@Box
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Fila 1: Frecuencia cardíaca en tiempo real
            item {
                SeccionFila(
                    titulo = "⚡ Frecuencia Cardíaca Actual — ${estado.frecuenciaCardiacaActual} bpm"
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        // Mostrar las últimas 3 lecturas si existen
                        items(estado.lecturas.takeLast(3)) { lectura ->
                            TarjetaLecturaFc(
                                lectura = lectura,
                                alHacerClic = { alSeleccionarLectura(lectura.id) }
                            )
                        }
                    }
                }
            }

            // Fila 2: Historial completo de lecturas en Room
            item {
                SeccionFila(
                    titulo = "📋 Historial Completo"
                ) {
                    if (estado.lecturas.isEmpty()) {
                        Text(
                            text = "No hay lecturas registradas.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(estado.lecturas) { lectura ->
                                TarjetaLecturaFc(
                                    lectura = lectura,
                                    alHacerClic = { alSeleccionarLectura(lectura.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Contenedor de sección de filas para estructurar títulos y sub-listas de forma uniforme.
 */
@Composable
private fun SeccionFila(
    titulo: String,
    contenido: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        contenido()
    }
}
