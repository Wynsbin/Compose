package com.yung.base.network

class NetworkException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
