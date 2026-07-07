package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter

/**
 * Fragmento principal para Android TV que muestra la información
 * utilizando la interfaz de tipo Browse de la librería Leanback.
 * Traducido al español en cumplimiento con las directivas del usuario.
 */
class FragmentoPrincipal : BrowseSupportFragment() {

    override fun onViewCreated(vista: View, estadoInstanciaGuardado: Bundle?) {
        super.onViewCreated(vista, estadoInstanciaGuardado)

        // Configuración del BrowseFragment
        title = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Color de la marca en el sidebar lateral
        brandColor = resources.getColor(R.color.sh_primary, null)

        cargarFilas()
    }

    private fun cargarFilas() {
        val adaptadorFilas = ArrayObjectAdapter(ListRowPresenter())

        // ── Fila 1: Estado actual (FC + Pasos) ───────────
        val adaptadorEstado = ArrayObjectAdapter(PresentadorTarjetaFC())
        // Datos simulados locales
        adaptadorEstado.add(LecturaFC(id = 0, valorBpm = 88, hora = "Ahora"))
        adaptadorEstado.add(LecturaFC(id = 1, valorBpm = 4250, hora = "Pasos"))
        adaptadorFilas.add(ListRow(HeaderItem("Estado actual"), adaptadorEstado))

        // ── Fila 2: Historial de FC ────────────────────
        val adaptadorHistorial = ArrayObjectAdapter(PresentadorTarjetaFC())
        DatosSimulados.historialFC.forEach { lectura ->
            adaptadorHistorial.add(lectura)
        }
        adaptadorFilas.add(ListRow(HeaderItem("Historial FC"), adaptadorHistorial))

        // Asignar el adaptador de filas al fragmento
        this.adapter = adaptadorFilas
    }
}
