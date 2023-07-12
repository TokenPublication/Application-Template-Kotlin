package com.tokeninc.sardis.application_template.data.entities.card_entities

import com.tokeninc.sardis.application_template.enums.CardServiceResult

class MSRCard: ICard {
    override var resultCode: Int = CardServiceResult.USER_CANCELLED.resultCode()
    override var mCardReadType = 0
    override var mCardNumber: String? = null
    var mTrack2Data: String? = null
    var mExpireDate: String? = null
    var mTranAmount1 = 0
}
