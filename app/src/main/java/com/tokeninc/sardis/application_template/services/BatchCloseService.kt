package com.tokeninc.sardis.application_template.services

import android.util.Log
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.ui.MainActivity
import com.tokeninc.sardis.application_template.database.slip.SlipDB
import com.tokeninc.sardis.application_template.database.batch.BatchDB
import com.tokeninc.sardis.application_template.enums.BatchResult
import com.tokeninc.sardis.application_template.helpers.printHelpers.BatchClosePrintHelper
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintServiceBinding
import com.tokeninc.sardis.application_template.responses.BatchCloseResponse
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * This is Batch Close Service, its aim is running batchClose and UI operations in parallel.
 */
class BatchCloseService {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mainActivity: MainActivity
    private lateinit var batchDB: BatchDB
    private lateinit var slipDB: SlipDB
    private lateinit var transactionViewModel: TransactionViewModel
    private var downloadNumber: Int = 0

    fun setter(mainActivity: MainActivity, batchDB: BatchDB, transactionViewModel: TransactionViewModel,slipDB: SlipDB){
        this.mainActivity = mainActivity
        this.batchDB = batchDB //TODO batch viewModel olacak
        this.transactionViewModel = transactionViewModel
        this.slipDB = slipDB
    }

    /** It runs functions in parallel while ui updating dynamically in main thread
     * It also calls finishBatchClose functions in parallel in IO coroutine thread.
     */
    suspend fun doInBackground(): BatchCloseResponse?{

        var batchCloseResponse: BatchCloseResponse? = null
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        coroutineScope.launch(Dispatchers.Main){
            mainActivity.showDialog(dialog)
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

    /** It gets all transactions from transaction View Model, then makes up slip from printService.
     * Lastly insert this slip to database, to print it again in next day. If it inserts it successfully, ui is updating
     * with Success Message. Lastly, update Batch number and resets group number and delete all transactions from Transaction Table.
     */
    private fun finishBatchClose(dialog: InfoDialog): BatchCloseResponse{
        val transactions = transactionViewModel.getAllTransactions()

        val printService = BatchClosePrintHelper()
        val printServiceBinding = PrintServiceBinding()
        val slip = printService.batchText(batchDB.getBatchNo().toString(),transactions,mainActivity,true)
        Log.d("Repetition",slip)
        if (slipDB.insertSlip(slip)){
            coroutineScope.async(Dispatchers.Main) {
                dialog.update(InfoDialog.InfoType.Confirmed, "Grup Kapama Başarılı")
            }
        }
        printServiceBinding.print(slip)
        batchDB.updateBatchNo(batchDB.getBatchNo()!! + 1)
        transactionViewModel.deleteAll()
        return BatchCloseResponse(BatchResult.SUCCESS, SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault()))
    }

}