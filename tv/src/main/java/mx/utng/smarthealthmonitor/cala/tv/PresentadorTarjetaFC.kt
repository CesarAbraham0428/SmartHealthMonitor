package mx.utng.smarthealthmonitor.cala.tv

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter

/**
 * Presentador de tarjetas para las lecturas de Frecuencia Cardíaca (FC) en Android TV.
 * Traducido al español en cumplimiento con las directivas del usuario.
 */
class PresentadorTarjetaFC : Presenter() {
    override fun onCreateViewHolder(padre: ViewGroup): ViewHolder {
        val vistaTarjeta = ImageCardView(padre.context).apply {
            // Permitir navegación del D-pad hacia esta tarjeta
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(240, 180)
        }
        return ViewHolder(vistaTarjeta)
    }

    override fun onBindViewHolder(portadorVista: ViewHolder, elemento: Any?) {
        val tarjeta = portadorVista.view as ImageCardView
        val lectura = elemento as LecturaFC
        tarjeta.titleText = "${lectura.valorBpm} bpm"
        tarjeta.contentText = lectura.hora

        // Color de fondo dinámico dependiendo de si la lectura es normal o no
        val colorFondo = if (lectura.esNormal) {
            Color.parseColor("#1B4F8A") // primary (sh_primary)
        } else {
            Color.parseColor("#B3261E") // error (sh_error)
        }
        tarjeta.setBackgroundColor(colorFondo)
    }

    override fun onUnbindViewHolder(portadorVista: ViewHolder) {
        val tarjeta = portadorVista.view as ImageCardView
        tarjeta.mainImage = null
    }
}
