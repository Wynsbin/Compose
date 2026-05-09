package com.yung.base.network.service

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SampleApiService {
    @GET("api/v1/home")
    suspend fun getHome(@Query("id") id: String): String

    @POST("api/v1/login")
    suspend fun postLogin(): String
}
