package com.yung.base.network

import retrofit2.HttpException

suspend fun <T> safeApiCall(call: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(call())
    } catch (httpException: HttpException) {
        NetworkResult.Failure(NetworkException("Http error: ${httpException.code()}", httpException))
    } catch (throwable: Throwable) {
        NetworkResult.Failure(throwable)
    }
}
