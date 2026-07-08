package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.cala.tv.R
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC
import mx.utng.cala.smarthealthmonitor.data.models.db.SmartHealthDB

/**
 * Fragmento que muestra los detalles de una lectura de frecuencia cardíaca específica.
 * Permite reproducir una alerta o regresar al historial.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class FragmentoDetalle : DetailsSupportFragment(), OnActionClickedListener {

    private var lecturaGuardada: LecturaFC? = null

    companion object {
        const val ARG_ID_LECTURA = "id_lectura"
        const val ACCION_REPRODUCIR = 1L
        const val ACCION_REGRESAR = 2L

        fun nuevaInstancia(idLectura: Int): FragmentoDetalle {
            return FragmentoDetalle().apply {
                arguments = Bundle().also { bundle ->
                    bundle.putInt(ARG_ID_LECTURA, idLectura)
                }
            }
        }
    }

    override fun onViewCreated(vista: View, estadoInstanciaGuardado: Bundle?) {
        super.onViewCreated(vista, estadoInstanciaGuardado)
        val idLectura = arguments?.getInt(ARG_ID_LECTURA) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            // Buscar la lectura en Room por su ID
            val lectura = SmartHealthDB.getDatabase(requireContext())
                .lecturaDao().obtenerPorId(idLectura)
            
            lectura?.let {
                lecturaGuardada = it
                construirDetalle(it)
            }
        }
    }

    private fun construirDetalle(lectura: LecturaFC) {
        val selector = ClassPresenterSelector()
        val presentadorDetalle = FullWidthDetailsOverviewRowPresenter(PresentadorDescripcionDetalles())
        
        presentadorDetalle.setOnActionClickedListener(this)
        selector.addClassPresenter(DetailsOverviewRow::class.java, presentadorDetalle)

        val fila = DetailsOverviewRow(lectura)

        // Icono de corazón como imagen del detalle (placeholders)
        val recursoIcono = if (lectura.esNormal) {
            android.R.drawable.ic_menu_compass
        } else {
            android.R.drawable.ic_dialog_alert
        }
        fila.imageDrawable = ContextCompat.getDrawable(requireContext(), recursoIcono)

        // Botones de acción
        val acciones = ArrayObjectAdapter()
        acciones.add(Action(ACCION_REPRODUCIR, "Reproducir alerta"))
        acciones.add(Action(ACCION_REGRESAR, "Volver al historial"))
        fila.actionsAdapter = acciones

        val adaptador = ArrayObjectAdapter(selector)
        adaptador.add(fila)
        this.adapter = adaptador
    }

    override fun onActionClicked(accion: Action) {
        when (accion.id) {
            ACCION_REPRODUCIR -> {
                val lectura = lecturaGuardada
                if (lectura != null) {
                    val urlAudioPrueba = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                    val reproduccion = FragmentoReproduccion.nuevaInstancia(
                        url = urlAudioPrueba,
                        titulo = "Alerta FC ${lectura.valorBpm} bpm"
                    )
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.contenedor_fragmento, reproduccion)
                        .addToBackStack(null)
                        .commit()
                }
            }
            ACCION_REGRESAR -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}
