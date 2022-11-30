package com.tokeninc.sardis.application_template.enums

enum class CardReadType(val type: Int) {
    NONE(0),
    ICC(1),
    MSR(2),
    ICC2MSR(3),
    KeyIn(4),
    CLCard(5),
    QrPay(6)
}