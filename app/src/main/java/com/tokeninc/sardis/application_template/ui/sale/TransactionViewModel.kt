package com.tokeninc.sardis.application_template.ui.sale

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionCols
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.data.entities.responses.OnlineTransactionResponse
import com.tokeninc.sardis.application_template.data.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.data.repositories.TransactionRepository
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import com.tokeninc.sardis.application_template.utils.printHelpers.DateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val transactionRepository: TransactionRepository): ViewModel() {

    fun allTransactions(): List<Transaction?>? {
        var returnList: List<Transaction?>?
        runBlocking {
            val deferred = viewModelScope.async(Dispatchers.IO){
                transactionRepository.allTransactions()
            }
            returnList = deferred.await()
        }
        return returnList
    }

    val list = MutableLiveData<List<Transaction?>>()
    var menuItemList = mutableListOf<IListMenuItem>()

    /**
     * It is for getting transactions with card number from database for recycler view on Void Operations.
     */
    fun createLiveData(cardNumber: String?): MutableList<Transaction?> {
        list.value = getTransactionsByCardNo(cardNumber!!)!!
        return list.value!!.toMutableList()
    }

    fun getTransactionsByRefNo(refNo: String): List<Transaction?>?{
        return transactionRepository.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): List<Transaction?>?{
        return transactionRepository.getTransactionsByCardNo(cardNo)
    }

    fun insertTransaction(transaction: Transaction){
        viewModelScope.launch(Dispatchers.IO){
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun setVoid(gupSN: Int, date: String?, card_SID: String?){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.setVoid(gupSN, date, card_SID)
        }
    }

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.deleteAll()
        }
    }


    private var downloadNumber: Int = 0
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
     * Additionally, in IO coroutine thread it parses the response and make it OnlineTransactionResponse
     * then call Finish Transaction operation with that parameter.
     * @param extraContent is null if it is sale, refund inputs if it is refund, the whole transaction if it is void type transaction.
     */
    suspend fun transactionRoutine( amount: Int, card: ICCCard, transactionCode: Int, extraContent: ContentValues,
                                onlinePin: String?, isPinByPass: Boolean, uuid: String?, isOffline: Boolean, batchViewModel: BatchViewModel,
    MID: String?, TID: String?, mainActivity:MainActivity) {

        coroutineScope.launch(Dispatchers.Main){//firstly updating the UI as loading
            uiState.postValue(UIState.Loading)
        }

        coroutineScope.launch {
            for (i in 0..10) {
                delay(300L)
                if (downloadNumber < 10) {
                    coroutineScope.launch(Dispatchers.Main) { //update UI in a dummy way
                        uiState.postValue(UIState.Connecting(downloadNumber*10))
                    }
                }
                downloadNumber++
                if (downloadNumber == 10){
                    val deferred = coroutineScope.launch(Dispatchers.IO) {
                        val onlineTransactionResponse = transactionRepository.parseResponse(card,extraContent,transactionCode)
                        coroutineScope.launch(Dispatchers.Main) { //update UI with returnin auth Code
                            uiState.postValue(UIState.Success("Preparing The Data"))
                        }
                        finishTransaction(amount, card,transactionCode,extraContent,onlinePin,isPinByPass,uuid,isOffline,onlineTransactionResponse,batchViewModel,MID, TID, mainActivity)
                    }
                    //deferred.join() //TODO olacak mı burası?
                }
            }
        }.join() //wait that job to finish to return it
    }


    /** Add values to content with respect to parameters, then if it is Void update transaction as changing isVoid else ->
     * insert that contents to Transaction table and update Group Serial Number of batch table.
     * Update dialog with success message if database operations result without an error.
     */
    private fun finishTransaction (amount: Int, card: ICCCard, transactionCode: Int, extraContent: ContentValues?, onlinePin: String?,
                                  isPinByPass: Boolean, uuid: String?, isOffline: Boolean, onlineTransactionResponse: OnlineTransactionResponse, batchViewModel: BatchViewModel,
                                   MID: String?, TID: String?, mainActivity: MainActivity
    ) {
        var transactionResponse: TransactionResponse? = null
        var batchNo: Int? = null
        var groupSn: Int? = null
        var responseCode = ResponseCode.ERROR //TODO CONTROLLER EKLE
        if (transactionCode == TransactionCode.VOID.type){ //if it is Void set it is as a void
            setVoid(extraContent!!.getAsString(TransactionCols.Col_GUP_SN).toInt(),"${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}",card.SID)
            responseCode = ResponseCode.SUCCESS
        } else{
            batchNo = batchViewModel.batchNo
            groupSn = batchViewModel.groupSN
            batchViewModel.updateGUPSN(groupSn)
            transactionResponse = transactionRepository.getTransactionResponse(amount,card,
                transactionCode,extraContent,onlinePin,isPinByPass, uuid,isOffline,onlineTransactionResponse,batchNo,groupSn)
            val responseTransactionCode = transactionResponse.transactionCode
            if (responseTransactionCode != 0){
                val content = transactionResponse.contentVal!!
                insertTransaction(ContentValHelper().getTransaction(content))
                responseCode = ResponseCode.SUCCESS
                Log.d("Service","Success: ")
            }
        }
        if (responseCode == ResponseCode.SUCCESS){
            coroutineScope.launch(Dispatchers.Main) {
                uiState.postValue(UIState.Success("Transaction is Successful"))
            }
        }
        if (transactionCode == TransactionCode.SALE.type){
            val intent = transactionRepository.prepareIntent(transactionResponse!!,amount, batchNo!!, groupSn!!, card,MID, TID, mainActivity)
            liveIntent.postValue(intent)
        }

        //repodan slip bastır
    }

}

