package com.tokeninc.sardis.application_template.responses

import android.content.ContentValues
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.responses.OnlineTransactionResponse

class TransactionResponse(var responseCode: ResponseCode , var onlineTransactionResponse: OnlineTransactionResponse
                          ,var contentVal: ContentValues?, var extraContent: ContentValues? , var transactionCode: Int)