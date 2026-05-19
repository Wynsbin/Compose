package com.yung.home.category.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import com.yung.home.category.data.model.GetCategoryResponse
import com.yung.home.category.data.model.GetFilesResponse

class CategoryApi(
    private val client: HttpClient = SleepHttpClient.client,
) {

    suspend fun fetchCategories(): GetCategoryResponse {
        return client.get("${SleepHttpClient.BASE_URL}/GetCategory") {
            parameter("pageSize", SleepHttpClient.PAGE_SIZE)
            parameter("app_id", SleepHttpClient.APP_ID)
            parameter("timestamp", System.currentTimeMillis())
            parameter("nonce", SleepHttpClient.newNonce())
        }.body()
    }

    suspend fun fetchFiles(categoryId: String): GetFilesResponse {
        return client.get("${SleepHttpClient.BASE_URL}/GetFiles") {
            parameter("app_id", SleepHttpClient.APP_ID)
            parameter("category_id", categoryId)
            parameter("pageIndex", 1)
            parameter("pageSize", SleepHttpClient.PAGE_SIZE)
            parameter("timestamp", System.currentTimeMillis())
            parameter("nonce", SleepHttpClient.newNonce())
        }.body()
    }
}
