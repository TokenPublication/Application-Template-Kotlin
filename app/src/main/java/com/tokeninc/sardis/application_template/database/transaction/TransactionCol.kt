package com.tokeninc.sardis.application_template.database.transaction

/**
 * This is an enum class for holding names of Transaction Table's columns
 */
enum class TransactionCol {
    Col_UUID,
    Col_GUP_SN,
    Col_BatchNo,
    Col_ReceiptNo,
    Col_CardReadType,
    Col_PAN,
    Col_CardSequenceNumber,
    Col_TransCode,
    Col_Amount,
    Col_Amount2,
    Col_ExpDate,
    Col_Track2,
    Col_CustName,
    Col_IsVoid,
    Col_InstCnt,
    Col_InstAmount,
    Col_TranDate,
    Col_HostLogKey,
    Col_VoidDateTime,
    Col_AuthCode,
    Col_Aid,
    Col_AidLabel,
    Col_TextPrintCode1,
    Col_TextPrintCode2,
    Col_DisplayData,
    Col_KeySequenceNumber,
    Col_isPinByPass,
    Col_isOffline,
    Col_AC,
    Col_CID,
    Col_ATC,
    Col_TVR,
    Col_TSI,
    Col_AIP,
    Col_CVM,
    Col_AID2,
    Col_UN,
    Col_IAD,
    Col_SID,
    Col_Ext_Conf,
    Col_Ext_Ref,
    Col_Ext_RefundDateTime;
}
