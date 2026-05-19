package com.yung.home.category.files

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CategoryFileListViewModelFactory(
    private val application: Application,
    private val categoryId: String,
    private val categoryName: String,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(CategoryFileListViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return CategoryFileListViewModel(application, categoryId, categoryName) as T
    }
}
