package com.tokeninc.sardis.application_template.data.database.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tokeninc.sardis.application_template.data.database.DatabaseInfo

@Entity(tableName = DatabaseInfo.TRANSACTION_TABLE,
indices = [Index(value = [TransactionCols.Col_GUP_SN],unique = true)])
data class Transaction(
    @ColumnInfo(name = TransactionCols.Col_UUID)
    var Col_UUID: String?,
    @ColumnInfo (name = TransactionCols.col_ulSTN)
    var col_ulSTN: Int,
    @ColumnInfo(name = TransactionCols.Col_GUP_SN)
    @PrimaryKey
    var Col_GUP_SN: Int,
    @ColumnInfo(name = TransactionCols.Col_BatchNo)
    var Col_BatchNo: Int,
    @ColumnInfo(name = TransactionCols.Col_ReceiptNo)
    var Col_ReceiptNo: Int?,
    @ColumnInfo(name = TransactionCols.col_ZNO)
    var ZNO: String?,
    @ColumnInfo(name = TransactionCols.Col_CardReadType)
    var Col_CardReadType: Int,
    @ColumnInfo(name = TransactionCols.Col_PAN)
    var Col_PAN: String,
    @ColumnInfo(name = TransactionCols.Col_CardSequenceNumber)
    var Col_CardSequenceNumber: String?, //
    @ColumnInfo(name = TransactionCols.Col_TransCode)
    var Col_TransCode: Int = 0,
    @ColumnInfo(name = TransactionCols.Col_Amount)
    var Col_Amount: Int,
    @ColumnInfo(name = TransactionCols.Col_Amount2)
    var Col_Amount2: Int? = 0,
    @ColumnInfo(name = TransactionCols.Col_ExpDate)
    var Col_ExpDate: String?,
    @ColumnInfo(name = TransactionCols.Col_Track2)
    var Col_Track2: String?,
    @ColumnInfo(name = TransactionCols.Col_CustomerName)
    var Col_CustomerName: String?,
    @ColumnInfo(name = TransactionCols.Col_IsVoid)
    var Col_IsVoid: Int,
    @ColumnInfo(name = TransactionCols.Col_InstCnt)
    var Col_InstCnt: Int? = 0,
    @ColumnInfo(name = TransactionCols.Col_TranDate)
    var Col_TranDate: String?,
    @ColumnInfo(name = TransactionCols.Col_RefNo)
    var Col_RefNo: String,
    @ColumnInfo(name = TransactionCols.Col_VoidDateTime)
    var Col_VoidDateTime: String?,
    @ColumnInfo(name = TransactionCols.col_stChipData)
    var Col_ChipData: String?,
    @ColumnInfo(name = TransactionCols.col_isSignature)
    var Col_IsSignature: Int?,
    @ColumnInfo(name = TransactionCols.Col_AuthCode)
    var Col_AuthCode: String,
    @ColumnInfo(name = TransactionCols.Col_Aid)
    var Col_Aid: String?,
    @ColumnInfo(name = TransactionCols.Col_AidLabel)
    var Col_AidLabel: String?,
    @ColumnInfo(name = TransactionCols.Col_TextPrintCode)
    var Col_TextPrintCode: String,
    @ColumnInfo(name = TransactionCols.Col_DisplayData)
    var Col_DisplayData: String,
    @ColumnInfo(name = TransactionCols.Col_KeySequenceNumber)
    var Col_KeySequenceNumber: String,
    @ColumnInfo(name = TransactionCols.Col_isPinByPass)
    var Col_isPinByPass: Int,
    @ColumnInfo(name = TransactionCols.Col_isOffline)
    var Col_isOffline: Int,
    @ColumnInfo(name = TransactionCols.Col_is_onlinePIN)
    var isOnlinePIN: Int,
    @ColumnInfo(name = TransactionCols.Col_AC)
    var Col_AC: String?,
    @ColumnInfo(name = TransactionCols.Col_CID)
    var Col_CID: String?,
    @ColumnInfo(name = TransactionCols.Col_ATC)
    var Col_ATC: String?,
    @ColumnInfo(name = TransactionCols.Col_TVR)
    var Col_TVR: String?,
    @ColumnInfo(name = TransactionCols.Col_TSI)
    var Col_TSI: String?,
    @ColumnInfo(name = TransactionCols.Col_AIP)
    var Col_AIP: String?,
    @ColumnInfo(name = TransactionCols.Col_CVM)
    var Col_CVM: String?,
    @ColumnInfo(name = TransactionCols.Col_AID2)
    var Col_AID2: String?,
    @ColumnInfo(name = TransactionCols.Col_UN)
    var Col_UN: String?,
    @ColumnInfo(name = TransactionCols.Col_IAD)
    var Col_IAD: String?,
    @ColumnInfo(name = TransactionCols.Col_SID)
    var Col_SID: String?,
    @ColumnInfo(name = TransactionCols.Col_Ext_Conf)
    var Col_Ext_Conf: Int? = 0,
    @ColumnInfo(name = TransactionCols.Col_Ext_RefundDateTime)
    var Col_Ext_RefundDateTime: String? //TranDate2 in Java
)
