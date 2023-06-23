package com.tokeninc.sardis.application_template.data.repositories

import androidx.lifecycle.LiveData
import com.tokeninc.sardis.application_template.data.database.batch.BatchDao
import com.tokeninc.sardis.application_template.data.database.batch.Batch
import javax.inject.Inject

class BatchRepository @Inject constructor(private val batchDao: BatchDao) {
    fun getGroupSN() = batchDao.getGUPSN()
    val batchNo = batchDao.getBatchNo()
    fun getPreviousBatchSlip(): LiveData<String?> = batchDao.getBatchPreviousSlip()
    var allBatch: List<Batch?> = batchDao.getAllBatch()

    suspend fun updateBatchNo(batchNo: Int){
        batchDao.updateBatchNo(batchNo)
    }

    suspend fun updateBatchSlip(batchSlip: String?,batchNo: Int?){
        batchDao.updateBatchSlip(batchSlip, batchNo)
    }

    suspend fun updateGUPSN(groupSn: Int){
        batchDao.updateGUPSN(groupSn)
    }

    fun deleteAll(){
        batchDao.deleteAll()
    }
}
