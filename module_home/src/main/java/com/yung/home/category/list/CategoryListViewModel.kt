package com.yung.home.category.list

import androidx.lifecycle.ViewModel
import com.yung.home.category.data.model.CategoryListItem
import com.yung.home.category.data.repository.CategoryRepository
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container as orbitContainer

data class CategoryListState(
    val isLoading: Boolean = true,
    val items: List<CategoryListItem> = emptyList(),
    val error: String? = null,
)

class CategoryListViewModel(
    private val repository: CategoryRepository = CategoryRepository(),
) : ViewModel(), ContainerHost<CategoryListState, Nothing> {

    override val container: Container<CategoryListState, Nothing> =
        orbitContainer(CategoryListState()) {
            loadCategories()
        }

    fun loadCategories() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        repository.loadCategories()
            .onSuccess { items ->
                reduce {
                    state.copy(
                        isLoading = false,
                        items = items,
                        error = null,
                    )
                }
            }
            .onFailure { throwable ->
                reduce {
                    state.copy(
                        isLoading = false,
                        error = throwable.message ?: "加载失败",
                    )
                }
            }
    }
}
