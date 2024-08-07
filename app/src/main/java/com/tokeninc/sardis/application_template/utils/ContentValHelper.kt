package com.tokeninc.sardis.application_template.utils

import android.content.ContentValues
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.database.transaction.TransactionCols

class ContentValHelper {
    fun getContentVal(transaction: Transaction): ContentValues{
        val contentVal = ContentValues()
        contentVal.put(TransactionCols.Col_UUID,transaction.Col_UUID)
        contentVal.put(TransactionCols.Col_GUP_SN,transaction.Col_GUP_SN)
        contentVal.put(TransactionCols.Col_BatchNo,transaction.Col_BatchNo)
        contentVal.put(TransactionCols.Col_ReceiptNo,transaction.Col_ReceiptNo)
        contentVal.put(TransactionCols.Col_CardReadType,transaction.Col_CardReadType)
        contentVal.put(TransactionCols.Col_PAN,transaction.Col_PAN)
        contentVal.put(TransactionCols.Col_CardSequenceNumber,transaction.Col_CardSequenceNumber)
        contentVal.put(TransactionCols.Col_TransCode,transaction.Col_TransCode)
        contentVal.put(TransactionCols.Col_Amount,transaction.Col_Amount)
        contentVal.put(TransactionCols.Col_Amount2,transaction.Col_Amount2)
        contentVal.put(TransactionCols.Col_ExpDate,transaction.Col_ExpDate)
        contentVal.put(TransactionCols.Col_Track2,transaction.Col_Track2)
        contentVal.put(TransactionCols.Col_CustomerName,transaction.Col_CustomerName)
        contentVal.put(TransactionCols.Col_IsVoid,transaction.Col_IsVoid)
        contentVal.put(TransactionCols.Col_InstCnt,transaction.Col_InstCnt)
        contentVal.put(TransactionCols.Col_TranDate,transaction.Col_TranDate)
        contentVal.put(TransactionCols.Col_RefNo,transaction.Col_RefNo)
        contentVal.put(TransactionCols.Col_VoidDateTime,transaction.Col_VoidDateTime)
        contentVal.put(TransactionCols.Col_AuthCode,transaction.Col_AuthCode)
        contentVal.put(TransactionCols.Col_Aid,transaction.Col_Aid)
        contentVal.put(TransactionCols.Col_AidLabel,transaction.Col_AidLabel)
        contentVal.put(TransactionCols.Col_TextPrintCode,transaction.Col_TextPrintCode)
        contentVal.put(TransactionCols.Col_DisplayData,transaction.Col_DisplayData)
        contentVal.put(TransactionCols.Col_KeySequenceNumber,transaction.Col_KeySequenceNumber)
        contentVal.put(TransactionCols.Col_isPinByPass,transaction.Col_isPinByPass)
        contentVal.put(TransactionCols.Col_isOffline,transaction.Col_isOffline)
        contentVal.put(TransactionCols.Col_AC,transaction.Col_AC)
        contentVal.put(TransactionCols.Col_CID,transaction.Col_CID)
        contentVal.put(TransactionCols.Col_ATC,transaction.Col_ATC)
        contentVal.put(TransactionCols.Col_TVR,transaction.Col_TVR)
        contentVal.put(TransactionCols.Col_TSI,transaction.Col_TSI)
        contentVal.put(TransactionCols.Col_AIP,transaction.Col_AIP)
        contentVal.put(TransactionCols.Col_CVM,transaction.Col_CVM)
        contentVal.put(TransactionCols.Col_AID2,transaction.Col_AID2)
        contentVal.put(TransactionCols.Col_UN,transaction.Col_UN)
        contentVal.put(TransactionCols.Col_IAD,transaction.Col_IAD)
        contentVal.put(TransactionCols.Col_SID,transaction.Col_SID)
        contentVal.put(TransactionCols.Col_Ext_RefundDateTime,transaction.Col_Ext_RefundDateTime)
        contentVal.put(TransactionCols.col_ulSTN,transaction.col_ulSTN)
        contentVal.put(TransactionCols.col_ZNO,transaction.ZNO)
        contentVal.put(TransactionCols.Col_is_onlinePIN,transaction.isOnlinePIN)
        contentVal.put(TransactionCols.col_stChipData,transaction.Col_ChipData)
        contentVal.put(TransactionCols.col_isSignature,transaction.Col_IsSignature)
        return contentVal
    }

