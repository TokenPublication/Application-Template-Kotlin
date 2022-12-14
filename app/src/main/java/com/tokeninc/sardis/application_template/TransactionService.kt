package com.tokeninc.sardis.application_template

import android.content.ContentValues
import android.content.Context
import android.util.Log
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.database.TransactionCol
import com.tokeninc.sardis.application_template.database.TransactionDB
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import kotlinx.coroutines.*

class TransactionService  {

    var context: Context? = null
    private var downloadNumber: Int = 0
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    var mainActivity: MainActivity? = null
    var transactionDB: TransactionDB? = null

    suspend fun doInBackground(context: Context, card: ICCCard, transactionCode: TransactionCode, extraContent: ContentValues?,
                               onlinePin: String?, isPinByPass: Boolean, uuid: String?, isOffline: Boolean):TransactionResponse? {
        this.context = context
        var transactionResponse: TransactionResponse? = null
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",true)
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
                if (downloadNumber == 10){
                    val deferred = coroutineScope.async(Dispatchers.Main) {
                        dialog.update(InfoDialog.InfoType.Confirmed,"İşlem Tamamlandı")
                        val onlineTransactionResponse = parseResponse(card,extraContent,transactionCode)
                        finishTransaction(context,card,transactionCode,extraContent,onlinePin,isPinByPass,uuid,isOffline,onlineTransactionResponse!!)
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
        onlineTransactionResponse.insCount = "123"
        onlineTransactionResponse.instAmount = 0
        return onlineTransactionResponse
        TODO(); "CHECK FROM DB"
    }

    private fun finishTransaction(context: Context, card: ICCCard, transactionCode: TransactionCode, extraContent: ContentValues?,
                                  onlinePin: String?, isPinByPass: Boolean, uuid: String?, isOffline: Boolean, onlineTransactionResponse: OnlineTransactionResponse):TransactionResponse{

        extraContent!!.put(TransactionCol.Col_Uuid.name, uuid)
        extraContent.put(TransactionCol.Col_UISTN.name, "Col_UISTN")
        extraContent.put(TransactionCol.Col_UIGUP_SN.name, "Col_UIGUP_SN")
        extraContent.put(TransactionCol.Col_BatchNo.name, "Col_BatchNo")
        extraContent.put(TransactionCol.Col_ReceiptNo.name, "Col_ReceiptNo")
        extraContent.put(TransactionCol.Col_CardReadType.name, card.mCardReadType)
        extraContent.put(TransactionCol.Col_PAN.name, card.mCardNumber)
        extraContent.put(TransactionCol.Col_CardSequenceNumber.name, card.CardSeqNum)
        extraContent.put(TransactionCol.Col_TransCode.name, transactionCode.name)
        extraContent.put(TransactionCol.Col_UIAmount.name, card.mTranAmount1)
        extraContent.put(TransactionCol.Col_UIAmount2.name, "Col_UIAmount2")
        extraContent.put(TransactionCol.Col_ExpDate.name, card.mExpireDate)
        extraContent.put(TransactionCol.Col_Track2.name, card.mTrack2Data)
        extraContent.put(TransactionCol.Col_CustName.name, card.ownerName)
        extraContent.put(TransactionCol.Col_IsVoid.name, "Col_IsVoid")
        extraContent.put(TransactionCol.Col_InstCnt.name, onlineTransactionResponse.insCount)
        extraContent.put(TransactionCol.Col_UIInstAmount.name, onlineTransactionResponse.instAmount)
        extraContent.put(TransactionCol.Col_TranDate.name, "Col_TranDate")
        extraContent.put(TransactionCol.Col_HostLogKey.name, onlineTransactionResponse.mHostLogKey)
        extraContent.put(TransactionCol.Col_VoidDateTime.name, "Col_VoidDateTime") //LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        extraContent.put(TransactionCol.Col_AuthCode.name, onlineTransactionResponse.mAuthCode)
        extraContent.put(TransactionCol.Col_Aid.name, "Col_Aid")
        extraContent.put(TransactionCol.Col_AidLabel.name, "Col_AidLabel")
        extraContent.put(TransactionCol.Col_TextPrintCode1.name, onlineTransactionResponse.mTextPrintCode1)
        extraContent.put(TransactionCol.Col_TextPrintCode2.name, onlineTransactionResponse.mTextPrintCode2)
        extraContent.put(TransactionCol.Col_DisplayData.name, onlineTransactionResponse.mDisplayData)
        extraContent.put(TransactionCol.Col_KeySequenceNumber.name, onlineTransactionResponse.mKeySequenceNumber)
        extraContent.put(TransactionCol.Col_AC.name, card.AC)
        extraContent.put(TransactionCol.Col_CID.name, card.CID)
        extraContent.put(TransactionCol.Col_ATC.name, card.ATC)
        extraContent.put(TransactionCol.Col_TVR.name, card.TVR)
        extraContent.put(TransactionCol.Col_TSI.name, card.TSI)
        extraContent.put(TransactionCol.Col_AIP.name, card.AIP)
        extraContent.put(TransactionCol.Col_CVM.name, card.CVM)
        extraContent.put(TransactionCol.Col_AID2.name, card.AID2)
        extraContent.put(TransactionCol.Col_UN.name, card.UN)
        extraContent.put(TransactionCol.Col_IAD.name, card.IAD)

        if (transactionDB!!.getColumn(TransactionCol.Col_Aid.name) == null){ //farklı bir controller lazım ya da
            transactionDB!!.insertContentVal(extraContent)
            Log.d("Service","Insert")
        }
        else{
            transactionDB!!.updateContentVal(extraContent)
            Log.d("Service","Update")
        }
        return TransactionResponse(extraContent,transactionCode)
    }

}