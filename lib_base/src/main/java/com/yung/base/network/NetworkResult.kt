package com.yung.base.network

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Failure(val throwable: Throwable) : NetworkResult<Nothing>()
}
