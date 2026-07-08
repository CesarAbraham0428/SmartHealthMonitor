package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.PlaybackSupportFragment
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.leanback.LeanbackPlayerAdapter

/**
 * Fragmento de reproducción para Android TV.
 * Utiliza ExoPlayer de Media3 y un adaptador de Leanback para controlar la reproducción de audio.
 * 
 * Traducido al español en cumplimiento con las reglas del usuario.
 */
class FragmentoReproduccion : PlaybackSupportFragment() {
    private lateinit var reproductor: ExoPlayer

    companion object {
        private const val RETRASO_ACTUALIZACION_MS = 16
        const val ARG_URL = "url_multimedia"
        const val ARG_TITULO = "titulo_multimedia"

        fun nuevaInstancia(url: String, titulo: String = "Alerta"): FragmentoReproduccion =
            FragmentoReproduccion().apply {
                arguments = Bundle().also { bundle ->
                    bundle.putString(ARG_URL, url)
                    bundle.putString(ARG_TITULO, titulo)
                }
            }
    }

    override fun onViewCreated(vista: View, estadoInstanciaGuardado: Bundle?) {
        super.onViewCreated(vista, estadoInstanciaGuardado)
        val url = arguments?.getString(ARG_URL) ?: return
        val titulo = arguments?.getString(ARG_TITULO) ?: "Alerta"

        // 1. Crear el motor de reproducción (ExoPlayer)
        reproductor = ExoPlayer.Builder(requireContext()).build()

        // 2. Conectar con la UI de Leanback usando el Adaptador y el Acople
        val adaptador = LeanbackPlayerAdapter(requireContext(), reproductor, RETRASO_ACTUALIZACION_MS)
        val acople = PlaybackTransportControlGlue(requireContext(), adaptador).apply {
            this.title = titulo
            this.subtitle = "SmartHealth Monitor"
        }
        acople.host = PlaybackSupportFragmentGlueHost(this@FragmentoReproduccion)
        acople.playWhenPrepared()

        // 3. Cargar y reproducir el contenido multimedia
        reproductor.setMediaItem(MediaItem.fromUri(url))
        reproductor.prepare()
    }

    // CRÍTICO: Siempre liberar ExoPlayer al destruir la vista para evitar fugas de memoria
    override fun onDestroyView() {
        super.onDestroyView()
        if (::reproductor.isInitialized) {
            reproductor.release()
        }
    }
}
