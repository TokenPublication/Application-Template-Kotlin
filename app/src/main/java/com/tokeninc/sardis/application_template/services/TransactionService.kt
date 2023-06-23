package com.tokeninc.sardis.application_template.services

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionCols
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.utils.printHelpers.DateUtil
import com.tokeninc.sardis.application_template.data.entities.responses.OnlineTransactionResponse
import com.tokeninc.sardis.application_template.data.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.MainActivity
import kotlinx.coroutines.*

/**
 * This is the Transaction Service class, whose mission is to run processes concurrently.
 */
class TransactionService  {

    private lateinit var context: Context
    private var downloadNumber: Int = 0
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mainActivity: MainActivity
    private lateinit var batchViewModel: BatchViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)

    fun setter(mainActivity: MainActivity, batchViewModel: BatchViewModel, transactionViewModel: TransactionViewModel){
        this.mainActivity = mainActivity
        this.batchViewModel = batchViewModel
        this.transactionViewModel = transactionViewModel
    }

    /** It runs functions in parallel while ui updating dynamically in main thread
     * Additionally, in IO coroutine thread it parses the response and make it OnlineTransactionResponse
     * then call Finish Transaction operation with that parameter.
     * @param extraContent is null if it is sale, refund inputs if it is refund, the whole transaction if it is void type transaction.
     */
    suspend fun doInBackground(context: Context, amount: Int, card: ICCCard, transactionCode: Int, extraContent: ContentValues,
                               onlinePin: String?, isPinByPass: Boolean, uuid: String?, isOffline: Boolean): TransactionResponse? {
        this.context = context
        var transactionResponse: TransactionResponse? = null

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
                if (downloadNumber == 10){
                    val deferred = coroutineScope.async(Dispatchers.IO) {
                        val onlineTransactionResponse = parseResponse(card,extraContent,transactionCode)
                        finishTransaction(context,amount, card,transactionCode,extraContent,onlinePin,isPinByPass,uuid,isOffline,onlineTransactionResponse)
                    }
                    transactionResponse = deferred.await()
                }
            }
        }.join() //wait that job to finish to return it
        return transactionResponse
    }

    /**
     * This is dummy, parsing response with respect to parameters.
     */
    private fun parseResponse(card: ICCCard, contentVal: ContentValues?, transactionCode: Int): OnlineTransactionResponse {
        val onlineTransactionResponse = OnlineTransactionResponse()
        onlineTransactionResponse.mResponseCode = ResponseCode.SUCCESS
        onlineTransactionResponse.mTextPrintCode1 = "Test Print 1"
        onlineTransactionResponse.mTextPrintCode2 = "Test Print 2"
        onlineTransactionResponse.mAuthCode = (0..99999).random().toString()
        onlineTransactionResponse.mHostLogKey = (0..99999999).random().toString()
        onlineTransactionResponse.mDisplayData = "Display Data"
        onlineTransactionResponse.mKeySequenceNumber = "3"
        if (transactionCode == TransactionCode.INSTALLMENT_REFUND.type)
            onlineTransactionResponse.insCount = contentVal!!.getAsString(ExtraKeys.INST_COUNT.name).toInt()
        else
            onlineTransactionResponse.insCount = 0
        onlineTransactionResponse.instAmount = 0
        onlineTransactionResponse.dateTime = "${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}"
        return onlineTransactionResponse
    }

    /** Add values to content with respect to parameters, then if it is Void update transaction as changing isVoid else ->
     * insert that contents to Transaction table and update Group Serial Number of batch table.
     * Update dialog with success message if database operations result without an error.
     */
    private fun finishTransaction(context: Context, amount: Int, card: ICCCard, transactionCode: Int, extraContent: ContentValues?, onlinePin: String?,
                                  isPinByPass: Boolean, uuid: String?, isOffline: Boolean, onlineTransactionResponse: OnlineTransactionResponse
    ): TransactionResponse {

        val content = ContentValues()
        content.put(TransactionCols.Col_UUID, uuid)
        content.put(TransactionCols.Col_BatchNo, batchViewModel.batchNo)
        if (transactionCode != TransactionCode.VOID.type) {
            batchViewModel.updateGUPSN(batchViewModel.groupSN)
            val lst = batchViewModel.allBatch
            val groupSn = batchViewModel.groupSN
            Log.d("groupSn",groupSn.toString())
            content.put(TransactionCols.Col_GUP_SN,groupSn)
            /**
            batchViewModel.groupSN.observe(mainActivity){ //backGroundda observeleyemiyor.
                val groupSN = it
                Log.d("GROUPSN",it.toString())
                content.put(TransactionCols.Col_GUP_SN,groupSN)
            }
            */
        }
        content.put(TransactionCols.Col_ReceiptNo, 2) // TODO Check Receipt NO 1000TR
        content.put(TransactionCols.Col_CardReadType, card.mCardReadType)
        content.put(TransactionCols.Col_PAN, card.mCardNumber)
        content.put(TransactionCols.Col_CardSequenceNumber, card.CardSeqNum)
        content.put(TransactionCols.Col_TransCode, transactionCode)
        content.put(TransactionCols.Col_Amount, amount)
        when (transactionCode) {
            TransactionCode.MATCHED_REFUND.type -> {
                content.put(TransactionCols.Col_Amount2,extraContent!!.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt())
                content.put(TransactionCols.Col_Ext_Conf,extraContent.getAsString(ExtraKeys.AUTH_CODE.name).toInt())
                content.put(TransactionCols.Col_Ext_Ref,extraContent.getAsString(ExtraKeys.REF_NO.name).toInt())
                content.put(TransactionCols.Col_Ext_RefundDateTime,extraContent.getAsString(ExtraKeys.TRAN_DATE.name))
            }
            TransactionCode.INSTALLMENT_REFUND.type -> {
                content.put(TransactionCols.Col_Amount2,extraContent!!.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt())
                content.put(TransactionCols.Col_Ext_RefundDateTime,extraContent.getAsString(ExtraKeys.TRAN_DATE.name))
                content.put(TransactionCols.Col_Ext_Conf,0)
                content.put(TransactionCols.Col_Ext_Ref,0)
            }
            TransactionCode.CASH_REFUND.type -> {
                content.put(TransactionCols.Col_Amount2, extraContent!!.getAsString(ExtraKeys.ORG_AMOUNT.name).toInt() )
                content.put(TransactionCols.Col_Ext_Conf,0)
                content.put(TransactionCols.Col_Ext_Ref,0)
                content.put(TransactionCols.Col_Ext_RefundDateTime,"")
            }
            else -> {
                content.put(TransactionCols.Col_Amount2,0)
                content.put(TransactionCols.Col_Ext_Conf,0)
                content.put(TransactionCols.Col_Ext_Ref,0)
                content.put(TransactionCols.Col_Ext_RefundDateTime,"")
            }
        }
        content.put(TransactionCols.Col_ExpDate, card.mExpireDate)
        content.put(TransactionCols.Col_Track2, card.mTrack2Data)
        content.put(TransactionCols.Col_CustName, card.ownerName)
        content.put(TransactionCols.Col_IsVoid, 0)
        if (isPinByPass)
            content.put(TransactionCols.Col_isPinByPass, 1)
        else
            content.put(TransactionCols.Col_isPinByPass, 0)
        if (isOffline)
            content.put(TransactionCols.Col_isOffline, 1)
        else
            content.put(TransactionCols.Col_isOffline, 0)
        content.put(TransactionCols.Col_InstCnt, onlineTransactionResponse.insCount)
        Log.d("Inst cnt","${onlineTransactionResponse.insCount}")
        content.put(TransactionCols.Col_InstAmount, onlineTransactionResponse.instAmount)
        content.put(TransactionCols.Col_TranDate, "${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}")
        content.put(TransactionCols.Col_HostLogKey, onlineTransactionResponse.mHostLogKey)
        content.put(TransactionCols.Col_VoidDateTime, "")
        content.put(TransactionCols.Col_AuthCode, onlineTransactionResponse.mAuthCode)
        content.put(TransactionCols.Col_Aid, card.AID2)
        content.put(TransactionCols.Col_AidLabel, card.AIDLabel)
        content.put(TransactionCols.Col_TextPrintCode1, onlineTransactionResponse.mTextPrintCode1)
        content.put(TransactionCols.Col_TextPrintCode2, onlineTransactionResponse.mTextPrintCode2)
        content.put(TransactionCols.Col_DisplayData, onlineTransactionResponse.mDisplayData)
        content.put(TransactionCols.Col_KeySequenceNumber, onlineTransactionResponse.mKeySequenceNumber)
        content.put(TransactionCols.Col_AC, card.AC)
        content.put(TransactionCols.Col_CID, card.CID)
        content.put(TransactionCols.Col_ATC, card.ATC)
        content.put(TransactionCols.Col_TVR, card.TVR)
        content.put(TransactionCols.Col_TSI, card.TSI)
        content.put(TransactionCols.Col_AIP, card.AIP)
        content.put(TransactionCols.Col_CVM, card.CVM)
        content.put(TransactionCols.Col_AID2, card.AID2)
        content.put(TransactionCols.Col_UN, card.UN)
        content.put(TransactionCols.Col_IAD, card.IAD)
        content.put(TransactionCols.Col_SID, card.SID)
        Log.d("Service","Transaction Code: $transactionCode")
        var success = false
        var responseCode = ResponseCode.ERROR
        if (transactionCode == TransactionCode.VOID.type){
            transactionViewModel.setVoid(extraContent!!.getAsString(TransactionCols.Col_GUP_SN).toInt(),"${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}",card.SID)
        }
        else if (transactionCode != 0){
            transactionViewModel.insertTransaction(ContentValHelper().getTransaction(content))
            Log.d("Service","Success: ")
        }
        //TODO CONTROLLER EKLE
        //if (success) {
        responseCode = ResponseCode.SUCCESS
        coroutineScope.launch(Dispatchers.Main) {
            dialog.update(InfoDialog.InfoType.Confirmed, "İşlem Tamamlandı")
        } //TODO join gerekiyor mu ?
        return TransactionResponse(responseCode, onlineTransactionResponse, content, extraContent, transactionCode)
        //} // TODO: Detailed response will be implemented

        //return TransactionResponse(responseCode, onlineTransactionResponse, content, extraContent, transactionCode)
        //return null // TODO: if error DB insert, return error...
    }
}
