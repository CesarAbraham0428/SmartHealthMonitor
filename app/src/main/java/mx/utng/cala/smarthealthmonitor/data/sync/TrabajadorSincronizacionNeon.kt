package mx.utng.cala.smarthealthmonitor.data.sync

import android.content.Context
import androidx.work.*
import mx.utng.cala.smarthealthmonitor.data.models.db.SmartHealthDB
import mx.utng.cala.smarthealthmonitor.data.repository.RepositorioSincronizacion
import java.util.concurrent.TimeUnit

class TrabajadorSincronizacionNeon(
    contexto: Context,
    parametros: WorkerParameters
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        return try {
            val baseDatos = SmartHealthDB.getDatabase(applicationContext)
            val repositorio = RepositorioSincronizacion(baseDatos.lecturaDao())
            
            repositorio.enviarPendientes()
            repositorio.sincronizarDesdeNeon(limite = 100)
            
            android.util.Log.d("TRABAJADOR_SYNC", "Sincronización completada con éxito")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("TRABAJADOR_SYNC", "Error en sincronización: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        const val NOMBRE_TRABAJO = "TrabajadorSincronizacionNeon"

        fun programar(contexto: Context) {
            val restricciones = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val peticion = PeriodicWorkRequestBuilder<TrabajadorSincronizacionNeon>(30, TimeUnit.MINUTES)
                .setConstraints(restricciones)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
                NOMBRE_TRABAJO,
                ExistingPeriodicWorkPolicy.KEEP,
                peticion
            )
        }
    }
}
