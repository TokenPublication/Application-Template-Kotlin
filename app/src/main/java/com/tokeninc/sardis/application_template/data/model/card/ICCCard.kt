package com.tokeninc.sardis.application_template.data.model.card

import com.tokeninc.sardis.application_template.data.model.type.CardReadType
import com.tokeninc.sardis.application_template.utils.StringHelper

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
    var OnlPINReq: Int? = null

    fun isPinByPass(): Boolean {
        if (mCardReadType == CardReadType.ICC.type && TVR != null) {
            val flag = StringHelper().hexStringToAscii(TVR!!)[2].code.toByte()
            return flag.toInt() and 0x08 == 0x08.toByte()
                .toInt() || flag.toInt() and 0x10 == 0x10.toByte()
                .toInt() || flag.toInt() and 0x20 == 0x20.toByte().toInt()
        }
        return false
    }

}
