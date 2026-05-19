package com.yung.home.category.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetFilesResponse(
    @SerialName("request_id") val requestId: String? = null,
    val code: Int,
    val data: List<FileDto> = emptyList(),
    val msg: String? = null,
)

@Serializable
data class FileDto(
    @SerialName("_id") val id: String,
    val cover: String? = null,
    @SerialName("zip_path") val zipPath: String? = null,
    @SerialName("material_name") val materialName: String? = null,
    @SerialName("material_alias") val materialAlias: String? = null,
)

data class CategoryFileItem(
    val id: String,
    val title: String,
    val coverUrl: String?,
    val audioUrl: String?,
)
