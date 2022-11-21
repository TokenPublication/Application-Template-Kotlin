package com.tokeninc.sardis.application_template.enums

enum class PaymentTypes(val type: Int) {
    CREDITCARD(3),
    TRQRCREDITCARD(23),
    TRQRFAST(24),
    TRQRMOBILE(25),
    TRQROTHER(26),
    OTHER(27)
}