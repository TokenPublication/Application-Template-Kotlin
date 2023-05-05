package com.tokeninc.sardis.application_template.repositories

import com.tokeninc.sardis.application_template.database.dao.BatchDao
import com.tokeninc.sardis.application_template.database.entities.Batch
import javax.inject.Inject

class BatchRepository @Inject constructor(private val batchDao: BatchDao) {
    val groupSN = batchDao.getGUPSN()
    val batchNo = batchDao.getBatchNo()
    val previousBatchSlip = batchDao.getBatchPreviousSlip()
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
