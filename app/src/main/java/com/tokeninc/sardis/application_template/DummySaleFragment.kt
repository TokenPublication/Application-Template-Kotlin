package com.tokeninc.sardis.application_template

import MenuItem
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.database.activation.ActivationDB
import com.tokeninc.sardis.application_template.database.batch.BatchDB
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.*
import com.tokeninc.sardis.application_template.helpers.StringHelper
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintService
import kotlinx.coroutines.*
import java.lang.String.valueOf
import java.util.*


class DummySaleFragment : Fragment() {

    private var _binding: FragmentDummySaleBinding? = null
    private val binding get() = _binding!!
    var activityContext: Context? = null //this is for getting context from activity class
    //private val notNullContext get() = _context!!
    private var resultIntent: Intent? = null
    var saleIntent: Intent? = null
    private var bundle: Bundle? = null
    var saleBundle: Bundle? = null
    var activationDB: ActivationDB? = null
    var transactionDB: TransactionDB? = null
    var batchDB: BatchDB? = null
    var mainActivity: MainActivity? = null
    var transactionService: TransactionService? = null

    private var menuItemList = mutableListOf<IListMenuItem>()
    private var card: ICCCard? = null
    var amount = 0

    companion object{
        var cardOwner =""
        var cardNumber = "**** ****"
        //listener for spinner
        private val listener: AdapterView.OnItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    (parent.getChildAt(0) as TextView).setTextColor(Color.WHITE)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDummySaleBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvAmount.text = StringHelper().getAmount(amount)
        prepareSpinner()
        clickButtons()
    }

