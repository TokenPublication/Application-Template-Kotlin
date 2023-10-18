package com.tokeninc.sardis.application_template.data.repositories

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionCols
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionDao
import com.tokeninc.sardis.application_template.data.model.card.ICCCard
import com.tokeninc.sardis.application_template.data.model.responses.OnlineTransactionResponse
import com.tokeninc.sardis.application_template.data.model.responses.TransactionResponse
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.data.model.type.PaymentType
import com.tokeninc.sardis.application_template.data.model.type.SlipType
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import com.tokeninc.sardis.application_template.utils.ExtraKeys
import com.tokeninc.sardis.application_template.utils.StringHelper
import com.tokeninc.sardis.application_template.utils.objects.SampleReceipt
import com.tokeninc.sardis.application_template.utils.printHelpers.DateUtil
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintHelper
import com.tokeninc.sardis.application_template.utils.printHelpers.TransactionPrintHelper
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {

    suspend fun allTransactions(): List<Transaction?>? = transactionDao.getAllTransactions()

    fun getTransactionsByRefNo(refNo: String): List<Transaction?>?{
        return transactionDao.getTransactionsByRefNo(refNo)
    }

    fun getTransactionsByCardNo(cardNo: String): List<Transaction?>{
        return transactionDao.getTransactionsByCardNo(cardNo)
    }

    suspend fun insertTransaction(transaction: Transaction){
        transactionDao.insertTransaction(transaction)
    }

    suspend fun setVoid(gupSN: Int, date: String?, card_SID: String?){
        transactionDao.setVoid(gupSN,date,card_SID)
    }

    fun isEmpty(): Boolean{
        return (transactionDao.getCount() == 0)
    }

    suspend fun deleteAll(){
        transactionDao.deleteAll()
    }



    /**
     * It parses the response in a dummy way. It represents communication between host and app in a real application
     */
    fun parseResponse (): OnlineTransactionResponse {
        val stringHelper = StringHelper()
        val onlineTransactionResponse = OnlineTransactionResponse()
        onlineTransactionResponse.mResponseCode = ResponseCode.SUCCESS //TODO Developer change it in cases it can connect host
        onlineTransactionResponse.mTextPrintCode = "Test Print"
        onlineTransactionResponse.mAuthCode = stringHelper.addZeros((0..99999).random().toString(),6)
        onlineTransactionResponse.mRefNo = stringHelper.addZeros((0..999999999).random().toString(),10)
        onlineTransactionResponse.mDisplayData = "Display Data"
        onlineTransactionResponse.mKeySequenceNumber = "3"
        onlineTransactionResponse.dateTime = "${DateUtil().getDate("yyyy-MM-dd")} ${DateUtil().getTime("HH:mm:ss")}"
        return onlineTransactionResponse
    }

    /** Add values to content with respect to parameters and transactionCode after that
     * @return TransactionResponse with required parameters for notifying where it is called.
     */
    fun getTransactionResponse (card: ICCCard, transactionCode: Int, onlineTransactionResponse: OnlineTransactionResponse,
                                batchNo: Int, groupSn: Int, stn: Int, bundle: Bundle
    ): TransactionResponse {
        val content = ContentValues()
        val responseCode = ResponseCode.SUCCESS
        val uuid = bundle.getString("UUID")
        val zNO = bundle.getString("ZNO")
        val receiptNo = bundle.getInt("ReceiptNo")
        // transaction columns from parameters
        content.put(TransactionCols.Col_UUID, uuid)
        content.put(TransactionCols.Col_ReceiptNo, receiptNo)
        content.put(TransactionCols.col_ZNO, zNO)
        content.put(TransactionCols.Col_BatchNo, batchNo)
        content.put(TransactionCols.Col_GUP_SN,groupSn)
        content.put(TransactionCols.col_ulSTN, stn)
        content.put(TransactionCols.Col_TransCode, transactionCode)
        content.put(TransactionCols.Col_IsVoid, 0) // Void operations couldn't enter this function therefore it's 0.
        content.put(TransactionCols.col_isSignature,0)
        // if it is different later it will change in if blocks
        content.put(TransactionCols.Col_Amount, card.mTranAmount1)

        // transaction parameters with respect to transaction type
        if (transactionCode == TransactionCode.MATCHED_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type){
            content.put(TransactionCols.Col_Amount2,bundle.getInt(ExtraKeys.REFUND_AMOUNT.name))
            content.put(TransactionCols.Col_Amount,bundle.getInt(ExtraKeys.ORG_AMOUNT.name))
            content.put(TransactionCols.Col_Ext_RefundDateTime,bundle.getString(ExtraKeys.TRAN_DATE.name))
            // these two info is for finding the corresponding sale, they are not this transaction's authCode and refNo
            //content.put(TransactionCols.Col_AuthCode,bundle.getString(ExtraKeys.AUTH_CODE.name))
            //content.put(TransactionCols.Col_RefNo,bundle.getString(ExtraKeys.REF_NO.name))
        } else if (transactionCode == TransactionCode.CASH_REFUND.type){
            content.put(TransactionCols.Col_Amount2, bundle.getInt(ExtraKeys.REFUND_AMOUNT.name))
        } else{
            content.put(TransactionCols.Col_Amount2, 0)
        }
        if (transactionCode == TransactionCode.INSTALLMENT_SALE.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type){
            content.put(TransactionCols.Col_InstCnt,bundle.getInt(ExtraKeys.INST_COUNT.name))
        }

        // transaction parameters comes from card
        content.put(TransactionCols.Col_CardReadType, card.mCardReadType)
        content.put(TransactionCols.Col_PAN, card.mCardNumber)
        content.put(TransactionCols.Col_CardSequenceNumber, card.CardSeqNum)
        content.put(TransactionCols.Col_ExpDate, card.mExpireDate)
        content.put(TransactionCols.Col_Track2, card.mTrack2Data)
        content.put(TransactionCols.Col_CustomerName, card.ownerName)
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
        content.put(TransactionCols.Col_Aid, card.AID2)
        content.put(TransactionCols.Col_AidLabel, card.AIDLabel)
        content.put(TransactionCols.Col_isOffline, 0) //TODO Developer, check for offline transaction
        content.put(TransactionCols.Col_isPinByPass, if(card.isPinByPass()) 1 else 0)
        content.put(TransactionCols.Col_is_onlinePIN, card.OnlPINReq)

        // transaction parameters comes from online Transaction Response
        content.put(TransactionCols.Col_TranDate, onlineTransactionResponse.dateTime)
        content.put(TransactionCols.Col_RefNo, onlineTransactionResponse.mRefNo)
        content.put(TransactionCols.Col_AuthCode, onlineTransactionResponse.mAuthCode)
        content.put(TransactionCols.Col_TextPrintCode, onlineTransactionResponse.mTextPrintCode)
        content.put(TransactionCols.Col_DisplayData, onlineTransactionResponse.mDisplayData)
        content.put(TransactionCols.Col_KeySequenceNumber, onlineTransactionResponse.mKeySequenceNumber)
        Log.i("Number Stn",stn.toString())
        Log.i("Number UUID ",uuid.toString())
        Log.d("Number Transaction Code: ", transactionCode.toString())
        return TransactionResponse(responseCode, onlineTransactionResponse, content, bundle, transactionCode)
    }

    /** This method puts required values to bundle (something like contentValues for data transferring).
     * After that, an intent will be created with this bundle to provide communication between GiB and Application Template via IPC
     */
    fun prepareSaleIntent(transactionResponse: TransactionResponse, card: ICCCard, mainActivity: MainActivity, receipt: SampleReceipt, zNO: String?, receiptNo: Int?)
            : Intent{
        Log.i("Transaction/Response","responseCode:${transactionResponse.responseCode} ContentValues: ${transactionResponse.contentVal}")
        val responseCode = transactionResponse.responseCode
        val batchNo: Int = receipt.batchNo.toInt()
        val groupSN: Int = receipt.groupSerialNo.toInt()
        val merchantID: String? = receipt.merchantID
        val terminalID: String? = receipt.terminalID
        val amount = card.mTranAmount1
        val intent = Intent()
        if (responseCode == ResponseCode.SUCCESS){
            val bundle = Bundle()
            bundle.putInt("ResponseCode", responseCode.ordinal)
            bundle.putString("CardOwner", card.ownerName) // Optional
            bundle.putInt("PaymentStatus", 0) // #2 Payment Status
            bundle.putInt("Amount", amount ) // #3 Amount
            bundle.putBoolean("IsSlip", true)
            bundle.putInt("BatchNo", batchNo)
            bundle.putString("CardNo", if (card.mCardNumber != null) StringHelper().maskCardForBundle(card.mCardNumber!!) else null) // Optional, Card No can be masked
            bundle.putString("MID", merchantID.toString())
            bundle.putString("TID", terminalID.toString())
            bundle.putInt("TxnNo",groupSN)
            bundle.putInt("PaymentType", PaymentType.CREDITCARD.type)

            var slipType: SlipType = SlipType.NO_SLIP
            if (responseCode == ResponseCode.CANCELED || responseCode == ResponseCode.UNABLE_DECLINE || responseCode == ResponseCode.OFFLINE_DECLINE) { //TODO if onlineResponse not successful
                slipType = SlipType.NO_SLIP
            }
            else{
                if (transactionResponse.responseCode == ResponseCode.SUCCESS){
                    val printHelper = TransactionPrintHelper()
                    bundle.putString("customerSlipData", printHelper.getFormattedText( receipt,
                        SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, transactionResponse.transactionCode, mainActivity,zNO, receiptNo,false))
                    bundle.putString("merchantSlipData", printHelper.getFormattedText( receipt,
                        SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, transactionResponse.transactionCode, mainActivity,zNO, receiptNo,false))
                    bundle.putString("RefundInfo", getRefundInfo(ContentValHelper().getTransaction(transactionResponse.contentVal!!),card.mCardNumber,receipt))
                    if(transactionResponse.contentVal != null) {
                        bundle.putString("RefNo", transactionResponse.contentVal!!.getAsString(
                            TransactionCols.Col_RefNo))
                        bundle.putString("AuthNo", transactionResponse.contentVal!!.getAsString(
                            TransactionCols.Col_AuthCode))
                    }
                }
            }
            bundle.putInt("SlipType", slipType.value)
            intent.putExtras(bundle)
        }
        return intent
    }

    /**
     * @return refundInfo which is Json with necessary components
     */
    private fun getRefundInfo(transaction: Transaction, cardNo: String?, receipt: SampleReceipt): String {
        val json = JSONObject()
        try {
            json.put("BatchNo", receipt.batchNo)
            json.put("TxnNo", receipt.groupSerialNo)
            json.put("Amount", transaction.Col_Amount)
            json.put("MID",receipt.merchantID)
            json.put("TID",receipt.terminalID)
            json.put("CardNo",cardNo)
            json.put("RefNo", transaction.Col_RefNo)
            json.put("AuthCode", transaction.Col_AuthCode)
            json.put("TranDate", transaction.Col_TranDate)
            json.put("InstCount", transaction.Col_InstCnt)
            if (transaction.Col_InstCnt != null){
                if (transaction.Col_InstCnt!! > 0){
                    json.put("InstAmount", transaction.Col_Amount/transaction.Col_InstCnt!!)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    /**
     * It prepares refund intent both for gib and normal refund and also print the slip
     */
    fun prepareRefundVoidIntent(transactionResponse: TransactionResponse, mainActivity: MainActivity, receipt: SampleReceipt,
                                zNO: String?, receiptNo: Int?): Intent{
        Log.d("TransactionResponse/Refund", "responseCode:${transactionResponse.responseCode} ContentValues: ${transactionResponse.contentVal}")
        val printHelper = TransactionPrintHelper()
        val customerSlip = printHelper.getFormattedText(receipt, SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, transactionResponse.transactionCode, mainActivity,zNO, receiptNo,false)
        val merchantSlip = printHelper.getFormattedText(receipt, SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, transactionResponse.transactionCode, mainActivity,zNO, receiptNo,false)
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
     * It prepares and prints the slip.
     */
    fun prepareCopySlip(receipt: SampleReceipt, mainActivity: MainActivity, transaction: Transaction,
        transactionCode: Int) {
        val transactionPrintHelper = TransactionPrintHelper()
        val contentValHelper = ContentValHelper()
        val customerSlipData: String = transactionPrintHelper.getFormattedText(receipt,SlipType.CARDHOLDER_SLIP,contentValHelper.getContentVal(transaction),
            transactionCode,mainActivity,transaction.ZNO,transaction.Col_ReceiptNo,true)
        val merchantSlipData: String = transactionPrintHelper.getFormattedText(receipt,SlipType.MERCHANT_SLIP,contentValHelper.getContentVal(transaction),
            transactionCode,mainActivity,transaction.ZNO,transaction.Col_ReceiptNo,true)
        print(customerSlipData,mainActivity)
        print(merchantSlipData,mainActivity)
    }

    private fun print(printText: String?, mainActivity: MainActivity) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(mainActivity.applicationContext))
    }

    fun prepareDummyResponse(
        price: Int,
        code: ResponseCode,
        slipType: SlipType,
        paymentType: Int,
        MID: String?,
        TID: String?,
        mainActivity: MainActivity
    ): Intent {
        val resultIntent = Intent()
        val bundle = Bundle()
        bundle.putInt("Amount",price)
        bundle.putInt("ResponseCode", code.ordinal)
        bundle.putInt("SlipType", slipType.value)
        bundle.putInt("PaymentType", paymentType)
        val message = when (code){
            ResponseCode.SUCCESS -> "İşlem Başarılı"
            ResponseCode.ERROR -> "İşlemde hata ile karşılaşıldı"
            ResponseCode.CANCELED -> "İşlem iptal Edildi"
            ResponseCode.ONLINE_DECLINE -> "İşlem online reddedildi"
            ResponseCode.OFFLINE_DECLINE -> "İşlem offline Reddedildi"
            ResponseCode.UNABLE_DECLINE -> "Unable Decline"
        }
        val slipData = PrintHelper().printDummyResponse(price,MID,TID,message)
        bundle.putString("customerSlipData", slipData)
        bundle.putString("merchantSlipData", slipData)
        //print(slipData, mainActivity) //app temp can print slip if needed
        resultIntent.putExtras(bundle)
        return resultIntent
    }
}
