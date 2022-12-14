package com.tokeninc.sardis.application_template

import com.tokeninc.sardis.application_template.enums.ResponseCode

class OnlineTransactionResponse {
    var mResponseCode: ResponseCode? = null
    var mTextPrintCode1: String? = null
    var mTextPrintCode2: String? = null
    var mAuthCode: String? = null
    var mHostLogKey: String? = null
    var mDisplayData: String? = null
    var mKeySequenceNumber: String? = null
    var insCount: String? = null
    var instAmount: Int? = null
}
