package com.tokeninc.sardis.application_template.data.model.card

import android.content.Context
import android.util.Log
import com.tokeninc.sardis.application_template.data.model.type.CardReadType
import com.tokeninc.sardis.application_template.utils.StringHelper
import java.lang.Exception

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

    fun isOnlinePin(): Boolean {
        try {
            val serviceCode: CharArray = mTrack2Data!!.toCharArray()
            /** Chine Union Pay
            val flag: CCFlag = if (SiteParameterDB.getInstance(context).getAOFlags21()
            .is8DigitBinAllowed()
            ) Bin8DigitParameterDB.getInstance(context)
            .get8DigitCCFlag(getmCardNumber()) else BinParameterDB.getInstance(context)
            .getCCFlag(getmCardNumber())
            uniton pay
            if (isInternational) {
            // Online pin for China Union Pay Card, if MSR-PIN-Req bit is set
            val RangeccFlag: RangeCCFlag =
            RangeDB.getInstance(context).getRangeCCFlag(getmCardNumber())
            if (RangeccFlag.isCUPOnlinePinReqMSR()) {
            return true
            }
            }
             */
            if (serviceCode[0] == '0' && mCardNumber!!.startsWith("420343")) {
                // No pin for Flextra Card
                return false
            }
            Log.i("serviceCode", serviceCode[2].toString())
            // TODO add flag.isPinCodeRequired()  for china union pay
            return  serviceCode[2] == '0' || serviceCode[2] == '3' || serviceCode[2] == '5' || serviceCode[2] == '6' || serviceCode[2] == '7'
        } catch (e: Exception){
            e.printStackTrace()
            Log.i("isOnlinePin", "exception: $e")
            return false
        }
    }
}