    fun getTransaction(contentVal: ContentValues): Transaction {
        val colUUID = contentVal.getAsString(TransactionCols.Col_UUID)
        val colulSTN = contentVal.getAsString(TransactionCols.col_ulSTN).toInt()
        val colGUPSN = contentVal.getAsString(TransactionCols.Col_GUP_SN).toInt()
        val colBatchNo = contentVal.getAsString(TransactionCols.Col_BatchNo).toInt()
        val colReceiptNo = contentVal.getAsString(TransactionCols.Col_ReceiptNo)?.toInt()
        val colZNo = contentVal.getAsString(TransactionCols.col_ZNO)
        val colCardReadType = contentVal.getAsString(TransactionCols.Col_CardReadType).toInt()
        val colPAN = contentVal.getAsString(TransactionCols.Col_PAN)
        val colCardSequenceNumber = contentVal.getAsString(TransactionCols.Col_CardSequenceNumber)
        val colTransCode = contentVal.getAsString(TransactionCols.Col_TransCode).toInt()
        val colAmount = contentVal.getAsString(TransactionCols.Col_Amount).toInt()
        val colAmount2 = contentVal.getAsString(TransactionCols.Col_Amount2).toInt()
        val colExpDate = contentVal.getAsString(TransactionCols.Col_ExpDate)
        val colTrack2 = contentVal.getAsString(TransactionCols.Col_Track2)
        val colCustName =  contentVal.getAsString(TransactionCols.Col_CustomerName)
        val colIsVoid = contentVal.getAsString(TransactionCols.Col_IsVoid).toInt()
        val colInstCnt = contentVal.getAsString(TransactionCols.Col_InstCnt)?.toInt()
        val colTranDate = contentVal.getAsString(TransactionCols.Col_TranDate)
        val colHostLogKey = contentVal.getAsString(TransactionCols.Col_RefNo)
        val colVoidDateTime = contentVal.getAsString(TransactionCols.Col_VoidDateTime)
        val colChipData = contentVal.getAsString(TransactionCols.col_stChipData)
        val colIsSignature = contentVal.getAsString(TransactionCols.col_isSignature).toInt()
        val colAuthCode = contentVal.getAsString(TransactionCols.Col_AuthCode)
        val colAid = contentVal.getAsString(TransactionCols.Col_Aid)
        val colAidLabel = contentVal.getAsString(TransactionCols.Col_AidLabel)
        val colTextPrintCode1 = contentVal.getAsString(TransactionCols.Col_TextPrintCode)
        val colDisplayData = contentVal.getAsString(TransactionCols.Col_DisplayData)
        val colKeySequenceNumber = contentVal.getAsString(TransactionCols.Col_KeySequenceNumber)
        val colIsPinByPass = contentVal.getAsString(TransactionCols.Col_isPinByPass).toInt()
        val colIsOffline = contentVal.getAsString(TransactionCols.Col_isOffline).toInt()
        val colIsOnlinePIN = contentVal.getAsString(TransactionCols.Col_is_onlinePIN)?.toInt()
        val colAC = contentVal.getAsString(TransactionCols.Col_AC)
        val colCID = contentVal.getAsString(TransactionCols.Col_CID)
        val colATC = contentVal.getAsString(TransactionCols.Col_ATC)
        val colTVR = contentVal.getAsString(TransactionCols.Col_TVR)
        val colTSI = contentVal.getAsString(TransactionCols.Col_TSI)
        val colAIP = contentVal.getAsString(TransactionCols.Col_AIP)
        val colCVM = contentVal.getAsString(TransactionCols.Col_CVM)
        val colAID2 = contentVal.getAsString(TransactionCols.Col_AID2)
        val colUN = contentVal.getAsString(TransactionCols.Col_UN)
        val colIAD = contentVal.getAsString(TransactionCols.Col_IAD)
        val colSID = contentVal.getAsString(TransactionCols.Col_SID)
        val colExtRefundDateTime = contentVal.getAsString(TransactionCols.Col_Ext_RefundDateTime)
        return Transaction(
            colUUID,
            colulSTN,
            colGUPSN,
            colBatchNo,
            colReceiptNo,
            colZNo,
            colCardReadType,
            colPAN,
            colCardSequenceNumber,
            colTransCode,
            colAmount,
            colAmount2,
            colExpDate,
            colTrack2,
            colCustName,
            colIsVoid,
            colInstCnt,
            colTranDate,
            colHostLogKey,
            colVoidDateTime,
            colChipData,
            colIsSignature,
            colAuthCode,
            colAid,
            colAidLabel,
            colTextPrintCode1,
            colDisplayData,
            colKeySequenceNumber,
            colIsPinByPass,
            colIsOffline,
            colIsOnlinePIN,
            colAC,
            colCID,
            colATC,
            colTVR,
            colTSI,
            colAIP,
            colCVM,
            colAID2,
            colUN,
            colIAD,
            colSID,
            colExtRefundDateTime
        )
    }
}