package com.tokeninc.sardis.application_template

import android.R
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.token.printerlib.IPrinterService
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.enums.PaymentTypes
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.helpers.StringHelper
import org.json.JSONObject
import java.lang.String.valueOf
import java.util.*


class DummySaleFragment : Fragment(), CardServiceListener {

    private var _binding: FragmentDummySaleBinding? = null
    private val binding get() = _binding!!
    private var _context: Context? = null //this is for getting context from activity class
    private val notNullContext get() = _context!!
    var resultIntent: Intent? = null
    var bundle: Bundle? = null
    val mainActivity = MainActivity()
    private var cardServiceBinding: CardServiceBinding? = null
    private var boolReadCard = false

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
        binding.tvAmount.text = StringHelper().getAmount(amount)
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
    fun setContext(mContext: Context?){
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


    private fun clickButtons(){
        binding.btnSale.setOnClickListener {
            readCard()
        }
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

    /**
     * define cardServiceBinding and make boolReadCard true because we read it.
     */
    private fun readCard(){
        cardServiceBinding = CardServiceBinding(notNullContext as AppCompatActivity?,this )
        boolReadCard = true
    }

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
        getNotNullBundle().putInt("ResponseCode", code.ordinal)
        getNotNullBundle().putString("CardOwner", cardOwner) // Optional
        getNotNullBundle().putString("CardNumber", cardNumber) // Optional, Card No can be masked
        getNotNullBundle().putInt("PaymentStatus", 0) // #2 Payment Status
        getNotNullBundle().putInt("Amount", price) // #3 Amount
        getNotNullBundle().putInt("Amount2", price)
        getNotNullBundle().putBoolean("IsSlip", hasSlip)

        //bundle.putInt("BatchNo", databaseHelper.getBatchNo())

        getNotNullBundle().putString("CardNo", StringHelper().MaskTheCardNo(cardNumber))

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
        mainActivity.dummySetResult(getNotNullIntent())


    }

    var mPrinterService: IPrinterService? = null

    /*
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

    override fun onCardServiceConnected() {
        if (boolReadCard){
            try {
                val obj = JSONObject()
                obj.put("forceOnline", 1)
                obj.put("zeroAmount", 0)
                obj.put("fallback", 1)
                obj.put("cardReadTypes", 6)
                obj.put("qrPay", 1)
                /*
                if (cardReadType == CardReadType.ICC.value) {
                    obj.put("showCardScreen", 0)
                }
                 */

                Log.w("CardServiceBind/Dummy","$cardServiceBinding")
                cardServiceBinding!!.getCard(amount, 40, obj.toString())
                boolReadCard = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCardDataReceived(cardData: String?) {
        Log.w("DummySale","Card Data Received")
    }

    override fun onPinReceived(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onICCTakeOut() {
        TODO("Not yet implemented")
    }

}