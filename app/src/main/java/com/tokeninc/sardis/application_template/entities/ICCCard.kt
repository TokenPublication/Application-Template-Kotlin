package com.tokeninc.sardis.application_template.entities

import com.tokeninc.sardis.application_template.enums.CardServiceResult

class ICCCard {
    var resultCode = CardServiceResult.USER_CANCELLED.resultCode()
    var mCardReadType = 0
    var mCardNumber: String? = null
    var mTrack2Data: String? = null
    var mExpireDate: String? = null
    var mTranAmount1 = 0
    var ownerName: String? = null
    var CardSeqNum: String? = null
    var AC: String? = null
    var CID: String? = null
    var ATC: String? = null
    var TVR: String? = null
    var TSI: String? = null
    var AIP: String? = null
    var CVM: String? = null
    var AID2: String? = null
    var UN: String? = null
    var IAD: String? = null
    var AIDLabel: String? = null
}