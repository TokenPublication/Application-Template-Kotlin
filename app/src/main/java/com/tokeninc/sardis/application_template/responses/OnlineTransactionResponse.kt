package com.tokeninc.sardis.application_template.responses

import com.tokeninc.sardis.application_template.enums.ResponseCode


/**
 * This is a class for holding some data in a regular way.
 */
class OnlineTransactionResponse {
    var mResponseCode: ResponseCode? = null
    var mTextPrintCode1: String? = null
    var mTextPrintCode2: String? = null
    var mAuthCode: String? = null
    var mHostLogKey: String? = null
    var mDisplayData: String? = null
    var mKeySequenceNumber: String? = null
    var insCount: Int? = null
    var instAmount: Int? = null
    var dateTime: String? = null
}
