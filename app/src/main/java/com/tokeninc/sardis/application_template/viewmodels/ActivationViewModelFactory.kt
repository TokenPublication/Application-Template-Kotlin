package com.tokeninc.sardis.application_template.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tokeninc.sardis.application_template.repositories.ActivationRepository

class ActivationViewModelFactory(private val repository: ActivationRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActivationViewModel(repository) as T
    }
}
