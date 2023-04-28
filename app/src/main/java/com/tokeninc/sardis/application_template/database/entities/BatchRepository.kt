package com.tokeninc.sardis.application_template.database.entities

class BatchRepository(private val batchDao: BatchDao) {
    val groupSN = batchDao.getGUPSN()
    val batchNo = batchDao.getBatchNo()
    val previousBatchSlip = batchDao.getBatchPreviousSlip()

    suspend fun updateBatchNo(batchNo: Int){
        batchDao.updateBatchNo(batchNo)
    }

    suspend fun updateBatchSlip(batchSlip: String?,batchNo: Int){
        batchDao.updateBatchSlip(batchSlip, batchNo)
    }

    suspend fun updateGUPSN(groupSn: Int?){
        batchDao.updateGUPSN(groupSn)
    }

    fun deleteAll(){
        batchDao.deleteAll()
    }
}
