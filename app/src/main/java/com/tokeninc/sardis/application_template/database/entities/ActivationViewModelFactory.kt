package com.tokeninc.sardis.application_template.database.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ActivationViewModelFactory(private val repository: ActivationRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActivationViewModel(repository) as T
    }
}
