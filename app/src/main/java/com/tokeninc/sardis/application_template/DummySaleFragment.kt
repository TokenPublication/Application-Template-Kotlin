package com.tokeninc.sardis.application_template

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.enums.PaymentTypes
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.helpers.StringHelper
import java.lang.String.valueOf
import java.util.*


class DummySaleFragment : Fragment() {

    private var _binding: FragmentDummySaleBinding? = null
    private val binding get() = _binding!!
    //this is for getting context from activity classes
    private var _context: Context? = null
    private val notNullContext get() = _context!!
    var resultIntent: Intent? = null
    var bundle: Bundle? = null

    companion object{
        var amount = 0
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
    ): View? {
        _binding = FragmentDummySaleBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvAmount.text = StringHelper.getAmount(amount)
        prepareSpinner()
        clickButtons()
    }

    /**
     * this is for getting amount from MainActivity by benefitting intents
     */
    fun setAmount(mAmount: Int){
        amount = mAmount
    }

    /**
     * this is for getting MainActivity's context to prevent some errors
     */
    fun set_Context(mContext: Context?){
        _context = mContext
    }

    private fun prepareSpinner(){
        val spinner = binding.spinner
        val items = mutableListOf<String>(
            java.lang.String.valueOf(PaymentTypes.CREDITCARD),
            valueOf(PaymentTypes.TRQRCREDITCARD),
            valueOf(PaymentTypes.TRQRFAST),
            valueOf(PaymentTypes.TRQRMOBILE),
            valueOf(PaymentTypes.TRQROTHER),
            valueOf(PaymentTypes.OTHER)
        )
        //because there were an error with context, we get context from mainActivity
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(notNullContext, R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
    }


    fun clickButtons(){
        binding.btnSuccess.setOnClickListener {
            prepareDummyResponse(ResponseCode.SUCCESS)
        }
        binding.btnError.setOnClickListener {
            prepareDummyResponse(ResponseCode.ERROR)
        }
        binding.btnCancel.setOnClickListener {
            prepareDummyResponse(ResponseCode.CANCELLED)
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

    /*
    @SuppressLint("NonConstantResourceId")
    fun onClick(view: View) {
        when (view.id) {
            //R.id.btnSale -> doSale()
            binding.btnSuccess.id -> prepareDummyResponse(ResponseCode.SUCCESS)
            binding.btnError.id -> prepareDummyResponse(ResponseCode.ERROR)
            binding.btnCancel.id -> prepareDummyResponse(ResponseCode.CANCELLED)
            binding.btnofflineDecline.id -> prepareDummyResponse(ResponseCode.OFFLINE_DECLINE)
            binding.btnunableDecline.id -> prepareDummyResponse(ResponseCode.UNABLE_DECLINE)
            binding.btnonlineDecline.id -> prepareDummyResponse(ResponseCode.ONLINE_DECLINE)
        }
    }
     */

    public fun prepareDummyResponse (code: ResponseCode){

        var paymentType = PaymentTypes.CREDITCARD.type
        val cbMerchant = binding.cbMerchant
        val cbCustomer = binding.cbCustomer

        //this is for slip type
        var slipType = SlipType.NO_SLIP
        if (cbMerchant.isChecked && cbCustomer.isChecked) slipType =
            SlipType.BOTH_SLIPS
        else if (cbMerchant.isChecked)
            slipType = SlipType.MERCHANT_SLIP
        else if (cbCustomer.isChecked)
            slipType = SlipType.CARDHOLDER_SLIP

        //if code is success then it gets selected item from spinner and modifies payment type
        //with respect to its type
        val spinner = binding.spinner
        if (code === ResponseCode.SUCCESS) {
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
    public fun getNewBundle(mBundle: Bundle){
        bundle = mBundle
    }
    private fun getNotNullBundle(): Bundle{
        return bundle!!
    }

    public fun getNewIntent(mIntent: Intent){
        resultIntent = mIntent
    }
    private fun getNotNullIntent(): Intent{
        return resultIntent!!
    }
    //TODO Data has to be returned to Payment Gateway after sale operation completed via template
    // below using actual data.
    public fun onSaleResponseRetrieved(price: Int, code: ResponseCode, hasSlip: Boolean,
                                       slipType: SlipType, cardNo: String, ownerName: String, paymentType: Int){

        //doğru olmuş mu sor burasını
        getNotNullBundle().putInt("ResponseCode", code.ordinal)
        getNotNullBundle().putString("CardOwner", cardOwner) // Optional
        getNotNullBundle().putString("CardNumber", cardNumber) // Optional, Card No can be masked
        getNotNullBundle().putInt("PaymentStatus", 0) // #2 Payment Status
        getNotNullBundle().putInt("Amount", price) // #3 Amount
        getNotNullBundle().putInt("Amount2", price)
        getNotNullBundle().putBoolean("IsSlip", hasSlip)

        //db helper yok o yüzden yapmadım
        //bundle.putInt("BatchNo", databaseHelper.getBatchNo())

        getNotNullBundle().putString("CardNo", StringHelper.MaskTheCardNo(cardNumber))

        //bundle.putString("MID", databaseHelper.getMerchantId()); //#6 Merchant ID
        //bundle.putString("TID", databaseHelper.getTerminalId()); //#7 Terminal ID
        //bundle.putInt("TxnNo", databaseHelper.getTxNo());
        getNotNullBundle().putInt("SlipType", slipType.value)

        //bundle.putString("RefundInfo", getRefundInfo(ResponseCode.SUCCESS))
        //bundle.putString("RefNo", String.valueOf(databaseHelper.getSaleID()))
        getNotNullBundle().putInt("PaymentType", paymentType)

        /*  PrintHelper almadım
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
        MainActivity().dummySetResult(getNotNullIntent())


    }

    /* dbhelper olmadan olmaz
    private fun getRefundInfo(response: ResponseCode): String? {
        val json = JSONObject()
        try {
            json.put("BatchNo", databaseHelper.getBatchNo())
            json.put("TxnNo", databaseHelper.getTxNo())
            json.put("Amount", amount)
            json.put("RefNo", valueOf(databaseHelper.getSaleID()))
            json.put("MID", databaseHelper.getMerchantId())
            json.put("TID", databaseHelper.getTerminalId())
            json.put("CardNo", StringHelper.MaskTheCardNo(cardNumber))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

     */
    //deprecated bulamadım alternatifini şu anlık
    private fun getApprovalCode(): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(notNullContext)
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

}