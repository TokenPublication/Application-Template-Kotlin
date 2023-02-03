package com.tokeninc.sardis.application_template.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tokeninc.sardis.application_template.database.activation.ActivationDB

/**
 * This is for calling Activation view model with Activation database.
 */
class ActivationVMFactory(val database: ActivationDB): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ActivationViewModel(database) as T
    }
}