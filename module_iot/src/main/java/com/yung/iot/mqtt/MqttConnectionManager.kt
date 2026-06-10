package com.yung.iot.mqtt

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.net.ssl.SSLSocketFactory

class MqttConnectionManager private constructor(
    private val appContext: Context,
    private val config: MqttConfig,
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var mqttClient: MqttAndroidClient? = null
    private var userRequestedDisconnect = false
    private var isDisconnecting = false
    private val subscribedTopics = linkedSetOf<String>()

    private val _connectionState = MutableStateFlow<MqttConnectionState>(MqttConnectionState.Disconnected)
    val connectionState: StateFlow<MqttConnectionState> = _connectionState.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<MqttIncomingMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<MqttIncomingMessage> = _incomingMessages.asSharedFlow()

    private val _lastPublished = MutableStateFlow<String?>(null)
    val lastPublished: StateFlow<String?> = _lastPublished.asStateFlow()

    fun connect() {
        if (_connectionState.value is MqttConnectionState.Connecting) return
        if (mqttClient?.isConnected == true) return

        _connectionState.value = MqttConnectionState.Connecting
        userRequestedDisconnect = false

        val client = MqttAndroidClient(appContext, config.serverUri, config.clientId)
        mqttClient = client
        client.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val payload = message?.toString().orEmpty()
                Log.d(TAG, "Receive message: $payload from topic: $topic")
                if (topic != null) {
                    _incomingMessages.tryEmit(MqttIncomingMessage(topic, payload))
                }
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause?.message}")
                if (!userRequestedDisconnect) {
                    mainHandler.post {
                        _connectionState.value = MqttConnectionState.Error("连接丢失")
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
        })

        val options = MqttConnectOptions().apply {
            if (config.username.isNotEmpty()) {
                userName = config.username
            }
            if (config.password.isNotEmpty()) {
                password = config.password.toCharArray()
            }
            isCleanSession = true
            connectionTimeout = 30
            keepAliveInterval = 60
            // tcp:// 不可设置 SSLSocketFactory，否则报 32105
            if (config.useTls) {
                socketFactory = SSLSocketFactory.getDefault()
            }
        }

        try {
            client.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                    mainHandler.post {
                        _connectionState.value = MqttConnectionState.Connected(subscribedTopics.toList())
                        subscribe(config.legacyTopic)
                        subscribe(MqttTopicResolver.propertyPostWildcard())
                        subscribe(MqttTopicResolver.stateWildcard())
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Connection failure", exception)
                    mainHandler.post {
                        _connectionState.value = MqttConnectionState.Error(
                            "连接失败: ${exception.rootCauseMessage()}",
                        )
                    }
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Connect error", e)
            _connectionState.value = MqttConnectionState.Error("连接异常: ${e.message}")
        }
    }

    fun subscribe(topic: String, qos: Int = 1) {
        val client = mqttClient ?: return
        if (!client.isConnected || subscribedTopics.contains(topic)) return
        try {
            client.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to $topic")
                    subscribedTopics.add(topic)
                    mainHandler.post {
                        _connectionState.update { state ->
                            if (state is MqttConnectionState.Connected) {
                                MqttConnectionState.Connected(subscribedTopics.toList())
                            } else {
                                state
                            }
                        }
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to subscribe $topic", exception)
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Subscribe error", e)
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        val client = mqttClient ?: return
        if (!client.isConnected) {
            _connectionState.value = MqttConnectionState.Error("未连接，无法发布")
            return
        }
        try {
            val message = MqttMessage().apply {
                this.payload = payload.toByteArray()
                this.qos = qos
                isRetained = retained
            }
            client.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "$payload published to $topic")
                    mainHandler.post {
                        _lastPublished.value = "已发布 -> $topic: $payload"
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to publish to $topic", exception)
                    mainHandler.post {
                        _connectionState.value = MqttConnectionState.Error(
                            "发布失败: ${exception?.message}",
                        )
                    }
                }
            })
        } catch (e: MqttException) {
            Log.e(TAG, "Publish error", e)
        }
    }

    fun disconnect() {
        val client = mqttClient ?: return
        if (isDisconnecting) return
        if (!client.isConnected) {
            _connectionState.value = MqttConnectionState.Disconnected
            releaseClient()
            return
        }
        isDisconnecting = true
        userRequestedDisconnect = true
        try {
            client.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Disconnected")
                    mainHandler.post {
                        isDisconnecting = false
                        userRequestedDisconnect = false
                        subscribedTopics.clear()
                        _connectionState.value = MqttConnectionState.Disconnected
                        releaseClient()
                    }
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Failed to disconnect", exception)
                    mainHandler.post {
                        isDisconnecting = false
                        userRequestedDisconnect = false
                        _connectionState.value = MqttConnectionState.Disconnected
                        releaseClient()
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Disconnect error", e)
            isDisconnecting = false
            userRequestedDisconnect = false
            _connectionState.value = MqttConnectionState.Disconnected
            releaseClient()
        }
    }

    private fun releaseClient() {
        val client = mqttClient ?: return
        try {
            client.unregisterResources()
            client.close()
        } catch (e: Exception) {
            Log.w(TAG, "Release mqtt client error", e)
        } finally {
            mqttClient = null
        }
    }

    companion object {
        private const val TAG = "IotMqtt"

        @Volatile
        private var instance: MqttConnectionManager? = null

        fun init(context: Context, config: MqttConfig = MqttConfig.DEFAULT) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = MqttConnectionManager(
                            appContext = context.applicationContext,
                            config = config,
                        )
                    }
                }
            }
        }

        fun getInstance(): MqttConnectionManager {
            return instance ?: error("MqttConnectionManager.init() must be called first")
        }
    }
}

private fun Throwable?.rootCauseMessage(): String {
    if (this == null) return ""
    var cause: Throwable? = this
    while (cause?.cause != null) {
        cause = cause.cause
    }
    return cause?.message ?: message.orEmpty()
}
