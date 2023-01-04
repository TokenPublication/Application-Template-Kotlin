package com.tokeninc.sardis.application_template

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.database.batch.BatchDB
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.helpers.printHelpers.DateUtil
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import kotlinx.coroutines.*

class TransactionService  {

    var context: Context? = null
    private var downloadNumber: Int = 0
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    var mainActivity: MainActivity? = null
    var transactionDB: TransactionDB? = null
    var batchDB: BatchDB? = null
    var transactionViewModel: TransactionViewModel? = null

    suspend fun doInBackground(context: Context, amount: Int, card: ICCCard, transactionCode: TransactionCode, extraContent: ContentValues,
                               onlinePin: String?, isPinByPass: Boolean, uuid: String?, isOffline: Boolean):TransactionResponse? {
        this.context = context
        var transactionResponse: TransactionResponse? = null
        val dialog = if (transactionCode == TransactionCode.SALE)
            InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        else
            InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",true)

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
                Log.d("DownloadNumb",downloadNumber.toString())
                if (downloadNumber == 10){
                    coroutineScope.async(Dispatchers.Main) {
                        dialog.update(InfoDialog.InfoType.Confirmed, "İşlem Tamamlandı")
                    }
                    val deferred = coroutineScope.async(Dispatchers.IO) {
                    val onlineTransactionResponse = parseResponse(card,extraContent,transactionCode)
                        finishTransaction(context,amount, card,transactionCode,extraContent,onlinePin,isPinByPass,uuid,isOffline,onlineTransactionResponse!!, ResponseCode.SUCCESS)
                    }
                    transactionResponse = deferred.await()
                }
            }
        }.join() //wait that job to finish to return it
        return transactionResponse
    }

    private fun parseResponse(card: ICCCard, contentVal: ContentValues?, transactionCode: TransactionCode): OnlineTransactionResponse?{
        val onlineTransactionResponse = OnlineTransactionResponse()
        onlineTransactionResponse.mResponseCode = ResponseCode.SUCCESS
        onlineTransactionResponse.mTextPrintCode1 = "Test Print 1"
        onlineTransactionResponse.mTextPrintCode2 = "Test Print 2"
        onlineTransactionResponse.mAuthCode = "12345"
        onlineTransactionResponse.mHostLogKey = "12345678"
        onlineTransactionResponse.mDisplayData = "Display Data"
        onlineTransactionResponse.mKeySequenceNumber = "3"
        onlineTransactionResponse.insCount = 0
        onlineTransactionResponse.instAmount = 0
        onlineTransactionResponse.dateTime = "${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}"
        return onlineTransactionResponse
    }

    private fun finishTransaction(context: Context, amount: Int, card: ICCCard, transactionCode: TransactionCode, extraContent: ContentValues?, onlinePin: String?,
                                  isPinByPass: Boolean, uuid: String?, isOffline: Boolean, onlineTransactionResponse: OnlineTransactionResponse, responseCode: ResponseCode): TransactionResponse? {

        val content = ContentValues()
        content.put(TransactionCol.Col_UUID.name, uuid)
        content.put(TransactionCol.Col_STN.name, batchDB?.getSTN())
        content.put(TransactionCol.Col_GUP_SN.name, batchDB?.updateSTN())
        content.put(TransactionCol.Col_BatchNo.name, batchDB?.getBatchNo())
        content.put(TransactionCol.Col_ReceiptNo.name, 2) // TODO Check Receipt NO 1000TR
        content.put(TransactionCol.Col_CardReadType.name, card.mCardReadType)
        content.put(TransactionCol.Col_PAN.name, card.mCardNumber)
        content.put(TransactionCol.Col_CardSequenceNumber.name, card.CardSeqNum)
        content.put(TransactionCol.Col_TransCode.name, transactionCode.name)
        content.put(TransactionCol.Col_Amount.name, amount)
        content.put(TransactionCol.Col_Amount2.name, amount)
        content.put(TransactionCol.Col_ExpDate.name, card.mExpireDate)
        content.put(TransactionCol.Col_Track2.name, card.mTrack2Data)
        content.put(TransactionCol.Col_CustName.name, card.ownerName)
        content.put(TransactionCol.Col_IsVoid.name, 0)
        content.put(TransactionCol.Col_isPinByPass.name, isPinByPass)
        content.put(TransactionCol.Col_isOffline.name, isOffline)
        content.put(TransactionCol.Col_InstCnt.name, onlineTransactionResponse.insCount)
        content.put(TransactionCol.Col_InstAmount.name, onlineTransactionResponse.instAmount)
        content.put(TransactionCol.Col_TranDate.name, "${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}")
        content.put(TransactionCol.Col_TranDate2.name, "Col_TranDate2") //TODO: If void get void date from OnlineTransactionResponse, bu yok
        content.put(TransactionCol.Col_HostLogKey.name, onlineTransactionResponse.mHostLogKey)
        if (transactionCode == TransactionCode.VOID)
            content.put(TransactionCol.Col_VoidDateTime.name, "${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}")
        else
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

        var success = true
        // TODO BARIS  VOID ise Insert etmeyecek!
        if (responseCode == ResponseCode.SUCCESS && transactionCode == TransactionCode.SALE) {
            success =
                runBlocking {
                    transactionViewModel!!.insertTransaction(content)
                }
            success = true
        }

        // TODO: TEST USE for get all transactions as content values list
        val allTransactions: List<ContentValues?> = transactionViewModel!!.getAllTransactions()
        allTransactions.forEach(::println)

        if (success) {
            return TransactionResponse(responseCode, onlineTransactionResponse, content, extraContent, transactionCode)
        } // TODO: Detailed response will be implemented
        return TransactionResponse(responseCode, onlineTransactionResponse, content, extraContent, transactionCode)
        //return null // TODO: if error DB insert, return error...
    }
}
