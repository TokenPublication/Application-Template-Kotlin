package com.tokeninc.sardis.application_template.data.model.resultCode

/**
 * This is enum class for holding codes of response in dummy sale.
 */
enum class ResponseCode() {
    SUCCESS,
    ERROR,
    CANCELED,
    OFFLINE_DECLINE,
    UNABLE_DECLINE,
    ONLINE_DECLINE
}
