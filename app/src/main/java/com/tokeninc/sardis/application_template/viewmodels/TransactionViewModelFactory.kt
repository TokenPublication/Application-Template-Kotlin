package com.tokeninc.sardis.application_template.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tokeninc.sardis.application_template.repositories.TransactionRepository

class TransactionViewModelFactory(private val repository: TransactionRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TransactionViewModel(repository) as T
    }
}
