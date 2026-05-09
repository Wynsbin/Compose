package com.yung.base.network.interceptor

import com.yung.base.network.NetworkException
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class NetworkErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (ioException: IOException) {
            throw NetworkException("Network request failed.", ioException)
        }
    }
}
