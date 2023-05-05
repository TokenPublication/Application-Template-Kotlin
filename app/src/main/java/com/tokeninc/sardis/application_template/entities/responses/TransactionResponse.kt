package com.tokeninc.sardis.application_template.entities.responses

import android.content.ContentValues
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.entities.responses.OnlineTransactionResponse

/**
 * This class is for transport some data to printing slip after transaction ends.
 */
class TransactionResponse(var responseCode: ResponseCode , var onlineTransactionResponse: OnlineTransactionResponse
                          ,var contentVal: ContentValues?, var extraContent: ContentValues? , var transactionCode: Int)