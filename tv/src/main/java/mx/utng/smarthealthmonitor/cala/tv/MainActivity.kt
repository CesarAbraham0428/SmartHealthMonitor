package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * MainActivity para Android TV.
 * Es solo el contenedor: carga FragmentoPrincipal.
 * TODA la lógica de UI va en el Fragment.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(estadoInstanciaGuardado: Bundle?) {
        super.onCreate(estadoInstanciaGuardado)
        setContentView(R.layout.activity_main)
        if (estadoInstanciaGuardado == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragmento_principal, FragmentoPrincipal())
                .commit()
        }
    }
}