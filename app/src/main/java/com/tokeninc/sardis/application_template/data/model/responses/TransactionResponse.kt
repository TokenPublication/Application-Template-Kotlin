package com.tokeninc.sardis.application_template.data.model.responses

import android.content.ContentValues
import android.os.Bundle
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode

/**
 * This class is for transport some data to printing slip after transaction ends.
 */
class TransactionResponse(var responseCode: ResponseCode, var onlineTransactionResponse: OnlineTransactionResponse
                          , var contentVal: ContentValues?, var bundle: Bundle, var transactionCode: Int)
