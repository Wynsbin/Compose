package com.yung.home.category.data.repository

import com.yung.home.category.data.api.CategoryApi
import com.yung.home.category.data.model.CategoryFileItem

class FileRepository(
    private val api: CategoryApi = CategoryApi(),
) {

    suspend fun loadFiles(categoryId: String): Result<List<CategoryFileItem>> = runCatching {
        val response = api.fetchFiles(categoryId)
        if (response.code != 200) {
            error(response.msg ?: "请求失败 (${response.code})")
        }
        response.data.map { dto ->
            CategoryFileItem(
                id = dto.id,
                title = dto.materialName ?: dto.materialAlias ?: dto.id,
                coverUrl = dto.cover,
                audioUrl = dto.zipPath,
            )
        }
    }
}
