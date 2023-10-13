package com.tokeninc.sardis.application_template.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.tokeninc.sardis.application_template.data.database.AppTempDB
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.data.model.type.SlipType
import com.tokeninc.sardis.application_template.data.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import com.tokeninc.sardis.application_template.utils.objects.SampleReceipt
import com.tokeninc.sardis.application_template.utils.printHelpers.TransactionPrintHelper


class CheckSaleReceiver : BroadcastReceiver() {
    /**
     * This class for receive the UUID from successful transaction performed via
     * battery run out flow. It takes UUID from
     * @param intent and control the transaction
     * is successful or not. If it's successful, it creates intent again and send to PGW
     * for print it. When the sending intent, used sendBroadcast function for communicate
     * with PGW.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra("UUID")) {
            Log.i("Check Sale Receiver","Entered")
            Log.i("UUID", intent.extras!!.getString("UUID")!!)
            val uuid = intent.extras!!.getString("UUID")
            val db: AppTempDB = AppTempDB.getInstance(context)
            val activationRepository = ActivationRepository(db.activationDao)
            val transactionList = db.transactionDao.getTransactionsByUUID(uuid!!)
            val transaction =  if (transactionList.isNullOrEmpty()) null else transactionList[0]
            val resultIntent = Intent()
            val printHelper = TransactionPrintHelper()
            val bundle = Bundle()
            if (transaction != null) {
                bundle.putInt("ResponseCode", ResponseCode.SUCCESS.ordinal)
                bundle.putInt("PaymentStatus", 0)
                bundle.putInt("Amount", transaction.Col_Amount)
                val sampleReceipt = SampleReceipt(transaction,activationRepository)
                bundle.putString("customerSlipData",
                    printHelper.getFormattedText(sampleReceipt,
                        SlipType.CARDHOLDER_SLIP,ContentValHelper().getContentVal(transaction),
                        TransactionCode.SALE.type, context,transaction.ZNO,transaction.Col_ReceiptNo,false))
                bundle.putString(
                    "merchantSlipData",
                    printHelper.getFormattedText(sampleReceipt,
                        SlipType.MERCHANT_SLIP,ContentValHelper().getContentVal(transaction),
                        TransactionCode.SALE.type, context,transaction.ZNO,transaction.Col_ReceiptNo,false))
                bundle.putInt("BatchNo", transaction.Col_BatchNo)
                bundle.putInt("TxnNo", transaction.Col_GUP_SN)
                bundle.putInt("SlipType", SlipType.BOTH_SLIPS.value)
                bundle.putBoolean("IsSlip", true)
            } else { // When PGW reboot on sale screen without any sale attempt it ensures to not giving an error
                Log.i("Check Sale Receiver","Transaction is null")
            }
            resultIntent.action = "check_sale_result"
            resultIntent.putExtras(bundle)
            resultIntent.setPackage("com.tokeninc.sardis.paymentgateway")
            Log.d("intent_control", resultIntent.toString())
            context.sendBroadcast(resultIntent)
        }
    }
}
