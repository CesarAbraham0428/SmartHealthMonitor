package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Fragmento principal temporal para Android TV.
 * En los siguientes pasos implementará BrowseSupportFragment de Leanback.
 */
class FragmentoPrincipal : Fragment() {

    override fun onCreateView(
        inflador: LayoutInflater,
        contenedor: ViewGroup?,
        estadoInstanciaGuardado: Bundle?
    ): View? {
        // Retorna una vista vacía por ahora para mostrar la pantalla en blanco
        return View(context)
    }
}
