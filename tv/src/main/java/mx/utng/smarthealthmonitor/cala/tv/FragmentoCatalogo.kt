package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import mx.utng.smarthealthmonitor.cala.tv.R

/**
 * Fragmento contenedor que aloja la pantalla de catálogo en Compose.
 * Facilita la transición fluida utilizando fragmentos entre el catálogo y los detalles.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class FragmentoCatalogo : Fragment() {

    override fun onCreateView(
        inflador: LayoutInflater,
        contenedor: ViewGroup?,
        estadoInstanciaGuardado: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                PantallaCatalogoTv(
                    alSeleccionarLectura = { idLectura ->
                        val fragmentoDetalle = FragmentoDetalle.nuevaInstancia(idLectura)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.contenedor_fragmento, fragmentoDetalle)
                            .addToBackStack(null)
                            .commit()
                    }
                )
            }
        }
    }
}
