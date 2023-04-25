package com.tokeninc.sardis.application_template.database.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BatchViewModelFactory (private val repository: BatchRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatchViewModel(repository) as T
    }
}