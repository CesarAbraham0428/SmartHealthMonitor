package mx.utng.smarthealthmonitor.wear.mqtt

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mx.utng.smarthealthmonitor.mqtt.ConfiguracionMqtt
import mx.utng.smarthealthmonitor.mqtt.MensajeFc
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class PublicadorMqttReloj(private val contexto: Context) {
    private var cliente: MqttAsyncClient? = null

    fun conectar() {
        cliente = MqttAsyncClient(
            ConfiguracionMqtt.URL_BROKER,
            ConfiguracionMqtt.ID_CLIENTE_RELOJ,
            MemoryPersistence()
        )

        val opciones = MqttConnectOptions().apply {
            userName = ConfiguracionMqtt.USUARIO
            password = ConfiguracionMqtt.CONTRASENA.toCharArray()
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 60
            // SSL habilitado automáticamente por la URL ssl://
            socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
        }

        cliente?.connect(opciones, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d("MQTT_RELOJ", "☑ Conectado a HiveMQ Cloud")
            }

            override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                Log.e("MQTT_RELOJ", "X Error al conectar: ${ex?.message}")
            }
        })
    }

    /** Publicar FC al topic MQTT */
    fun publicarFrecuenciaCardiaca(bpm: Int, estado: String) {
        val clienteMqtt = cliente
        if (clienteMqtt == null || !clienteMqtt.isConnected) {
            Log.w("MQTT_RELOJ", "No se puede publicar: Cliente desconectado")
            return
        }
        val mensaje = MensajeFc(bpm = bpm, estado = estado)
        val cargaUtil = Json.encodeToString(mensaje).toByteArray()
        val mensajeMqtt = MqttMessage(cargaUtil).apply {
            qos = ConfiguracionMqtt.QOS
            isRetained = true // El TV verá el último valor al conectarse
        }
        clienteMqtt.publish(ConfiguracionMqtt.TEMA_FC, mensajeMqtt)
        Log.d("MQTT_RELOJ", " Publicado: ${bpm} bpm en ${ConfiguracionMqtt.TEMA_FC}")
    }

    fun desconectar() {
        try {
            cliente?.disconnect()
        } catch (e: Exception) {
            Log.e("MQTT_RELOJ", "Error al desconectar: ${e.message}")
        }
    }
}
