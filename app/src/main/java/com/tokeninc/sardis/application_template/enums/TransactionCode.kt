package com.tokeninc.sardis.application_template.enums

enum class TransactionCode(val type: Int) {
    SALE(1),
    INSTALLMENT_SALE(2),
    VOID(3);
}