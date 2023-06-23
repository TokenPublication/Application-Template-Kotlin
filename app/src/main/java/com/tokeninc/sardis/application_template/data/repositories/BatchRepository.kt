package com.tokeninc.sardis.application_template.data.repositories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.database.batch.BatchDao
import com.tokeninc.sardis.application_template.data.database.batch.Batch
import com.tokeninc.sardis.application_template.data.entities.responses.BatchCloseResponse
import com.tokeninc.sardis.application_template.data.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintService
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

    /**
     * It finishes the batch operation via printing slip with respect to achieved data and
     * passes the response code to liveData intent which is observed in its fragment and finishes the mainActivity
     */
    fun prepareBatchIntent(batchCloseResponse: BatchCloseResponse, mainActivity: MainActivity, slip: String): Intent {
        Log.d("finishBatch","${batchCloseResponse.batchResult}")
        val responseCode = batchCloseResponse.batchResult
        val intent = Intent()
        val bundle = Bundle()
        print(slip,mainActivity)
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)
        return intent
    }

    fun print(printText: String?, mainActivity: MainActivity) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(mainActivity.applicationContext))
    }
}
