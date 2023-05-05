package com.tokeninc.sardis.application_template.helpers

import android.content.ContentValues
import com.tokeninc.sardis.application_template.database.entities.Transaction
import com.tokeninc.sardis.application_template.entities.col_names.TransactionCols


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
        contentVal.put(TransactionCols.Col_CustName,transaction.Col_CustName)
        contentVal.put(TransactionCols.Col_IsVoid,transaction.Col_IsVoid)
        contentVal.put(TransactionCols.Col_InstCnt,transaction.Col_InstCnt)
        contentVal.put(TransactionCols.Col_InstAmount,transaction.Col_InstAmount)
        contentVal.put(TransactionCols.Col_TranDate,transaction.Col_TranDate)
        contentVal.put(TransactionCols.Col_HostLogKey,transaction.Col_HostLogKey)
        contentVal.put(TransactionCols.Col_VoidDateTime,transaction.Col_VoidDateTime)
        contentVal.put(TransactionCols.Col_AuthCode,transaction.Col_AuthCode)
        contentVal.put(TransactionCols.Col_Aid,transaction.Col_Aid)
        contentVal.put(TransactionCols.Col_AidLabel,transaction.Col_AidLabel)
        contentVal.put(TransactionCols.Col_TextPrintCode1,transaction.Col_TextPrintCode1)
        contentVal.put(TransactionCols.Col_TextPrintCode2,transaction.Col_TextPrintCode2)
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
        contentVal.put(TransactionCols.Col_Ext_Conf,transaction.Col_Ext_Conf)
        contentVal.put(TransactionCols.Col_Ext_Ref,transaction.Col_Ext_Ref)
        contentVal.put(TransactionCols.Col_Ext_RefundDateTime,transaction.Col_Ext_RefundDateTime)
        return contentVal
    }

    fun getTransaction(contentVal: ContentValues): Transaction {
        val Col_UUID = contentVal.getAsString(TransactionCols.Col_UUID)
        val Col_GUP_SN = contentVal.getAsString(TransactionCols.Col_GUP_SN).toInt() //boş geliyor
        val Col_BatchNo = contentVal.getAsString(TransactionCols.Col_BatchNo).toInt()
        val Col_ReceiptNo = contentVal.getAsString(TransactionCols.Col_ReceiptNo).toInt()
        val Col_CardReadType = contentVal.getAsString(TransactionCols.Col_CardReadType).toInt()
        val Col_PAN = contentVal.getAsString(TransactionCols.Col_PAN)
        val Col_CardSequenceNumber = contentVal.getAsString(TransactionCols.Col_CardSequenceNumber)
        val Col_TransCode = contentVal.getAsString(TransactionCols.Col_TransCode).toInt()
        val Col_Amount = contentVal.getAsString(TransactionCols.Col_Amount).toInt()
        val Col_Amount2 = contentVal.getAsString(TransactionCols.Col_Amount2).toInt()
        val Col_ExpDate = contentVal.getAsString(TransactionCols.Col_ExpDate)
        val Col_Track2 = contentVal.getAsString(TransactionCols.Col_Track2)
        val Col_CustName =  contentVal.getAsString(TransactionCols.Col_CustName)
        val Col_IsVoid = contentVal.getAsString(TransactionCols.Col_IsVoid).toInt()
        val Col_InstCnt = contentVal.getAsString(TransactionCols.Col_InstCnt).toInt()
        val Col_InstAmount = contentVal.getAsString(TransactionCols.Col_InstAmount).toInt()
        val Col_TranDate = contentVal.getAsString(TransactionCols.Col_TranDate)
        val Col_HostLogKey = contentVal.getAsString(TransactionCols.Col_HostLogKey)
        val Col_VoidDateTime = contentVal.getAsString(TransactionCols.Col_VoidDateTime)
        val Col_AuthCode = contentVal.getAsString(TransactionCols.Col_AuthCode)
        val Col_Aid = contentVal.getAsString(TransactionCols.Col_Aid)
        val Col_AidLabel = contentVal.getAsString(TransactionCols.Col_AidLabel)
        val Col_TextPrintCode1 = contentVal.getAsString(TransactionCols.Col_TextPrintCode1)
        val Col_TextPrintCode2 = contentVal.getAsString(TransactionCols.Col_TextPrintCode2)
        val Col_DisplayData = contentVal.getAsString(TransactionCols.Col_DisplayData)
        val Col_KeySequenceNumber = contentVal.getAsString(TransactionCols.Col_KeySequenceNumber)
        val Col_isPinByPass = contentVal.getAsString(TransactionCols.Col_isPinByPass).toInt()
        val Col_isOffline = contentVal.getAsString(TransactionCols.Col_isOffline).toInt()
        val Col_AC = contentVal.getAsString(TransactionCols.Col_AC)
        val Col_CID = contentVal.getAsString(TransactionCols.Col_CID)
        val Col_ATC = contentVal.getAsString(TransactionCols.Col_ATC)
        val Col_TVR = contentVal.getAsString(TransactionCols.Col_TVR)
        val Col_TSI = contentVal.getAsString(TransactionCols.Col_TSI)
        val Col_AIP = contentVal.getAsString(TransactionCols.Col_AIP)
        val Col_CVM = contentVal.getAsString(TransactionCols.Col_CVM)
        val Col_AID2 = contentVal.getAsString(TransactionCols.Col_AID2)
        val Col_UN = contentVal.getAsString(TransactionCols.Col_UN)
        val Col_IAD = contentVal.getAsString(TransactionCols.Col_IAD)
        val Col_SID = contentVal.getAsString(TransactionCols.Col_SID)
        val Col_Ext_Conf = contentVal.getAsString(TransactionCols.Col_Ext_Conf).toInt()
        val Col_Ext_Ref = contentVal.getAsString(TransactionCols.Col_Ext_Ref).toInt()
        val Col_Ext_RefundDateTime = contentVal.getAsString(TransactionCols.Col_Ext_RefundDateTime)
        return Transaction(Col_UUID,Col_GUP_SN,Col_BatchNo,Col_ReceiptNo,Col_CardReadType,Col_PAN,
            Col_CardSequenceNumber, Col_TransCode, Col_Amount, Col_Amount2, Col_ExpDate, Col_Track2,
            Col_CustName, Col_IsVoid, Col_InstCnt, Col_InstAmount, Col_TranDate, Col_HostLogKey, Col_VoidDateTime,
            Col_AuthCode, Col_Aid, Col_AidLabel, Col_TextPrintCode1, Col_TextPrintCode2, Col_DisplayData,
            Col_KeySequenceNumber, Col_isPinByPass, Col_isOffline, Col_AC, Col_CID, Col_ATC, Col_TVR, Col_TSI,
            Col_AIP, Col_CVM, Col_AID2, Col_UN, Col_IAD, Col_SID, Col_Ext_Conf, Col_Ext_Ref, Col_Ext_RefundDateTime)
    }


}