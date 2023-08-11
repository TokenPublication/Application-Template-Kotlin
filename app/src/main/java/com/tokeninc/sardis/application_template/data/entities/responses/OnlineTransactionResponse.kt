package com.tokeninc.sardis.application_template.data.entities.responses

import com.tokeninc.sardis.application_template.enums.ResponseCode

/**
 * This is a class for holding some data in a regular way.
 */
class OnlineTransactionResponse {
    var mResponseCode: ResponseCode? = null
    var mTextPrintCode: String? = null
    var mAuthCode: String? = null
    var mRefNo: String? = null
    var mDisplayData: String? = null
    var mKeySequenceNumber: String? = null
    var dateTime: String? = null
}
