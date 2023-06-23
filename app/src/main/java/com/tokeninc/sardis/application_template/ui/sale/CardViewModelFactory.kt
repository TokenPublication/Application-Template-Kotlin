package com.tokeninc.sardis.application_template.ui.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tokeninc.sardis.application_template.data.repositories.CardRepository

class CardViewModelFactory (private val repository: CardRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CardViewModel(repository) as T
    }
}
