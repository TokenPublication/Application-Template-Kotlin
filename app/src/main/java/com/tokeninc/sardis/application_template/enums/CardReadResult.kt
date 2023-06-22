package com.tokeninc.sardis.application_template.enums

enum class CardReadResult(private val value: Int) {
    SALE_NOT_GIB_CL(0),
    SALE_NOT_GIB_ICC(1),
    VOID_GIB(2),
    VOID_NOT_GIB(3),
    SALE_GIB(4),
    REFUND_GIB(5),
    REFUND_NOT_GIB(6),
    QR_PAY(7)
}


