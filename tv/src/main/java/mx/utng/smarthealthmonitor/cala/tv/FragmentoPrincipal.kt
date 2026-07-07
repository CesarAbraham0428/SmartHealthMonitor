package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC

/**
 * Fragmento principal para Android TV que muestra la información
 * utilizando la interfaz de tipo Browse de la librería Leanback.
 * Observa de forma reactiva los cambios de frecuencia cardíaca, pasos e historial.
 * Traducido al español en cumplimiento con las directivas del usuario.
 */
class FragmentoPrincipal : BrowseSupportFragment() {

    private lateinit var vistaModelo: TvVistaModelo
    private lateinit var adaptadorEstado: ArrayObjectAdapter
    private lateinit var adaptadorHistorial: ArrayObjectAdapter

    override fun onViewCreated(vista: View, estadoInstanciaGuardado: Bundle?) {
        super.onViewCreated(vista, estadoInstanciaGuardado)

        // Inicializar el VistaModelo
        vistaModelo = ViewModelProvider(this)[TvVistaModelo::class.java]

        // Configuración del BrowseFragment
        title = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Color de la marca en el sidebar lateral
        brandColor = resources.getColor(R.color.sh_primary, null)

        cargarFilas()
        observarDatos()
    }

    private fun cargarFilas() {
        val adaptadorFilas = ArrayObjectAdapter(ListRowPresenter())

        // ── Fila 1: Estado actual (FC + Pasos) ───────────
        adaptadorEstado = ArrayObjectAdapter(PresentadorTarjetaFC())
        // Se inicializa con valores por defecto hasta que se reciban actualizaciones del repositorio
        adaptadorEstado.add(LecturaFC(id = -1, valorBpm = 0, esNormal = true))
        adaptadorEstado.add(LecturaFC(id = -2, valorBpm = 0, esNormal = true))
        adaptadorFilas.add(ListRow(HeaderItem("Estado actual"), adaptadorEstado))

        // ── Fila 2: Historial de FC ────────────────────
        adaptadorHistorial = ArrayObjectAdapter(PresentadorTarjetaFC())
        adaptadorFilas.add(ListRow(HeaderItem("Historial FC"), adaptadorHistorial))

        // Asignar el adaptador de filas al fragmento
        this.adapter = adaptadorFilas
    }

    private fun observarDatos() {
        // Observar cambios en el historial desde la base de datos Room
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vistaModelo.historial.collect { lecturas ->
                    adaptadorHistorial.clear()
                    lecturas.forEach { lectura ->
                        adaptadorHistorial.add(lectura)
                    }
                }
            }
        }

        // Observar cambios en tiempo real del estado actual (BPM y Pasos)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(vistaModelo.frecuenciaCardiaca, vistaModelo.pasos) { bpm, pasos ->
                    Pair(bpm, pasos)
                }.collect { (bpm, pasos) ->
                    adaptadorEstado.clear()
                    
                    // Card para la frecuencia cardíaca actual
                    val lecturaFC = LecturaFC(
                        id = -1,
                        valorBpm = bpm,
                        esNormal = bpm in 60..100
                    )
                    
                    // Card para los pasos acumulados (usamos el valorBpm para representar los pasos)
                    val lecturaPasos = LecturaFC(
                        id = -2,
                        valorBpm = pasos,
                        esNormal = true
                    )
                    
                    // Necesitamos una manera de diferenciar en la interfaz "Ahora" de "Pasos"
                    // Para esto creamos copias customizadas con la hora modificada
                    val lecturaFCConHora = lecturaFC.copy(hora = "Ahora")
                    val lecturaPasosConHora = lecturaPasos.copy(hora = "Pasos")
                    
                    adaptadorEstado.add(lecturaFCConHora)
                    adaptadorEstado.add(lecturaPasosConHora)
                }
            }
        }
    }
}
