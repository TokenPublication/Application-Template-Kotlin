package com.tokeninc.sardis.application_template.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB

/**
 * This is for calling Transaction view model with Transaction database.
 */
class TransactionVMFactory(val database: TransactionDB): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TransactionViewModel(database) as T
    }

}
