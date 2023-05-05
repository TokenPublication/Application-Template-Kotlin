package com.tokeninc.sardis.application_template.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tokeninc.sardis.application_template.repositories.BatchRepository

class BatchViewModelFactory (private val repository: BatchRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BatchViewModel(repository) as T
    }
}