    private fun prepareSpinner(){
        val spinner = binding.spinner
        val items = mutableListOf<String>(
            valueOf(PaymentTypes.CREDITCARD),
            valueOf(PaymentTypes.TRQRCREDITCARD),
            valueOf(PaymentTypes.TRQRFAST),
            valueOf(PaymentTypes.TRQRMOBILE),
            valueOf(PaymentTypes.TRQROTHER),
            valueOf(PaymentTypes.OTHER)
        )
        //because there were an error with context, we get context from mainActivity
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(activityContext!!, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
    }


    private fun clickButtons(){
        binding.btnSale.setOnClickListener {
            mainActivity!!.isSale = true
            mainActivity!!.readCard()
        }
        binding.btnSuccess.setOnClickListener {
            prepareDummyResponse(ResponseCode.SUCCESS)
        }
        binding.btnError.setOnClickListener {
            prepareDummyResponse(ResponseCode.ERROR)
        }
        binding.btnCancel.setOnClickListener {
            prepareDummyResponse(ResponseCode.CANCELED)
        }
        binding.btnofflineDecline.setOnClickListener {
            prepareDummyResponse(ResponseCode.OFFLINE_DECLINE)
        }
        binding.btnonlineDecline.setOnClickListener {
            prepareDummyResponse(ResponseCode.ONLINE_DECLINE)
        }
        binding.btnunableDecline.setOnClickListener {
            prepareDummyResponse(ResponseCode.UNABLE_DECLINE)
        }
    }


    private fun doSale() {
        transactionService!!.mainActivity = mainActivity
        CoroutineScope(Dispatchers.Default).launch {
            val transactionResponse = transactionService!!.doInBackground(activityContext!!,
                amount, card!!,TransactionCode.SALE,
                ContentValues(), null,false,null ,false)
            finishSale(transactionResponse!!)
        }
    }

    private fun finishSale(transactionResponse: TransactionResponse){
        Log.d("Transaction/Response","${transactionResponse.contentVal}")

        val responseCode = transactionResponse.responseCode
        if (responseCode == ResponseCode.SUCCESS){
            getNotNullBundle().putInt("ResponseCode", responseCode.ordinal)
            getNotNullBundle().putInt("PaymentStatus", 0) // #2 Payment Status
            getNotNullBundle().putInt("Amount", amount ) // #3 Amount
            getNotNullBundle().putInt("Amount2", amount)
            getNotNullBundle().putBoolean("IsSlip", true)

            getNotNullBundle().putInt("BatchNo", 1) // TODO Do it after implementing Batch
            getNotNullBundle().putString("CardNo", StringHelper().maskCardNumber(card!!.mCardNumber!!))
            getNotNullBundle().putString("MID", activationDB!!.getMerchantId());
            getNotNullBundle().putString("TID", activationDB!!.getTerminalId());
            getNotNullBundle().putInt("TxnNo",5)  // TODO Do it after implementing Batch
            getNotNullBundle().putInt("PaymentType", PaymentTypes.CREDITCARD.type) //TODO check it

            var slipType: SlipType = SlipType.NO_SLIP
            if (responseCode == ResponseCode.CANCELED || responseCode == ResponseCode.UNABLE_DECLINE || responseCode == ResponseCode.OFFLINE_DECLINE) {
                slipType = SlipType.NO_SLIP
            }
            else{
                if (transactionResponse.responseCode == ResponseCode.SUCCESS){
                    val printHelper = PrintService()
                    getNotNullBundle().putString("customerSlipData", printHelper.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, null, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, activityContext!!,1, 1,false))
                    getNotNullBundle().putString("merchantSlipData", printHelper.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, null, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, activityContext!!,1, 1,false))
                    //getNotNullBundle().putString("RefundInfo", getRefundInfo(response)); //TODO sonra bakılacak
                    if(transactionResponse.contentVal != null) {
                        getNotNullBundle().putString("RefNo", transactionResponse.contentVal!!.getAsString(TransactionCol.Col_HostLogKey.name))
                        getNotNullBundle().putString("AuthNo", transactionResponse.contentVal!!.getAsString(TransactionCol.Col_AuthCode.name))
                    }
                }
            }
            getNotNullBundle().putInt("SlipType", slipType.value) //TODO fail receipt yap
            getNotNullIntent().putExtras(getNotNullBundle())
            mainActivity!!.dummySetResult(getNotNullIntent())
        }
    }

    private fun getRefundInfo(transactionResponse: TransactionResponse): String {
        val json = JSONObject()
        val transaction: ContentValues? = transactionResponse.contentVal
        try {
            json.put("BatchNo", 1) // TODO Do it after implementing Batch
            json.put("TxnNo", 100) // TODO Do it after implementing Batch
            json.put("Amount", amount)
            json.put("RefNo", transaction?.getAsString(TransactionCol.Col_HostLogKey.name))
            json.put("AuthCode", transaction?.getAsString(TransactionCol.Col_AuthCode.name))
            json.put("TranDate", transaction?.getAsString(TransactionCol.Col_TranDate.name))
            if (transaction?.getAsInteger(TransactionCol.Col_InstCnt.name) != null && transaction.getAsInteger(TransactionCol.Col_InstCnt.name) > 0) {
                val installment = JSONObject()
                installment.put("InstCount", transaction.getAsInteger(TransactionCol.Col_InstCnt.name))
                installment.put("InstAmount", transaction.getAsInteger(TransactionCol.Col_InstAmount.name))
                json.put("Installment", installment)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    private fun prepareDummyResponse (code: ResponseCode){

        var paymentType = PaymentTypes.CREDITCARD.type
        val cbMerchant = binding.cbMerchant
        val cbCustomer = binding.cbCustomer

        //this is for slip type //TODO buraya bakmıyor olabilir
        var slipType = SlipType.NO_SLIP
        if (cbMerchant.isChecked && cbCustomer.isChecked)
            slipType = SlipType.BOTH_SLIPS
        else if (cbMerchant.isChecked)
            slipType = SlipType.MERCHANT_SLIP
        else if (cbCustomer.isChecked)
            slipType = SlipType.CARDHOLDER_SLIP

        //if code is success then it gets selected item from spinner and modifies payment type
        //with respect to its type
        val spinner = binding.spinner
        if (code == ResponseCode.SUCCESS) {
            val text: String = spinner.getSelectedItem().toString()
            if (text == valueOf(PaymentTypes.TRQRCREDITCARD))
                paymentType = PaymentTypes.TRQRCREDITCARD.type
            else if (text == valueOf(PaymentTypes.TRQRFAST))
                paymentType = PaymentTypes.TRQRFAST.type
            else if (text == valueOf(PaymentTypes.TRQRMOBILE))
                paymentType = PaymentTypes.TRQRMOBILE.type
            else if (text == valueOf(PaymentTypes.TRQROTHER))
                paymentType = PaymentTypes.TRQROTHER.type
            else if (text == valueOf(PaymentTypes.OTHER))
                paymentType = PaymentTypes.OTHER.type
        }

        onSaleResponseRetrieved(amount, code, true, slipType, "1234 **** **** 7890",
            "OWNER NAME", paymentType)
    }

    /**
     * This methods for preventing some errors
     * Because we operate in fragment, its bundle and intents aren't be same as activities
     * Therefore we get those objects from activity.
     */
    fun getNewBundle(mBundle: Bundle){
        bundle = mBundle
    }
    private fun getNotNullBundle(): Bundle{
        return bundle!!
    }

    fun getNewIntent(mIntent: Intent){
        resultIntent = mIntent
    }
    private fun getNotNullIntent(): Intent{
        return resultIntent!!
    }
    //TODO Data has to be returned to Payment Gateway after sale operation completed via template
    // below using actual data.
    private fun onSaleResponseRetrieved(price: Int, code: ResponseCode, hasSlip: Boolean, slipType: SlipType, cardNo: String, ownerName: String, paymentType: Int){
        getNotNullBundle().putInt("ResponseCode", code.ordinal)
        getNotNullBundle().putString("CardOwner", cardOwner) // Optional
        getNotNullBundle().putString("CardNumber", cardNumber) // Optional, Card No can be masked
        getNotNullBundle().putInt("PaymentStatus", 0) // #2 Payment Status
        getNotNullBundle().putInt("Amount", price) // #3 Amount
        getNotNullBundle().putInt("Amount2", price)
        getNotNullBundle().putBoolean("IsSlip", hasSlip)

        //bundle.putInt("BatchNo", databaseHelper.getBatchNo())

        getNotNullBundle().putString("CardNo", StringHelper().maskCardNumber(cardNumber))

        //bundle.putString("MID", databaseHelper.getMerchantId()); //#6 Merchant ID
        //bundle.putString("TID", databaseHelper.getTerminalId()); //#7 Terminal ID
        //bundle.putInt("TxnNo", databaseHelper.getTxNo());
        getNotNullBundle().putInt("SlipType", slipType.value)

        //bundle.putString("RefundInfo", getRefundInfo(ResponseCode.SUCCESS))
        //bundle.putString("RefNo", String.valueOf(databaseHelper.getSaleID()))
        getNotNullBundle().putInt("PaymentType", paymentType)

        /*
        if (slipType == SlipType.CARDHOLDER_SLIP || slipType == SlipType.BOTH_SLIPS) {
            bundle.putString("customerSlipData", SalePrintHelper.getFormattedText(getSampleReceipt(cardNo, ownerName), SlipType.CARDHOLDER_SLIP, this, 1, 2));
            //  bundle.putByteArray("customerSlipBitmapData",PrintHelper.getBitmap(getApplicationContext()));
        }
        if (slipType == SlipType.MERCHANT_SLIP || slipType == SlipType.BOTH_SLIPS) {
            bundle.putString("merchantSlipData", SalePrintHelper.getFormattedText(getSampleReceipt(cardNo, ownerName), SlipType.MERCHANT_SLIP, this, 1, 2));
            //  bundle.putByteArray("merchantSlipBitmapData",PrintHelper.getBitmap(getApplicationContext()));
        }
         */
        //bundle.putString("ApprovalCode", getApprovalCode())
        getNotNullIntent().putExtras(getNotNullBundle())
        mainActivity!!.dummySetResult(getNotNullIntent())


    }



    private fun getApprovalCode(): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activityContext!!)
        var approvalCode = sharedPref.getInt("ApprovalCode", 0)
        sharedPref.edit().putInt("ApprovalCode", ++approvalCode).apply()
        return String.format(Locale.ENGLISH, "%06d", approvalCode)
    }


    /**
     * this is for avoiding memory leaks
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun getStrings(resID: Int): String{
        return mainActivity!!.getString(resID)
    }

    fun prepareSaleMenu(mCard: ICCCard?) {
        card = mCard
        mainActivity!!.isSale = false
        menuItemList.add(MenuItem( getStrings(R.string.sale), {
            doSale()
        }))
        menuItemList.add(MenuItem(getStrings(R.string.Installment_sale), { }))
        menuItemList.add(MenuItem(getStrings(R.string.loyalty_sale), { }))
        menuItemList.add(MenuItem(getStrings(R.string.campaign_sale), { }))
        val fragment = ListMenuFragment.newInstance(menuItemList,
            getStrings(R.string.sale_type), false, null)
        mainActivity!!.replaceFragment(fragment)
    }



}