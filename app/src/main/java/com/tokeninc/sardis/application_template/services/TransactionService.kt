package com.tokeninc.sardis.application_template.services

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.ui.MainActivity
import com.tokeninc.sardis.application_template.database.batch.BatchDB
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.helpers.printHelpers.DateUtil
import com.tokeninc.sardis.application_template.responses.OnlineTransactionResponse
import com.tokeninc.sardis.application_template.responses.TransactionResponse
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import kotlinx.coroutines.*

/**
 * This is the Transaction Service class, whose mission is to run processes concurrently.
 */
class TransactionService  {

    private lateinit var context: Context
    private var downloadNumber: Int = 0
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var mainActivity: MainActivity
    private lateinit var batchDB: BatchDB
    private lateinit var transactionViewModel: TransactionViewModel
    private val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)

    fun setter(mainActivity: MainActivity, batchDB: BatchDB, transactionViewModel: TransactionViewModel){
        this.mainActivity = mainActivity
        this.batchDB = batchDB
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
    private fun parseResponse(card: ICCCard, contentVal: ContentValues?, transactionCode: Int): OnlineTransactionResponse{
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
                                  isPinByPass: Boolean, uuid: String?, isOffline: Boolean, onlineTransactionResponse: OnlineTransactionResponse): TransactionResponse {

        val content = ContentValues()
        content.put(TransactionCol.Col_UUID.name, uuid)
        content.put(TransactionCol.Col_BatchNo.name, batchDB.getBatchNo())
        if (transactionCode != TransactionCode.VOID.type) {
            content.put(TransactionCol.Col_GUP_SN.name, batchDB.updateGUPSN())
        }
        content.put(TransactionCol.Col_ReceiptNo.name, 2) // TODO Check Receipt NO 1000TR
        content.put(TransactionCol.Col_CardReadType.name, card.mCardReadType)
        content.put(TransactionCol.Col_PAN.name, card.mCardNumber)
        content.put(TransactionCol.Col_CardSequenceNumber.name, card.CardSeqNum)
        content.put(TransactionCol.Col_TransCode.name, transactionCode)
        content.put(TransactionCol.Col_Amount.name, amount)
        when (transactionCode) {
            TransactionCode.MATCHED_REFUND.type -> {
                content.put(TransactionCol.Col_Amount2.name,extraContent!!.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt())
                content.put(TransactionCol.Col_Ext_Conf.name,extraContent.getAsString(ExtraKeys.AUTH_CODE.name).toInt())
                content.put(TransactionCol.Col_Ext_Ref.name,extraContent.getAsString(ExtraKeys.REF_NO.name).toInt())
                content.put(TransactionCol.Col_Ext_RefundDateTime.name,extraContent.getAsString(ExtraKeys.TRAN_DATE.name))
            }
            TransactionCode.INSTALLMENT_REFUND.type -> {
                content.put(TransactionCol.Col_Amount2.name,extraContent!!.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt())
                content.put(TransactionCol.Col_Ext_RefundDateTime.name,extraContent.getAsString(ExtraKeys.TRAN_DATE.name))
            }
            TransactionCode.CASH_REFUND.type -> {
                content.put(TransactionCol.Col_Amount2.name, extraContent!!.getAsString(ExtraKeys.ORG_AMOUNT.name).toInt() )
            }
            else -> {
                content.put(TransactionCol.Col_Amount2.name,0)
                content.put(TransactionCol.Col_Ext_Conf.name,0)
                content.put(TransactionCol.Col_Ext_Ref.name,0)
                content.put(TransactionCol.Col_Ext_RefundDateTime.name,"")
            }
        }
        content.put(TransactionCol.Col_ExpDate.name, card.mExpireDate)
        content.put(TransactionCol.Col_Track2.name, card.mTrack2Data)
        content.put(TransactionCol.Col_CustName.name, card.ownerName)
        content.put(TransactionCol.Col_IsVoid.name, 0)
        content.put(TransactionCol.Col_isPinByPass.name, isPinByPass)
        content.put(TransactionCol.Col_isOffline.name, isOffline)
        content.put(TransactionCol.Col_InstCnt.name, onlineTransactionResponse.insCount)
        Log.d("Inst cnt","${onlineTransactionResponse.insCount}")
        content.put(TransactionCol.Col_InstAmount.name, onlineTransactionResponse.instAmount)
        content.put(TransactionCol.Col_TranDate.name, "${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}")
        content.put(TransactionCol.Col_HostLogKey.name, onlineTransactionResponse.mHostLogKey)
        content.put(TransactionCol.Col_VoidDateTime.name, "")
        content.put(TransactionCol.Col_AuthCode.name, onlineTransactionResponse.mAuthCode)
        content.put(TransactionCol.Col_Aid.name, card.AID2)
        content.put(TransactionCol.Col_AidLabel.name, card.AIDLabel)
        content.put(TransactionCol.Col_TextPrintCode1.name, onlineTransactionResponse.mTextPrintCode1)
        content.put(TransactionCol.Col_TextPrintCode2.name, onlineTransactionResponse.mTextPrintCode2)
        content.put(TransactionCol.Col_DisplayData.name, onlineTransactionResponse.mDisplayData)
        content.put(TransactionCol.Col_KeySequenceNumber.name, onlineTransactionResponse.mKeySequenceNumber)
        content.put(TransactionCol.Col_AC.name, card.AC)
        content.put(TransactionCol.Col_CID.name, card.CID)
        content.put(TransactionCol.Col_ATC.name, card.ATC)
        content.put(TransactionCol.Col_TVR.name, card.TVR)
        content.put(TransactionCol.Col_TSI.name, card.TSI)
        content.put(TransactionCol.Col_AIP.name, card.AIP)
        content.put(TransactionCol.Col_CVM.name, card.CVM)
        content.put(TransactionCol.Col_AID2.name, card.AID2)
        content.put(TransactionCol.Col_UN.name, card.UN)
        content.put(TransactionCol.Col_IAD.name, card.IAD)
        content.put(TransactionCol.Col_SID.name, card.SID)
        Log.d("Service","Transaction Code: $transactionCode")
        var success = false
        var responseCode = ResponseCode.ERROR
        if (transactionCode == TransactionCode.VOID.type){
            success = transactionViewModel.setVoid(extraContent!!.getAsString(TransactionCol.Col_GUP_SN.name).toInt(),"${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}",card)
        }
        else if (transactionCode != 0){
            success = transactionViewModel.insertTransaction(content)
            Log.d("Service","Success: $success")
        }

        if (success) {
            responseCode = ResponseCode.SUCCESS
            coroutineScope.async(Dispatchers.Main) {
                dialog.update(InfoDialog.InfoType.Confirmed, "İşlem Tamamlandı")
            }
            return TransactionResponse(responseCode, onlineTransactionResponse, content, extraContent, transactionCode)
        } // TODO: Detailed response will be implemented

        return TransactionResponse(responseCode, onlineTransactionResponse, content, extraContent, transactionCode)
        //return null // TODO: if error DB insert, return error...
    }
}