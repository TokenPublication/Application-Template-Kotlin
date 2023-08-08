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
import com.tokeninc.sardis.application_template.data.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.data.repositories.BatchRepository
import com.tokeninc.sardis.application_template.data.repositories.TransactionRepository
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import com.tokeninc.sardis.application_template.utils.objects.SampleReceipt
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
    fun createLiveData(cardNumber: String?): MutableList<Transaction?> { //TODO rename
        list.value = getTransactionsByCardNo(cardNumber!!)!!
        return list.value!!.toMutableList()
    }

    fun getTransactionsByRefNo(refNo: String): List<Transaction?>?{
        return transactionRepository.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): List<Transaction?>?{
        return transactionRepository.getTransactionsByCardNo(cardNo)
    }
    private fun insertTransaction(transaction: Transaction){
        viewModelScope.launch(Dispatchers.IO){
            transactionRepository.insertTransaction(transaction)
        }
    }

    private fun setVoid(gupSN: Int, date: String?, card_SID: String?){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.setVoid(gupSN, date, card_SID)
        }
    }

    fun deleteAll(){
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.deleteAll()
        }
    }

    //these variables should only for storing the operation's result and intents' responses.
    // they don't have to be a LiveData because they won't be used for UI updating
    lateinit var refNo: String
    var extraContents : ContentValues? = null

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
     * @param uuid comes from Payment Gateway in Sale Transaction. It can be null
     */
    suspend fun transactionRoutine( amount: Int, card: ICCCard, transactionCode: Int, extraContent: ContentValues,
                                onlinePin: String?, isPinByPass: Boolean, uuid: String?, isOffline: Boolean,
                                    batchViewModel: BatchViewModel, MID: String?, TID: String?,
                                    mainActivity:MainActivity, activationRepository: ActivationRepository) {
        var downloadNumber = 0
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
                    coroutineScope.launch(Dispatchers.IO) {
                        val onlineTransactionResponse = transactionRepository.parseResponse(extraContent,transactionCode)
                        //TODO updateStn buraya
                        coroutineScope.launch(Dispatchers.Main) {
                            uiState.postValue(UIState.Success("Preparing The Data"))
                        }
                        finishTransaction(amount, card,transactionCode,extraContent,onlinePin,isPinByPass,uuid,isOffline,
                            onlineTransactionResponse,batchViewModel,MID, TID, mainActivity,activationRepository)
                    }
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
                                   MID: String?, TID: String?, mainActivity: MainActivity, activationRepository: ActivationRepository
    ) {
        var transactionResponse: TransactionResponse? = null
        var batchNo: Int? = null
        var groupSn: Int? = null
        var responseCode = ResponseCode.ERROR
        if (transactionCode == TransactionCode.VOID.type){ // if it is a void operation
            setVoid(extraContent!!.getAsString(TransactionCols.Col_GUP_SN).toInt(),"${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}",card.SID)
            responseCode = ResponseCode.SUCCESS
            transactionResponse = TransactionResponse(responseCode,onlineTransactionResponse,extraContent,ContentValues(),transactionCode) //it comes from parameters
        } else{
            batchNo = batchViewModel.getBatchNo()
            batchViewModel.updateGUPSN()
            groupSn = batchViewModel.getGroupSN()
            batchViewModel.updateSTN()
            val stn = batchViewModel.getSTN() + 1 //since it's not fast as get the updated version simultaneous it's increased by 1 to equalize updated number.
            transactionResponse = transactionRepository.getTransactionResponse(amount,card,
                transactionCode,extraContent,onlinePin,isPinByPass, uuid,isOffline,onlineTransactionResponse,batchNo,groupSn, stn)
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
        val kmsService = mainActivity.tokenKMS //to use it later
        if (transactionCode == TransactionCode.SALE.type || transactionCode == TransactionCode.INSTALLMENT_SALE.type){
            val transaction = ContentValHelper().getTransaction(transactionResponse.contentVal!!)
            val receipt = SampleReceipt(transaction,activationRepository,batchViewModel.batchRepository)
            val intent = transactionRepository.prepareSaleIntent(transactionResponse,amount, card, mainActivity, receipt)
            liveIntent.postValue(intent)
        }
        else if (transactionCode == TransactionCode.CASH_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type || transactionCode == TransactionCode.MATCHED_REFUND.type){
            val transaction = ContentValHelper().getTransaction(transactionResponse.contentVal!!)
            val receipt = SampleReceipt(transaction,activationRepository,batchViewModel.batchRepository)
            val intent = transactionRepository.prepareRefundIntent(transactionResponse,mainActivity,receipt)
            liveIntent.postValue(intent)
        }
        else if(transactionCode == TransactionCode.VOID.type){
            val receipt = SampleReceipt(ContentValHelper().getTransaction(extraContent!!),activationRepository,batchViewModel.batchRepository)
            val intent = transactionRepository.prepareVoidIntent(transactionResponse,mainActivity,receipt)
            liveIntent.postValue(intent)
        }
    }
}
