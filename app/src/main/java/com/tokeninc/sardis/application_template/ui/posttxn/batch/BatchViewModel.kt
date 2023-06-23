package com.tokeninc.sardis.application_template.ui.posttxn.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokeninc.sardis.application_template.data.repositories.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchViewModel @Inject constructor(private val batchRepository: BatchRepository): ViewModel() {

    val groupSN = batchRepository.groupSN
    val batchNo = batchRepository.batchNo
    val previousBatchSlip = batchRepository.previousBatchSlip
    val allBatch = batchRepository.allBatch

    fun updateBatchNo(batchNo: Int){
        viewModelScope.launch(Dispatchers.IO) {
            batchRepository.updateBatchNo(batchNo)
        }
    }

    fun updateBatchSlip(batchSlip: String?,batchNo: Int?){
        viewModelScope.launch(Dispatchers.IO){
            batchRepository.updateBatchSlip(batchSlip, batchNo)
        }
    }

    fun updateGUPSN(groupSn: Int){
        viewModelScope.launch(Dispatchers.IO) {
            batchRepository.updateGUPSN(groupSn)
        }
    }

    fun deleteAll(){
        batchRepository.deleteAll()
    }
}