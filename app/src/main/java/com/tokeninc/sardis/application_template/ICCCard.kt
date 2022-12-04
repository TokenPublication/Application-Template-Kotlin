package com.tokeninc.sardis.application_template

class ICCCard  {
    var resultCode = 0
    var mCardReadType = 0
    var cardNumber: String? = null
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
    //yıldızda olmayanlar null geç burada date time
}