package com.tokeninc.sardis.application_template.data.model.card

/**
 * This is a class for keeping ICC card data.
 */
class ICCCard: ICard {
    override var resultCode = CardServiceResult.USER_CANCELLED.resultCode()
    override var mCardReadType = 0
    override var mCardNumber: String? = null
    var mTrack2Data: String? = null
    var mExpireDate: String? = null
    var mTranAmount1 = 0
    var ownerName: String? = null
    var CardSeqNum: String? = null
    var dateTime: String? = null
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
    var SID: String? = null
}
