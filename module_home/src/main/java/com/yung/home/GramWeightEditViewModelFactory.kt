package com.yung.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GramWeightEditViewModelFactory(
    private val initialGrams: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(GramWeightEditViewModel::class.java)) {
            "Unknown ViewModel class $modelClass"
        }
        @Suppress("UNCHECKED_CAST")
        return GramWeightEditViewModel(initialGrams) as T
    }
}
