package com.tokeninc.sardis.application_template.ui.posttxn.batch

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.entities.responses.BatchCloseResponse
import com.tokeninc.sardis.application_template.data.repositories.BatchRepository
import com.tokeninc.sardis.application_template.enums.BatchResult
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.utils.printHelpers.BatchClosePrintHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BatchViewModel @Inject constructor(private val batchRepository: BatchRepository): ViewModel() {

    //val groupSN = batchRepository.groupSN
    fun getGroupSN()  = batchRepository.getGroupSN()
    val batchNo = batchRepository.batchNo
    fun getPreviousBatchSlip(): LiveData<String?> = batchRepository.getPreviousBatchSlip()
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


    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val uiState = MutableLiveData<UIState>()

    fun getUiState(): LiveData<UIState> = uiState

    private val liveIntent = MutableLiveData<Intent>()
    fun getLiveIntent(): LiveData<Intent> = liveIntent //it is for observing the intent from saleFragment

    //this is a UI state to update UI in mainActivity
    sealed class UIState {
        object Loading : UIState()
        data class Connecting(val data: Int) : UIState()
        data class Success(val message: String) : UIState()
        // Add more states as needed
    }

    /** It runs functions in parallel while ui updating dynamically in main thread
     * It also calls finishBatchClose functions in parallel in IO coroutine thread.
     */
    suspend fun batchRoutine(mainActivity: MainActivity, transactionViewModel: TransactionViewModel){

        var downloadNumber: Int = 0
        coroutineScope.launch(Dispatchers.Main){//firstly updating the UI as loading
            uiState.postValue(UIState.Loading)
        }

        coroutineScope.launch {
            for (i in 0..10) {
                delay(300L)
                if (downloadNumber < 10) {
                    coroutineScope.launch(Dispatchers.Main) {
                        uiState.postValue(UIState.Connecting(downloadNumber*10))
                    }
                }
                downloadNumber++
            }
            coroutineScope.launch(Dispatchers.IO) {
                finishBatchClose(mainActivity,transactionViewModel)
            }
        }.join()
    }



    /** It gets all transactions from transaction View Model, then makes up slip from printService.
     * Lastly insert this slip to database, to print it again in next day. If it inserts it successfully, ui is updating
     * with Success Message. Lastly, update Batch number and resets group number and delete all transactions from Transaction Table.
     */
    private fun finishBatchClose(mainActivity: MainActivity, transactionViewModel: TransactionViewModel) {
        val transactions = transactionViewModel.allTransactions() //get all transactions from viewModel
        coroutineScope.launch(Dispatchers.Main) {
            uiState.postValue(UIState.Success("Grup Kapama Başarılı"))
        }
        val printService = BatchClosePrintHelper()
        val copySlip = printService.batchText(batchNo.toString(),transactions!!,mainActivity,true)
        updateBatchSlip(copySlip,batchNo) //update the batch slip for previous day
        val slip = printService.batchText(batchNo.toString(),transactions,mainActivity,false)
        updateBatchNo(batchNo) //update the batch number
        transactionViewModel.deleteAll() //delete all the transactions
        val batchCloseResponse = BatchCloseResponse(BatchResult.SUCCESS, SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault()))
        val intent = batchRepository.prepareBatchIntent(batchCloseResponse,mainActivity,slip) //prepare intent and print slip
        liveIntent.postValue(intent)
    }

}