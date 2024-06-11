package com.tokeninc.sardis.application_template.ui.postTxn.batch

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.model.resultCode.BatchResult
import com.tokeninc.sardis.application_template.data.repositories.BatchRepository
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchViewModel @Inject constructor(private val batchRepository: BatchRepository): ViewModel() {

    fun getGroupSN()  = batchRepository.getGroupSN()
    fun getBatchNo() = batchRepository.getBatchNo()
    fun getSTN() = batchRepository.getSTN()
    fun getPreviousBatchSlip(): LiveData<String?> = batchRepository.getPreviousBatchSlip()

    /**
     * This function works in IO thread, so it doesn't lock the main thread.
     * It increases batch number 1 and make groupSn 1
     */
    private fun updateBatchNo(){
        viewModelScope.launch(Dispatchers.IO) {
            batchRepository.updateBatchNo()
        }
    }

    /**
     * This function works in IO thread, so it doesn't lock the main thread.
     * It updates the previous batch slip
     */
    private fun updateBatchSlip(batchSlip: String?){
        viewModelScope.launch(Dispatchers.IO){
            batchRepository.updateBatchSlip(batchSlip)
        }
    }

    /**
     * This function works in IO thread, so it doesn't lock the main thread.
     * It increases the group Serial No as one.
     */
    fun updateGUPSN(){
        viewModelScope.launch(Dispatchers.IO) {
            batchRepository.updateGUPSN()
        }
    }

    /**
     * This function works in IO thread, so it doesn't lock the main thread.
     * It increases STN as one.
     */
    fun updateSTN(){
        viewModelScope.launch(Dispatchers.IO) {
            batchRepository.updateSTN()
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    //this is for storing UI state, it is observed from the fragment to update UI
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

    /** It runs functions in parallel while ui updating dynamically in main thread with UI States
     * It also calls finishBatchClose functions in parallel in IO coroutine thread.
     */
    suspend fun batchCloseRoutine(mainActivity: MainActivity, transactionViewModel: TransactionViewModel, activationViewModel: ActivationViewModel){
        var downloadNumber = 0
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
                finishBatchClose(mainActivity,transactionViewModel,activationViewModel)
            }
        }.join()
    }

    /** It gets all transactions from transaction View Model, then makes up slip from printService.
     * Lastly insert this slip to database, to print it again in next day. If it inserts it successfully, ui is updating
     * with Success Message. Finally, update Batch number and resets group number and delete all transactions from Transaction Table.
     */
    private fun finishBatchClose(mainActivity: MainActivity, transactionViewModel: TransactionViewModel,activationViewModel: ActivationViewModel) {
        val transactions = transactionViewModel.allTransactions() //get all transactions from viewModel
        val copySlip = batchRepository.prepareSlip(mainActivity,activationViewModel,transactions,true)
        updateBatchSlip(copySlip) //update the batch slip for previous day
        val slip = batchRepository.prepareSlip(mainActivity,activationViewModel,transactions,false)
        updateBatchNo() //update the batch number
        transactionViewModel.deleteAll() //delete all the transactions
        val batchCloseResponse = batchRepository.prepareResponse(BatchResult.SUCCESS)
        val intent = batchRepository.prepareBatchIntent(batchCloseResponse,mainActivity,slip) //prepare intent and print slip
        coroutineScope.launch(Dispatchers.Main) {
            uiState.postValue(UIState.Success(mainActivity.getString(R.string.batch_close_success)))
        }
        liveIntent.postValue(intent)
    }

    private val isPrinted = MutableLiveData<Boolean>(false)
    fun getIsPrinted(): LiveData<Boolean> = isPrinted

    /**
     * It prepares and prints current transaction List
     */
    fun printTransactionListSlip(activity: FragmentActivity, activationViewModel: ActivationViewModel, transactions: List<Transaction?>?){
        viewModelScope.launch(Dispatchers.IO) {
            val transactionListSlip = batchRepository.prepareSlip(activity, activationViewModel, transactions, false, false)
            batchRepository.print(transactionListSlip, activity)
            isPrinted.postValue(true)
        }
    }

    /**
     * It prints previous batchSlip
     */
    fun printPreviousBatchSlip(activity: FragmentActivity, batchSlip: String?){
        viewModelScope.launch(Dispatchers.IO){
            batchRepository.print(batchSlip,activity)
            isPrinted.postValue(true)
        }
    }

}
