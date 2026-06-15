package mx.utng.cala.wear.presentation.data.models

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.data.SampleDataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking


class HealthDataService : PassiveListenerService() {

    private lateinit var wearDataSender: WearDataSender

    override fun onCreate() {
        super.onCreate()
        wearDataSender = WearDataSender(this)
        Log.d("HealthDataService", "Servicio creado")
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val fcDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)

        val ultimoDatoFC = fcDataPoints
            .filterIsInstance<SampleDataPoint<Double>>()
            .lastOrNull()

        if (ultimoDatoFC != null) {
            val bpm = ultimoDatoFC.value.toInt()

            Log.d("HealthDataService", "FC recibida desde sensor: $bpm")

            runBlocking(Dispatchers.IO) {
                wearDataSender.enviarFC(bpm)
            }
        } else {
            Log.d("HealthDataService", "No llegó dato válido de FC")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("HealthDataService", "Servicio destruido")
    }

    companion object {
        suspend fun registrar(context: Context) {
            val hsClient = HealthServices.getClient(context)
            val passiveClient = hsClient.passiveMonitoringClient

            val config = PassiveListenerConfig.builder()
                .setDataTypes(setOf(DataType.HEART_RATE_BPM))
                .setShouldUserActivityInfoBeRequested(true)
                .build()

            passiveClient.setPassiveListenerServiceAsync(
                HealthDataService::class.java,
                config
            ).await()

            Log.d("HealthDataService", "HealthDataService registrado correctamente")
        }
    }
}