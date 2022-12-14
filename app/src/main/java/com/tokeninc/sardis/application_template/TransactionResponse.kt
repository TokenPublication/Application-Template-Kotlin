package com.tokeninc.sardis.application_template

import android.content.ContentValues
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode

class TransactionResponse(var responseCode: ResponseCode , var onlineTransactionResponse: OnlineTransactionResponse
                          ,var contentVal: ContentValues?, var transactionCode: TransactionCode ) {

}