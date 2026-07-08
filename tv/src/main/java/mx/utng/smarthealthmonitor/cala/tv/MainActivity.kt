package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * Actividad principal de Android TV.
 * Configura la vista con soporte de fragmentos y carga el fragmento del catálogo inicial.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(estadoInstanciaGuardado: Bundle?) {
        super.onCreate(estadoInstanciaGuardado)
        setContentView(R.layout.actividad_principal)

        if (estadoInstanciaGuardado == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragmento, FragmentoCatalogo())
                .commit()
        }
    }
}