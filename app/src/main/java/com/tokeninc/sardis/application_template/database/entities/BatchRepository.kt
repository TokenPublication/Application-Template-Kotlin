package com.tokeninc.sardis.application_template.database.entities

class BatchRepository(private val batchDao: BatchDao) {
    val groupSN = batchDao.getGUPSN()
    val batchNo = batchDao.getBatchNo()

    suspend fun updateBatchNo(batchNo: Int){
        batchDao.updateBatchNo(batchNo)
    }

    suspend fun updateGUPSN(groupSn: Int){
        batchDao.updateGUPSN(groupSn)
    }

    fun deleteAll(){
        batchDao.deleteAll()
    }
}
