package mx.utng.smarthealthmonitor.cala.tv

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import mx.utng.cala.smarthealthmonitor.data.models.db.LecturaFC

/**
 * Presentador para mostrar la descripción detallada de una lectura de frecuencia cardíaca.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class PresentadorDescripcionDetalles : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(contenedorVista: ViewHolder, elemento: Any) {
        val lectura = elemento as LecturaFC

        // Título: valor de frecuencia cardíaca (FC)
        contenedorVista.title.text = "${lectura.valorBpm} bpm"

        // Subtítulo: estado de salud actual
        contenedorVista.subtitle.text = if (lectura.esNormal) {
            "✓ Frecuencia normal"
        } else {
            "⚠ Fuera de rango, consulta al médico"
        }

        // Cuerpo: hora del registro y el ID completo
        contenedorVista.body.text = "Registrado a las ${lectura.hora}\nID de lectura: ${lectura.id}"
    }
}
