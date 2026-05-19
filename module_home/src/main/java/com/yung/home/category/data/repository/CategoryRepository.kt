package com.yung.home.category.data.repository

import com.yung.home.category.data.api.CategoryApi
import com.yung.home.category.data.model.CategoryListItem

class CategoryRepository(
    private val api: CategoryApi = CategoryApi(),
) {

    suspend fun loadCategories(): Result<List<CategoryListItem>> = runCatching {
        val response = api.fetchCategories()
        if (response.code != 200) {
            error(response.msg ?: "请求失败 (${response.code})")
        }
        val roots = response.data?.data.orEmpty()
        buildList {
            roots.forEach { parent ->
                add(
                    CategoryListItem(
                        id = parent.id,
                        name = parent.categoryName,
                        isChild = false,
                    ),
                )
                parent.children.forEach { child ->
                    add(
                        CategoryListItem(
                            id = child.id,
                            name = child.categoryName,
                            isChild = true,
                        ),
                    )
                }
            }
        }
    }
}
