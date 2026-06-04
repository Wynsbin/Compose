package com.yung.iot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alibaba.android.arouter.facade.annotation.Route
import com.yung.route.RoutePath
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.net.ssl.SSLSocketFactory

@Route(path = RoutePath.Iot.MAIN)
class IotMainActivity : ComponentActivity() {

    private lateinit var mqttClient: MqttAndroidClient

    private var status by mutableStateOf("未连接")
    private var lastMessage by mutableStateOf("暂无消息")
    private var isDisconnecting by mutableStateOf(false)
    private var userRequestedDisconnect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connect()
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            ) {
                Text(text = "IoT MQTT")
                Text(text = "状态: $status")
                Text(text = "最近消息: $lastMessage", modifier = Modifier.fillMaxWidth())
                Button(onClick = { publish(TOPIC, "Hello from Android") }) {
                    Text(text = "发布消息")
                }
                Button(onClick = { disconnect() }, enabled = !isDisconnecting) {
                    Text(text = if (isDisconnecting) "断开中..." else "断开连接")
                }
            }
        }
    }

    private fun connect() {
        mqttClient = MqttAndroidClient(applicationContext, SERVER_URI, CLIENT_ID)
        mqttClient.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val payload = message?.toString().orEmpty()
                Log.d(TAG, "Receive message: $payload from topic: $topic")
                runOnUiThread {
                    lastMessage = "$topic -> $payload"
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause?.message}")
                runOnUiThread {
                    if (!userRequestedDisconnect) {
                        status = "连接丢失"
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
        })

        val options = MqttConnectOptions().apply {
            userName = USERNAME
            password = PASSWORD.toCharArray()
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 60
            // MQTTX 使用 mqtts://（8883），Paho 需用 ssl:// 并启用系统 CA 证书校验
            socketFactory = SSLSocketFactory.getDefault()
        }

        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    runOnUiThread { status = "已连接" }
                    subscribe(TOPIC)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Connection failure", exception)
                    runOnUiThread {
                        status = "连接失败: ${exception?.rootCauseMessage()}"
                    }
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Connect error", e)
            status = "连接异常: ${e.message}"
        }
    }

    private fun subscribe(topic: String, qos: Int = 1) {
        try {
            mqttClient.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                    runOnUiThread { status = "已连接，已订阅 $topic" }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic", exception)
                    runOnUiThread { status = "订阅失败: ${exception?.message}" }
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Subscribe error", e)
        }
    }

    private fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {
        try {
            val message = MqttMessage().apply {
                payload = msg.toByteArray()
                this.qos = qos
                isRetained = retained
            }
            mqttClient.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$msg published to $topic")
                    runOnUiThread { lastMessage = "已发布 -> $topic: $msg" }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish $msg to $topic", exception)
                    runOnUiThread { status = "发布失败: ${exception?.message}" }
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Publish error", e)
        }
    }

    private fun disconnect() {
        if (!::mqttClient.isInitialized || isDisconnecting) return
        if (!mqttClient.isConnected) {
            status = "已断开"
            releaseMqttClient()
            return
        }
        isDisconnecting = true
        userRequestedDisconnect = true
        try {
            mqttClient.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                    runOnUiThread {
                        status = "已断开"
                        isDisconnecting = false
                        userRequestedDisconnect = false
                        releaseMqttClient()
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect", exception)
                    runOnUiThread {
                        status = "断开失败: ${exception?.message}"
                        isDisconnecting = false
                        userRequestedDisconnect = false
                        releaseMqttClient()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error", e)
            isDisconnecting = false
            userRequestedDisconnect = false
            status = "断开异常: ${e.message}"
            releaseMqttClient()
        }
    }

    /** disconnect 后 clientHandle 会被置空，重复调用 disconnect() 会 NPE，需先判连再释放资源 */
    private fun releaseMqttClient() {
        if (!::mqttClient.isInitialized) return
        try {
            mqttClient.unregisterResources()
            mqttClient.close()
        } catch (e: Exception) {
            Log.w(TAG, "Release mqtt client error", e)
        }
    }

    override fun onDestroy() {
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            try {
                mqttClient.disconnect()
            } catch (_: Exception) {
            }
        }
        releaseMqttClient()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "IotMainActivity"

        //Host（mqtt:// 在 Paho 中对应 ssl://）   Port(1883)
        private const val SERVER_URI = "ssl://XXX:8883"
        private const val USERNAME = ""//Username
        private const val PASSWORD = ""//Password
        // 勿与 MQTTX 使用相同 Client ID，否则后连的会把先连的踢掉
        private const val CLIENT_ID = "android_iot_client"
        private const val TOPIC = "a/b"//订阅主题
    }
}

private fun Throwable.rootCauseMessage(): String {
    var cause: Throwable? = this
    while (cause?.cause != null) {
        cause = cause.cause
    }
    return cause?.message ?: message.orEmpty()
}
