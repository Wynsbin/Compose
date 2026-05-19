package com.yung.home.category.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetCategoryResponse(
    @SerialName("request_id") val requestId: String? = null,
    val code: Int,
    val data: CategoryPage? = null,
    val msg: String? = null,
)

@Serializable
data class CategoryPage(
    val total: Int = 0,
    val data: List<CategoryDto> = emptyList(),
)

@Serializable
data class CategoryDto(
    val id: String,
    @SerialName("category_name") val categoryName: String,
    @SerialName("category_alias") val categoryAlias: String? = null,
    val children: List<CategoryDto> = emptyList(),
)

data class CategoryListItem(
    val id: String,
    val name: String,
    val isChild: Boolean,
)
