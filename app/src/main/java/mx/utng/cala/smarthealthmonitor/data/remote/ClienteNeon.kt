package mx.utng.cala.smarthealthmonitor.data.remote

import mx.utng.cala.smarthealthmonitor.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClienteNeon {
    private const val URL_BASE = "https://${BuildConfig.NEON_HOST}/"
    val CABECERA_AUTORIZACION = "Bearer ${BuildConfig.NEON_API_KEY}"
    val CADENA_CONEXION = "postgresql://neondb_owner:npg_eHGP1d9rZRVn@ep-quiet-firefly-atb25uua.c-9.us-east-1.aws.neon.tech/neondb?sslmode=require"

    val servicioApi: ServicioApiNeon by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .build()
            .create(ServicioApiNeon::class.java)
    }
}
