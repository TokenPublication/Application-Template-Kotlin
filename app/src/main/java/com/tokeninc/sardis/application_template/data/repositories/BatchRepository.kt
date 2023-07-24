package com.tokeninc.sardis.application_template.data.repositories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.database.batch.BatchDao
import com.tokeninc.sardis.application_template.data.entities.responses.BatchCloseResponse
import javax.inject.Inject

/**
 * This class contains both Batch Dao operations and some operations about the preparing batch intent data.
 */
class BatchRepository @Inject constructor(private val batchDao: BatchDao) {
    fun getGroupSN() = batchDao.getGUPSN()
    fun getBatchNo() = batchDao.getBatchNo()
    fun getSTN() = batchDao.getSTN()
    fun getPreviousBatchSlip(): LiveData<String?> = batchDao.getBatchPreviousSlip()
    suspend fun updateBatchNo(batchNo: Int){
        batchDao.updateBatchNo(batchNo)
    }

    suspend fun updateBatchSlip(batchSlip: String?,batchNo: Int?){
        batchDao.updateBatchSlip(batchSlip, batchNo)
    }

    suspend fun updateGUPSN(groupSn: Int){
        batchDao.updateGUPSN(groupSn)
    }

    suspend fun updateSTN(){
        batchDao.updateSTN()
    }

    /**
     * It finishes the batch operation via printing slip with respect to
     * @param batchCloseResponse
     * and passes the response code as a liveData intent which is observed from its fragment and finishes the mainActivity
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

    private fun print(printText: String?, mainActivity: MainActivity) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(mainActivity.applicationContext))
    }
}
