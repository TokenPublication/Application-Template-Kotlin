package com.tokeninc.sardis.application_template.data.model.type

/**
 * This is enum class for holding types of slip.
 */
enum class SlipType(val value: Int) {
    NO_SLIP(0),
    MERCHANT_SLIP(1),
    CARDHOLDER_SLIP(2),
    BOTH_SLIPS(3)
}
