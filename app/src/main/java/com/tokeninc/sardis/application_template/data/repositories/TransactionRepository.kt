package com.tokeninc.sardis.application_template.data.repositories

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionDao
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionCols
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.data.entities.responses.OnlineTransactionResponse
import com.tokeninc.sardis.application_template.data.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.PaymentTypes
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.utils.StringHelper
import com.tokeninc.sardis.application_template.utils.printHelpers.DateUtil
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintService
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {

    suspend fun allTransactions(): List<Transaction?>? = transactionDao.getAllTransactions()

    fun getTransactionsByRefNo(refNo: String): List<Transaction?>?{
        return transactionDao.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): List<Transaction?>?{
        return transactionDao.getTransactionsByCardNo(cardNo)
    }

    suspend fun insertTransaction(transaction: Transaction){
        transactionDao.insertTransaction(transaction)
    }

    suspend fun setVoid(gupSN: Int, date: String?, card_SID: String?){
        transactionDao.setVoid(gupSN,date,card_SID)
    }

    suspend fun deleteAll(){
        transactionDao.deleteAll()
    }

    fun parseResponse(card: ICCCard, contentVal: ContentValues?, transactionCode: Int): OnlineTransactionResponse {
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
    fun getTransactionResponse (amount: Int, card: ICCCard, transactionCode: Int, extraContent: ContentValues?, onlinePin: String?,
                                   isPinByPass: Boolean, uuid: String?, isOffline: Boolean, onlineTransactionResponse: OnlineTransactionResponse,batchNo: Int, groupSn: Int
    ): TransactionResponse {
        val content = ContentValues()
        var responseCode = ResponseCode.SUCCESS //TODO CONTROLLER EKLE
        content.put(TransactionCols.Col_UUID, uuid)
        content.put(TransactionCols.Col_BatchNo, batchNo)
        content.put(TransactionCols.Col_GUP_SN,groupSn)
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
                content.put(
                    TransactionCols.Col_Ext_RefundDateTime,extraContent.getAsString(
                        ExtraKeys.TRAN_DATE.name))
            }
            TransactionCode.INSTALLMENT_REFUND.type -> {
                content.put(TransactionCols.Col_Amount2,extraContent!!.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt())
                content.put(
                    TransactionCols.Col_Ext_RefundDateTime,extraContent.getAsString(
                        ExtraKeys.TRAN_DATE.name))
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
        return TransactionResponse(responseCode, onlineTransactionResponse, content, extraContent, transactionCode)
    }

    /** This method is called from doSale() method. It puts required values to bundle (something like contentValues for data transferring).
     * After that, this bundle is put to intent and that intent is assigned to mainActivity.
     * This intent ensures IPC between application and GiB.
     */
    fun prepareSaleIntent(transactionResponse: TransactionResponse, amount: Int, batchNo: Int, groupSN: Int, card: ICCCard, MID: String?, TID: String?, mainActivity:MainActivity)
            : Intent{
        Log.i("Transaction/Response","responseCode:${transactionResponse.responseCode} ContentVals: ${transactionResponse.contentVal}")
        val responseCode = transactionResponse.responseCode
        val intent = Intent()
        if (responseCode == ResponseCode.SUCCESS){
            val bundle = Bundle()
            bundle.putInt("ResponseCode", responseCode.ordinal) //TODO bunu diğerlerinde de yap
            bundle.putInt("PaymentStatus", 0) // #2 Payment Status
            bundle.putInt("Amount", amount ) // #3 Amount
            bundle.putInt("Amount2", 0)
            bundle.putBoolean("IsSlip", true)
            bundle.putInt("BatchNo", batchNo)
            bundle.putString("CardNo", StringHelper().maskCardForBundle(card.mCardNumber!!))
            bundle.putString("MID", MID.toString())
            bundle.putString("TID", TID.toString())
            bundle.putInt("TxnNo",groupSN)
            bundle.putInt("PaymentType", PaymentTypes.CREDITCARD.type) //TODO check it

            var slipType: SlipType = SlipType.NO_SLIP
            if (responseCode == ResponseCode.CANCELED || responseCode == ResponseCode.UNABLE_DECLINE || responseCode == ResponseCode.OFFLINE_DECLINE) {
                slipType = SlipType.NO_SLIP
            }
            else{
                if (transactionResponse.responseCode == ResponseCode.SUCCESS){
                    val printHelper = PrintService()
                    //TODO maini almadan, saleFragmentta yapmaya çalış.
                    bundle.putString("customerSlipData", printHelper.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, null, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false))
                    bundle.putString("merchantSlipData", printHelper.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, null, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false))
                    bundle.putString("RefundInfo", getRefundInfo(transactionResponse,batchNo,groupSN,amount,MID,TID,card))
                    if(transactionResponse.contentVal != null) {
                        bundle.putString("RefNo", transactionResponse.contentVal!!.getAsString(
                            TransactionCols.Col_HostLogKey))
                        bundle.putString("AuthNo", transactionResponse.contentVal!!.getAsString(
                            TransactionCols.Col_AuthCode))
                    }
                }
            }
            bundle.putInt("SlipType", slipType.value) //TODO fail receipt yap
            intent.putExtras(bundle)
        }
        return intent
    }

    /** @return refundInfo which is Json with necessary components
     *
     */
    private fun getRefundInfo(transactionResponse: TransactionResponse, batchNo: Int, groupSN: Int, amount: Int, MID: String?, TID: String?, card: ICCCard): String {
        val json = JSONObject()
        val transaction = transactionResponse.contentVal
        try {
            json.put("BatchNo", batchNo)
            json.put("TxnNo", groupSN)
            json.put("Amount", amount)
            json.put("RefNo", transaction!!.getAsString(TransactionCols.Col_HostLogKey))
            json.put("AuthCode", transaction.getAsString(TransactionCols.Col_AuthCode))
            json.put("TranDate", transaction.getAsString(TransactionCols.Col_TranDate))
            json.put("MID",MID)
            json.put("TID",TID)
            json.put("CardNo",card.mCardNumber!!)
            if (transaction.getAsInteger(TransactionCols.Col_InstCnt) != null && transaction.getAsInteger(
                    TransactionCols.Col_InstCnt) > 0) {
                //val installment = JSONObject()
                json.put("InstCount", transaction.getAsInteger(TransactionCols.Col_InstCnt))
                json.put("InstAmount", transaction.getAsInteger(TransactionCols.Col_InstAmount))
                //json.put("Installment", installment)
            }
            else{
                json.put("InstCount", 0)
                json.put("InstAmount", 0)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    /**
     * It prepares refund intent both for gib and normal refund and also print the slip
     */
    fun prepareRefundIntent(transactionResponse: TransactionResponse, mainActivity: MainActivity): Intent{
        Log.d("TransactionResponse/Refund", "responseCode:${transactionResponse.responseCode} ContentVals: ${transactionResponse.contentVal}")
        val printHelper = PrintService()
        val customerSlip = printHelper.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!,transactionResponse.extraContent!!, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        val merchantSlip = printHelper.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!,transactionResponse.extraContent!!, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        print(customerSlip, mainActivity)
        print(merchantSlip, mainActivity)
        val responseCode = transactionResponse.responseCode
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)
        return intent
    }

    /**
     * It finishes the void operation via printing slip with respect to achieved data and
     * passes the response code as a result to mainActivity and finishes void transaction.
     */
    //TODO eşlenikli ve taksitli iade iptali slibinde İade miktarı değil, İlk org amount basıldı ona bak
    fun prepareVoidIntent(transactionResponse: TransactionResponse, mainActivity: MainActivity): Intent {
        Log.d("TransactionResponse/PostTxn", "responseCode:${transactionResponse.responseCode} ContentVals: ${transactionResponse.contentVal}")
        val printService = PrintService()
        val customerSlip = printService.getFormattedText(SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, transactionResponse.extraContent, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        val merchantSlip = printService.getFormattedText(SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, transactionResponse.extraContent, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        print(customerSlip,mainActivity)
        print(merchantSlip,mainActivity)
        val responseCode = transactionResponse.responseCode
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)
        return intent
    }

    fun print(printText: String?, mainActivity: MainActivity) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(mainActivity.applicationContext))
    }

}