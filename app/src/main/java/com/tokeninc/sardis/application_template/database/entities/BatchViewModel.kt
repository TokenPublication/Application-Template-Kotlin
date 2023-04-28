package com.tokeninc.sardis.application_template.database.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatchViewModel(private val batchRepository: BatchRepository): ViewModel() {

    val groupSN = batchRepository.groupSN
    val batchNo = batchRepository.batchNo
    val previousBatchSlip = batchRepository.previousBatchSlip

    fun updateBatchNo(batchNo: Int){
        viewModelScope.launch(Dispatchers.IO) {
            batchRepository.updateBatchNo(batchNo)
        }
    }

    fun updateBatchSlip(batchSlip: String?,batchNo: Int){
        viewModelScope.launch(Dispatchers.IO){
            batchRepository.updateBatchSlip(batchSlip, batchNo)
        }
    }

    fun updateGUPSN(groupSn: Int?){
        viewModelScope.launch(Dispatchers.IO) {
            batchRepository.updateGUPSN(groupSn)
        }
    }

    fun deleteAll(){
        batchRepository.deleteAll()
    }
}