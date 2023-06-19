package com.tokeninc.sardis.application_template.ui

import MenuItem
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.viewmodels.ActivationViewModel
import com.tokeninc.sardis.application_template.viewmodels.BatchViewModel
import com.tokeninc.sardis.application_template.entities.col_names.TransactionCols
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.enums.*
import com.tokeninc.sardis.application_template.helpers.StringHelper
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintService
import com.tokeninc.sardis.application_template.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.services.TransactionService
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.String.valueOf

/**
 * This Class is for Sale operations, it depends on Transaction View Model
 * It has dummy sale layout, which is the only view that we created. Other ui elements come from tokeninc.ui library
 */
class DummySaleFragment(private val viewModel: TransactionViewModel) : Fragment() {

    private var _binding: FragmentDummySaleBinding? = null
    private val binding get() = _binding!!

    private lateinit var bundle: Bundle
    private lateinit var intent: Intent
    private lateinit var activationViewModel: ActivationViewModel
    private lateinit var batchViewModel: BatchViewModel
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionService: TransactionService
    var card: ICCCard? = null
    private var amount = 0

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

    /**
     * This is for initializing some variables on that class, it is called from mainActivity before this class is called
     */
    fun setter(mainActivity: MainActivity, bundle: Bundle, intent:Intent, activationViewModel: ActivationViewModel, batchViewModel: BatchViewModel, transactionService: TransactionService, amount:Int ){
        this.mainActivity = mainActivity
        this.bundle = bundle
        this.intent = intent
        this.amount = amount
        this.transactionService = transactionService
        this.activationViewModel = activationViewModel
        this.batchViewModel = batchViewModel
    }

    /** Flow: Clicking Sale Button > Read Card > On Card Data Received > (if card is ICC) -> here
     * It is a sale menu, if user click sale it calls doSale() method
     */
    fun prepareSaleMenu(mCard: ICCCard?) {
        card = mCard
        mainActivity.transactionCode = 0
        val menuItemList = viewModel.menuItemList
        menuItemList.add(MenuItem( getStrings(R.string.sale), {
            doSale()
        }))
        menuItemList.add(MenuItem(getStrings(R.string.Installment_sale), { }))
        menuItemList.add(MenuItem(getStrings(R.string.loyalty_sale), { }))
        menuItemList.add(MenuItem(getStrings(R.string.campaign_sale), { }))
        val fragment = ListMenuFragment.newInstance(menuItemList,
            getStrings(R.string.sale_type), false, null)
        mainActivity.replaceFragment(fragment)
    }

