package com.tokeninc.sardis.application_template.data.model.card

/**
 * This is enum class for holding results of card services.
 */
enum class CardServiceResult(private val value: Int) {
    SUCCESS(0),
    USER_CANCELLED(1),
    ERROR(2),
    ERROR_MSR_TRACK_IS_EMPTY(3),
    ERROR_JSON_PARSE(4),
    ERROR_UNSUPPORTED_ENCODING(5),
    ERROR_TIMEOUT(6),
    ERROR_FALLBACK_AUTH(7);

    fun resultCode(): Int {
        return value
    }
}
