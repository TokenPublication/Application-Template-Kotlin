package com.tokeninc.sardis.application_template.utils.objects

import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.data.repositories.BatchRepository
import com.tokeninc.sardis.application_template.utils.StringHelper
import kotlin.properties.Delegates

/**
 * This class is for holding some values in a more structured way.
 * This class regulate some variables while printing slip instead of too much parameter passing.
 */
class SampleReceipt(
    var transaction: Transaction,
    activationRepository: ActivationRepository,
    batchRepository: BatchRepository
) {
    var merchantName = "Token Financial Technologies"
    var merchantID = activationRepository.merchantID()
    var terminalID = activationRepository.terminalID()
    var aid = transaction.Col_Aid
    var aidLabel = transaction.Col_AidLabel
    var cardNo = StringHelper().maskCardNumber(transaction.Col_PAN)
    var amount = StringHelper().getAmount(transaction.Col_Amount)
    var authCode = transaction.Col_AuthCode
    var refNo = transaction.Col_HostLogKey
    var batchNo = batchRepository.getBatchNo().toString()
    var transactionCode = transaction.Col_TransCode
    var cardReadType = transaction.Col_CardReadType
    var groupSerialNo =  batchRepository.getGroupSN().toString()
    var approvalCode = StringHelper().generateApprovalCode(batchNo, groupSerialNo, (groupSerialNo.toInt() - 1).toString())!!


}
