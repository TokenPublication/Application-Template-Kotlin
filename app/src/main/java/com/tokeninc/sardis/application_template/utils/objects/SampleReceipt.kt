package com.tokeninc.sardis.application_template.utils.objects

import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.data.repositories.BatchRepository
import com.tokeninc.sardis.application_template.utils.StringHelper

/**
 * This class is for holding some values in a more structured way.
 * This class regulate some variables while printing slip instead of too much parameter passing.
 */
class SampleReceipt(
    var transaction: Transaction,
    activationRepository: ActivationRepository,
) {
    var merchantName = "Token Financial Technologies"
    var merchantID = activationRepository.merchantID()
    var terminalID = activationRepository.terminalID()
    var aid = transaction.Col_Aid
    var aidLabel = transaction.Col_AidLabel
    var cardNo = StringHelper().maskCardNumber(transaction.Col_PAN)
    var amount = StringHelper().getAmount(transaction.Col_Amount)
    var authCode = transaction.Col_AuthCode
    var refNo = transaction.Col_RefNo
    var batchNo = transaction.Col_BatchNo.toString()
    var transactionCode = transaction.Col_TransCode
    var cardReadType = transaction.Col_CardReadType
    var groupSerialNo =  transaction.Col_GUP_SN.toString()
}
