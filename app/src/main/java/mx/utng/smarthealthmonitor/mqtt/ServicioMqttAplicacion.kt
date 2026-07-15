package mx.utng.smarthealthmonitor.mqtt

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServicioMqttAplicacion(
    private val contexto: Context,
    private val alRecibirBpm: (Int) -> Unit
) {
    private var cliente: MqttAsyncClient? = null

    fun conectar() {
        cliente = MqttAsyncClient(
            ConfiguracionMqtt.URL_BROKER,
            ConfiguracionMqtt.ID_CLIENTE_APP,
            MemoryPersistence()
        )

        val opciones = MqttConnectOptions().apply {
            userName = ConfiguracionMqtt.USUARIO
            password = ConfiguracionMqtt.CONTRASENA.toCharArray()
            isCleanSession = true
            connectionTimeout = 10
            keepAliveInterval = 60
            socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
        }

        cliente?.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String, msg: MqttMessage) {
                if (topic == ConfiguracionMqtt.TEMA_FC) {
                    procesarMensajeFc(msg)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.w("MQTT_APP", "Conexión perdida: ${cause?.message}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })

        cliente?.connect(opciones, null, object : IMqttActionListener {
            override fun onSuccess(token: IMqttToken?) {
                cliente?.subscribe(ConfiguracionMqtt.TEMA_FC, ConfiguracionMqtt.QOS)
                Log.d("MQTT_APP", "Conectado y suscrito a ${ConfiguracionMqtt.TEMA_FC}")
            }

            override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                Log.e("MQTT_APP", "X Error al conectar: ${ex?.message}")
            }
        })
    }

    private fun procesarMensajeFc(msg: MqttMessage) {
        try {
            val mensajeFc = Json.decodeFromString<MensajeFc>(String(msg.payload))

            // 1. Actualizar a través del callback (para guardar localmente)
            alRecibirBpm(mensajeFc.bpm)

            // 2. Re-publicar al topic de TV con la hora actual formateada
            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val mensajeTv = MensajeTv(bpm = mensajeFc.bpm, estado = mensajeFc.estado, hora = hora)
            val cargaUtilTv = Json.encodeToString(mensajeTv).toByteArray()

            val mensajeMqttTv = MqttMessage(cargaUtilTv).apply {
                qos = ConfiguracionMqtt.QOS
                isRetained = true
            }
            cliente?.publish(ConfiguracionMqtt.TEMA_TV, mensajeMqttTv)
            Log.d("MQTT_APP", "Re-publicado al TV: ${mensajeFc.bpm} bpm")
        } catch (e: Exception) {
            Log.e("MQTT_APP", "Error al procesar mensaje de FC: ${e.message}")
        }
    }

    fun desconectar() {
        try {
            cliente?.disconnect()
        } catch (e: Exception) {
            Log.e("MQTT_APP", "Error al desconectar: ${e.message}")
        }
    }
}
