package mx.utng.cala.smarthealthmonitor.cast

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.*

class ProveedorOpcionesCast : OptionsProvider {
    override fun getCastOptions(contexto: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(
                CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
            )
            .build()

    override fun getAdditionalSessionProviders(contexto: Context): List<SessionProvider> =
        emptyList()
}
