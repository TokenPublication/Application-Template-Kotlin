package com.tokeninc.sardis.application_template

import MenuItem
import android.R
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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.database.activation.ActivationDB
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.*
import com.tokeninc.sardis.application_template.helpers.StringHelper
import com.tokeninc.sardis.application_template.helpers.printHelpers.SalePrintHelper
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.String.valueOf
import java.util.*


class DummySaleFragment : Fragment(), CardServiceListener {

    private var _binding: FragmentDummySaleBinding? = null
    private val binding get() = _binding!!
    var activityContext: Context? = null //this is for getting context from activity class
    //private val notNullContext get() = _context!!
    var resultIntent: Intent? = null
    var saleIntent: Intent? = null
    var bundle: Bundle? = null
    var saleBundle: Bundle? = null
    var activationDB: ActivationDB? = null
    var transactionDB: TransactionDB? = null
    var mainActivity: MainActivity? = null
    private var cardServiceBinding: CardServiceBinding? = null
    private var boolReadCard = false
    var transactionService: TransactionService? = null

    private var menuItemList = mutableListOf<IListMenuItem>()
    private var card: ICCCard? = null

    companion object{
        var amount = 0
        var cardOwner =""
        var cardNumber = "**** ****"
        var cardData: String? = null
        var cardReadType = 0
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
            ArrayAdapter<String>(activityContext!!, R.layout.simple_spinner_dropdown_item, items)
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

    /**
     * define cardServiceBinding and make boolReadCard true because we read it.
     */
    private fun readCard(){
        cardServiceBinding = CardServiceBinding(activityContext!! as AppCompatActivity?,this )
        boolReadCard = true
    }

    private fun doSale(transactionCode: TransactionCode) {
        transactionService!!.mainActivity = mainActivity
        CoroutineScope(Dispatchers.Default).launch {
            val transactionResponse = transactionService!!.doInBackground(activityContext!!,
                amount, card!!,transactionCode,
                ContentValues(), null,false,null ,false)
            finishSale(transactionResponse!!)
        }
    }

    private fun finishSale(transactionResponse: TransactionResponse){
        Log.d("Transaction/Response","${transactionResponse.contentVal.toString()}")



        // if transactionResponse.getResponseCode == Success
        // PrepareSaleSlip
        // Slibe transactionResponse'un içindeki content val ve online Transaction Response parametre olarak ata
        // SalePrint   slip type müşteri ve iş yeri    2 tane to string yapılacak
        // örnek slip aynısını yap 2 to stringle
        //dummySale 212 248 bak onları yap

        val responseCode = transactionResponse.responseCode
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
                val salePrintHelper = SalePrintHelper()
                getNotNullBundle().putString("customerSlipData", salePrintHelper.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, transactionResponse.onlineTransactionResponse, activityContext!!,1, 1,false))
                getNotNullBundle().putString("merchantSlipData", salePrintHelper.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, transactionResponse.onlineTransactionResponse, activityContext!!,1, 1,false))
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
    private fun prepareDummyResponse (code: ResponseCode){

        var paymentType = PaymentTypes.CREDITCARD.type
        val cbMerchant = binding.cbMerchant
        val cbCustomer = binding.cbCustomer

        //this is for slip type
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
    fun onSaleResponseRetrieved(price: Int, code: ResponseCode, hasSlip: Boolean,
                                       slipType: SlipType, cardNo: String, ownerName: String, paymentType: Int){
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

    override fun onCardServiceConnected() {
        if (boolReadCard){
            try {
                val obj = JSONObject()
                obj.put("forceOnline", 1)
                obj.put("zeroAmount", 0)
                obj.put("fallback", 1)
                obj.put("cardReadTypes", 6)
                obj.put("qrPay", 1)

                Log.w("CardServiceBind/Dummy","$cardServiceBinding")
                cardServiceBinding!!.getCard(amount, 40, obj.toString())
                boolReadCard = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun prepareSaleMenu() {
        Log.d("PrepareSale","Girdi")
        menuItemList.add(MenuItem( "Sale", { menuItem ->
            doSale(TransactionCode.SALE)
        }))
        menuItemList.add(MenuItem("Installment Sale", { menuItem -> }))
        menuItemList.add(MenuItem("Loyalty Sale", { menuItem ->}))
        menuItemList.add(MenuItem("Campaign Sale", { menuItem -> }))
        val fragment = ListMenuFragment.newInstance(menuItemList,
                "Sale Type", false, null)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id,fragment)
            commit()
        }
    }



    override fun onCardDataReceived(cardData: String?) {
        try {
            val json = JSONObject(cardData)
            val type = json.getInt("mCardReadType")
            card = Gson().fromJson(cardData, ICCCard::class.java)
            if (card!!.resultCode == CardServiceResult.ERROR.resultCode()) {
                Log.d("Error","Girdi")
            }
            if (card!!.resultCode == CardServiceResult.SUCCESS.resultCode()) {
                Log.d("Success","Girdi")
                if (type == CardReadType.QrPay.type) {
                    //QrSale()
                    return
                }
                if (type == CardReadType.CLCard.type) {
                    cardReadType = CardReadType.CLCard.type
                    card = Gson().fromJson(cardData, ICCCard::class.java)
                } else if (type == CardReadType.ICC.type) {
                    card = Gson().fromJson(cardData, ICCCard::class.java)
                } else if (type == CardReadType.ICC2MSR.type || type == CardReadType.MSR.type || type == CardReadType.KeyIn.type) {
                    //card = Gson().fromJson(cardData, ICCCard::class.java)
                    //cardServiceBinding!!.getOnlinePIN(amount, card?.cardNumber, 0x0A01, 0, 4, 8, 30)
                }
                prepareSaleMenu()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onPinReceived(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onICCTakeOut() {
        TODO("Not yet implemented")
    }

}