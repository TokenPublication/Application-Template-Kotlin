package com.tokeninc.sardis.application_template.data.repositories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.database.batch.BatchDao
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.model.responses.BatchCloseResponse
import com.tokeninc.sardis.application_template.data.model.resultCode.BatchResult
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.utils.printHelpers.BatchClosePrintHelper
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

/**
 * This class contains both Batch Dao operations and some operations about the preparing batch intent data.
 */
class BatchRepository @Inject constructor(private val batchDao: BatchDao) {
    fun getGroupSN() = batchDao.getGUPSN()
    fun getBatchNo() = batchDao.getBatchNo()
    fun getSTN() = batchDao.getSTN()
    fun getPreviousBatchSlip(): LiveData<String?> = batchDao.getBatchPreviousSlip()
    suspend fun updateBatchNo(){
        batchDao.updateBatchNo()
    }

    suspend fun updateBatchSlip(batchSlip: String?){
        batchDao.updateBatchSlip(batchSlip)
    }

    suspend fun updateGUPSN(){
        batchDao.updateGUPSN()
    }

    suspend fun updateSTN(){
        batchDao.updateSTN()
    }

    fun prepareSlip(activity: FragmentActivity, activationViewModel: ActivationViewModel, transactionList: List<Transaction?>?, isCopy: Boolean, isBatch: Boolean = true): String {
        return BatchClosePrintHelper().batchText(getBatchNo().toString(),transactionList!!, activity, activationViewModel, isCopy, isBatch)
    }

    fun prepareResponse(batchResult: BatchResult): BatchCloseResponse{
        val date = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault())
        return BatchCloseResponse(batchResult,date)
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

    fun print(printText: String?, activity: FragmentActivity) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(activity.applicationContext))
    }
}
