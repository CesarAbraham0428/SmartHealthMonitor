package mx.utng.cala.smarthealthmonitor.data.remote

import mx.utng.cala.smarthealthmonitor.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ClienteNeon {
    private val urlBase: String
        get() {
            val host = BuildConfig.NEON_HOST
                .trim()
                .removePrefix("https://")
                .removePrefix("http://")
                .trimEnd('/')
            require(host.isNotEmpty()) { "NEON_HOST no est\u00e1 configurado" }
            return "https://$host/"
        }

    val CABECERA_AUTORIZACION: String
        get() = throw IllegalStateException(
            "Un API key administrativo de Neon no debe empaquetarse en Android. " +
                "Usa el JWT de Neon Auth o un backend."
        )

    /**
     * Una cadena PostgreSQL con privilegios de propietario nunca debe viajar en un APK.
     * La antigua API de este proyecto necesitaba esta cabecera para ejecutar SQL arbitrario;
     * se bloquea de forma deliberada hasta migrar a Neon Data API con JWT + RLS o a un backend.
     */
    val CADENA_CONEXION: String
        get() = throw IllegalStateException(
            "La aplicaci\u00f3n no puede ejecutar SQL de Neon con credenciales de propietario. " +
                "Usa Neon Data API con JWT y RLS, o un backend."
        )

    val servicioApi: ServicioApiNeon by lazy {
        Retrofit.Builder()
            .baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .callTimeout(20, TimeUnit.SECONDS)
                    .build()
            )
            .build()
            .create(ServicioApiNeon::class.java)
    }
}
