package com.tokeninc.sardis.application_template

import android.content.ContentValues
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode

class TransactionResponse(val contentVal: ContentValues?,
val transactionCode: TransactionCode, val message: String? = null) {

}