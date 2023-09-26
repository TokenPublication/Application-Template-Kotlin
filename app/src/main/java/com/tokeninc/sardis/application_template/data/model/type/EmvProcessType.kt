package com.tokeninc.sardis.application_template.data.model.type

enum class EmvProcessType(ordinal: Int) {
    PARTIAL_EMV(0),  //After reading card data with requested Tags, requesting AAC at the 1st Generate AC to complete transaction and return requested Tag data
    READ_CARD(1),  //After app selection, read requested card data and return
    CONTINUE_EMV(2),  //Continue and complete full EMV Txn from Read Card Data process and returns  requested Tag data.
    FULL_EMV(3) //Actions based EMV Specification, such as Initiate App. Processing, Read App. Data, Off. Auth., Processing restrictions, Cardholder Verification, Term. Risk Man., First Gen AC and returns  requested Tag data.
}
