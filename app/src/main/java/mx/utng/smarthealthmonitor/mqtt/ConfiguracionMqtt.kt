package mx.utng.smarthealthmonitor.mqtt

import mx.utng.cala.smarthealthmonitor.BuildConfig

object ConfiguracionMqtt {
    val URL_BROKER = BuildConfig.MQTT_BROKER_URL
    val USUARIO = BuildConfig.MQTT_USERNAME
    val CONTRASENA = BuildConfig.MQTT_PASSWORD

    // Topics del proyecto
    const val TEMA_FC = "utng/smarthealthmonitor/fc"
    const val TEMA_TV = "utng/smarthealthmonitor/tv"
    const val TEMA_ALERTA = "utng/smarthealthmonitor/alerta"

    // QoS: 0 best effort, 1 at least once, 2 exactly once
    const val QOS = 1

    // Client IDs únicos por dispositivo
    const val ID_CLIENTE_RELOJ = "smarthealthmonitor-wear"
    const val ID_CLIENTE_APP = "smarthealthmonitor-app"
    const val ID_CLIENTE_TV = "smarthealthmonitor-tv"
}
