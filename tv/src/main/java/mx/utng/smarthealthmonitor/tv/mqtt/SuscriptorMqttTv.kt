package mx.utng.smarthealthmonitor.tv.mqtt

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import mx.utng.smarthealthmonitor.mqtt.ConfiguracionMqtt
import mx.utng.smarthealthmonitor.mqtt.MensajeTv
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class SuscriptorMqttTv(
    private val contexto: Context,
    private val alRecibirMensajeTv: (MensajeTv) -> Unit
) {
    private var cliente: MqttAsyncClient? = null

    fun conectar() {
        cliente = MqttAsyncClient(
            ConfiguracionMqtt.URL_BROKER,
            ConfiguracionMqtt.ID_CLIENTE_TV,
            MemoryPersistence()
        )

        cliente?.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String, msg: MqttMessage) {
                if (topic == ConfiguracionMqtt.TEMA_TV) {
                    try {
                        val mensajeTv = Json.decodeFromString<MensajeTv>(String(msg.payload))
                        alRecibirMensajeTv(mensajeTv)
                        Log.d("MQTT_TV", " Recibido en TV: ${mensajeTv.bpm} bpm")
                    } catch (e: Exception) {
                        Log.e("MQTT_TV", "Error al procesar mensaje en TV: ${e.message}")
                    }
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.w("MQTT_TV", "Conexión perdida en TV: ${cause?.message}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })

        val opciones = MqttConnectOptions().apply {
            userName = ConfiguracionMqtt.USUARIO
            password = ConfiguracionMqtt.CONTRASENA.toCharArray()
            isCleanSession = true
            socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
        }

        cliente?.conectar(opciones, null, object : IMqttActionListener {
            override fun onSuccess(token: IMqttToken?) {
                cliente?.subscribe(ConfiguracionMqtt.TEMA_TV, ConfiguracionMqtt.QOS)
                Log.d("MQTT_TV", " TV suscrita a ${ConfiguracionMqtt.TEMA_TV}")
            }

            override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                Log.e("MQTT_TV", "X Error al conectar en TV: ${ex?.message}")
            }
        })
    }

    fun desconectar() {
        try {
            cliente?.disconnect()
        } catch (e: Exception) {
            Log.e("MQTT_TV", "Error al desconectar: ${e.message}")
        }
    }
}
