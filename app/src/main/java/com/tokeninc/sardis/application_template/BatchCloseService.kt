package com.tokeninc.sardis.application_template

import android.util.Log
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.database.SlipDB
import com.tokeninc.sardis.application_template.database.batch.BatchDB
import com.tokeninc.sardis.application_template.enums.BatchResult
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.helpers.printHelpers.BatchClosePrintHelper
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintServiceBinding
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class BatchCloseService {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    var mainActivity: MainActivity? = null
    var batchDB: BatchDB? = null
    var slipDB: SlipDB? = null
    var transactionViewModel: TransactionViewModel? = null
    private var downloadNumber: Int = 0


    suspend fun doInBackground(): BatchCloseResponse?{

        var batchCloseResponse: BatchCloseResponse? = null
        var dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        coroutineScope.launch(Dispatchers.Main){
            mainActivity!!.showDialog(dialog)
        }

        coroutineScope.launch {
            for (i in 0..10) {
                delay(300L)
                if (downloadNumber < 10) {
                    coroutineScope.launch(Dispatchers.Main) {
                        dialog.update(InfoDialog.InfoType.Progress,"Connecting ${downloadNumber*10}")
                    }
                }
                downloadNumber++
            }
            val deferred = coroutineScope.async(Dispatchers.IO) {
                finishBatchClose(dialog)
            }
            batchCloseResponse = deferred.await()

        }.join()
        return batchCloseResponse
    }


    private fun finishBatchClose(dialog: InfoDialog): BatchCloseResponse?{
        val transactions = transactionViewModel!!.getAllTransactions()
        coroutineScope.async(Dispatchers.Main) {
            dialog.update(InfoDialog.InfoType.Confirmed, "Grup Kapama Başarılı")
        }
        val printService = BatchClosePrintHelper()
        val printServiceBinding = PrintServiceBinding()
        var slip = printService.batchText(batchDB!!.getBatchNo().toString(),transactions,mainActivity!!,false)
        printServiceBinding.print(slip)
        slip = printService.batchText(batchDB!!.getBatchNo().toString(),transactions,mainActivity!!,true)
        Log.d("Repetition",slip)
        slipDB!!.insertSlip(slip)
        batchDB!!.updateBatchNo(batchDB!!.getBatchNo()!! + 1)
        transactionViewModel!!.deleteAll()
        return BatchCloseResponse(BatchResult.SUCCESS, SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault()))
    }

}