package com.yung.home.category.data.api

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.Logger
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal object SleepHttpClient {
    const val BASE_URL = "https://android-app-static.54yks.cn/api/v1"
    const val APP_ID = "com.restcare.sleepdetect"
    const val PAGE_SIZE = 100

    val client: HttpClient by lazy { createClient() }

    fun newNonce(length: Int = 32): String {
        val alphabet = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(length) { alphabet.random() }.joinToString("")
    }

    private fun createClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                },
            )
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(TAG, message)
                }
            }
            level = LogLevel.INFO
        }
    }

    private const val TAG = "SleepHttp"
}