    /**
     * This method is for preparing Spinner on layout, Spinner contains 6 Payment Methods
     */
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
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(mainActivity, R.layout.spinner_item, items)
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = listener
    }

    /**
     * These are listeners for buttons. If user clicks Sale button, transaction operation will start with reading card.
     */
    private fun clickButtons(){
        binding.btnSale.setOnClickListener {
            mainActivity.transactionCode = TransactionCode.SALE.type
            mainActivity.readCard()
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

    /** Flow: Clicking Sale Button > Read Card > On Card Data Received > (if card is Contactless) -> here
     *  else -> Prepare Sale Menu > Click Sale > here
     * It creates a transaction response in Default Coroutine Thread with doInBackGround method on Transaction Service
     * after transactionResponse is returning, it calls finishSale method
     */
    fun doSale() {
        CoroutineScope(Dispatchers.Default).launch {
        val transactionResponse = transactionService.doInBackground(mainActivity, amount, card!!,TransactionCode.SALE.type,
            ContentValues(), null,false,null ,false)
        finishSale(transactionResponse!!)
        }
    }

    /** This method is called from doSale() method. It puts required values to bundle (something like contentValues for data transferring).
     * After that, this bundle is put to intent and that intent is assigned to mainActivity.
     * This intent ensures IPC between application and GiB.
     */
    private fun finishSale(transactionResponse: TransactionResponse){
        Log.d("Transaction/Response","responseCode:${transactionResponse.responseCode} ContentVals: ${transactionResponse.contentVal}")

        val responseCode = transactionResponse.responseCode
        if (responseCode == ResponseCode.SUCCESS){
            bundle.putInt("ResponseCode", responseCode.ordinal) //TODO bunu diğerlerinde de yap
            bundle.putInt("PaymentStatus", 0) // #2 Payment Status
            bundle.putInt("Amount", amount ) // #3 Amount
            bundle.putInt("Amount2", 0)
            bundle.putBoolean("IsSlip", true)
            bundle.putInt("BatchNo", batchViewModel.batchNo)
            bundle.putString("CardNo", StringHelper().maskCardForBundle(card!!.mCardNumber!!))
            bundle.putString("MID", mainActivity.currentMID)
            bundle.putString("TID", mainActivity.currentTID)
            bundle.putInt("TxnNo",batchViewModel.groupSN)
            bundle.putInt("PaymentType", PaymentTypes.CREDITCARD.type) //TODO check it

            var slipType: SlipType = SlipType.NO_SLIP
            if (responseCode == ResponseCode.CANCELED || responseCode == ResponseCode.UNABLE_DECLINE || responseCode == ResponseCode.OFFLINE_DECLINE) {
                slipType = SlipType.NO_SLIP
            }
            else{
                if (transactionResponse.responseCode == ResponseCode.SUCCESS){
                    val printHelper = PrintService()
                    bundle.putString("customerSlipData", printHelper.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, null, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false))
                    bundle.putString("merchantSlipData", printHelper.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, null, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false))
                    bundle.putString("RefundInfo", getRefundInfo(transactionResponse))
                    if(transactionResponse.contentVal != null) {
                        bundle.putString("RefNo", transactionResponse.contentVal!!.getAsString(
                            TransactionCols.Col_HostLogKey))
                        bundle.putString("AuthNo", transactionResponse.contentVal!!.getAsString(
                            TransactionCols.Col_AuthCode))
                    }
                }
            }
            bundle.putInt("SlipType", slipType.value) //TODO fail receipt yap
            intent.putExtras(bundle)
            mainActivity.setResult(intent)
        }
    }

    /** @return refundInfo which is Json with necessary components
     *
     */
    private fun getRefundInfo(transactionResponse: TransactionResponse): String {
        val json = JSONObject()
        val transaction = transactionResponse.contentVal
        try {
            json.put("BatchNo", batchViewModel.batchNo)
            json.put("TxnNo", batchViewModel.groupSN)
            json.put("Amount", amount)
            json.put("RefNo", transaction!!.getAsString(TransactionCols.Col_HostLogKey))
            json.put("AuthCode", transaction.getAsString(TransactionCols.Col_AuthCode))
            json.put("TranDate", transaction.getAsString(TransactionCols.Col_TranDate))
            json.put("MID",mainActivity.currentMID)
            json.put("TID",mainActivity.currentTID)
            json.put("CardNo",card!!.mCardNumber!!)
            if (transaction.getAsInteger(TransactionCols.Col_InstCnt) != null && transaction.getAsInteger(
                    TransactionCols.Col_InstCnt) > 0) {
                //val installment = JSONObject()
                json.put("InstCount", transaction.getAsInteger(TransactionCols.Col_InstCnt))
                json.put("InstAmount", transaction.getAsInteger(TransactionCols.Col_InstAmount))
                //json.put("Installment", installment)
            }
            else{
                json.put("InstCount", 0)
                json.put("InstAmount", 0)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json.toString()
    }

    /**
     * This is a dummy response, it is doing nothing its only mission is to show how to simulate buttons for now.
     */
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
            val text: String = spinner.selectedItem.toString()
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

        //onSaleResponseRetrieved(amount, code, true, slipType, "1234 **** **** 7890", "OWNER NAME", paymentType)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    /**
     * Fragment couldn't use getString from res > values > strings, therefore this method call that string from mainActivity.
     */
    private fun getStrings(resID: Int): String{
        return mainActivity.getString(resID)
    }

}
