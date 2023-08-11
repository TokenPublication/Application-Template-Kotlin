package com.tokeninc.sardis.application_template.ui.sale

import android.app.Activity
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.ListMenuFragment.MenuItemClickListener
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.enums.CardReadType
import com.tokeninc.sardis.application_template.enums.CardServiceResult
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.PaymentTypes
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.utils.StringHelper
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.String.valueOf

/**
 * This Class is for Sale operations, it depends on Transaction View Model
 * It has dummy sale layout, which is the only view that we created. Other ui elements come from ui library
 */
class SaleFragment(private val transactionViewModel: TransactionViewModel, private val mainActivity: MainActivity,
                   private val batchViewModel: BatchViewModel, private val cardViewModel: CardViewModel,
                   private val activationViewModel: ActivationViewModel) : Fragment() {

    private var _binding: FragmentDummySaleBinding? = null
    private val binding get() = _binding!!

    var card: ICCCard? = null
    private var amount = 0
    var installmentCount = 0 // this is for tracking instalment count if it will be an instalment sale
    var transactionCode = TransactionCode.SALE.type // this is for tracking transaction code, it can be also installment sale
    private var isICC = false
    companion object{
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

    /** Flow: Clicking Sale Button > Read Card > On Card Data Received > (if card is ICC) -> here
     * It is a sale menu, if user click sale it calls doSale() method
     */
    private fun prepareSaleMenu(mCard: ICCCard?) {
        card = mCard
        val menuItemList = transactionViewModel.menuItemList
        menuItemList.add(MenuItem( getStrings(R.string.sale), {
            cardReader(false)
        }))
        //TODO Developer check from parameterDB
        val isInstallment = true
        val isLoyalty = true
        val isCampaign = true
        if (isInstallment) menuItemList.add(MenuItem(getStrings(R.string.installment_sale), {
            cardViewModel.setTransactionCode(TransactionCode.INSTALLMENT_SALE.type)
            showInstallments()
        }))
        if (isLoyalty) menuItemList.add(MenuItem(getStrings(R.string.loyalty_sale), { })) //TODO transactionRoutine gitsin
        if (isCampaign) menuItemList.add(MenuItem(getStrings(R.string.campaign_sale), { })) //TODO transactionRoutine gitsin
        val listMenuFragment = ListMenuFragment.newInstance(menuItemList,
            getStrings(R.string.sale_type), false, R.drawable.token_logo_png)
        mainActivity.replaceFragment(listMenuFragment as Fragment)
    }

    /**
     * This method for show installment count choice screen. After user selects instCount, it calls cardReader method for read card.
     */
    private fun showInstallments() {
        val listener = MenuItemClickListener { menuItem: MenuItem? ->
            val itemName = menuItem!!.name.split(" ")
            installmentCount = itemName[0].toInt()
            mainActivity.popFragment()
            cardReader(false)
        }
        val maxInst = 12
        val menuItems = mutableListOf<IListMenuItem>()
        for (i in 2..maxInst) {
            val menuItem = MenuItem(i.toString() + " " + getStrings(R.string.installment), listener)
            menuItems.add(menuItem)
        }
        val instFragment = ListMenuFragment.newInstance(
            menuItems,
            getStrings(R.string.installment_sale),
            true,
            R.drawable.token_logo_png
        )
        mainActivity.replaceFragment(instFragment as Fragment)
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
            cardReader(false)
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

    fun cardReader(isGib: Boolean){
        if (installmentCount == 0){
            cardViewModel.setTransactionCode(TransactionCode.SALE.type)
        } else {
            cardViewModel.setTransactionCode(TransactionCode.INSTALLMENT_SALE.type)
        }
        cardViewModel.setAmount(amount) // set its sale amount
        mainActivity.readCard()
        cardViewModel.getCardLiveData().observe(mainActivity) { cardData -> //firstly observing cardData
            if (cardData != null && cardData.resultCode != CardServiceResult.USER_CANCELLED.resultCode()) { //when the cardData is not null (it is updated after onCardDataReceived)
                Log.d("CardResult", cardData.mCardNumber.toString())
                this.card = cardData
                if (isGib){ // if gibSale
                    doSale(null)
                } else{
                    val cardReadType = cardData.mCardReadType
                    Log.d("Card Read type",cardReadType.toString())
                    if (cardReadType == CardReadType.QrPay.type){ //if qrPay
                        qrSale()
                    } else if (cardReadType == CardReadType.ICC.type && !isICC){ // if read as ICC for the first time (it reads twice)
                        isICC = true
                        cardViewModel.resetCard()
                        prepareSaleMenu(card)
                    } else{ // if read Contactless or ICC for the second time
                        doSale(null)
                    }
                }
            }
        }
    }

    private var isCancelable = true
    private var qrSuccess = true
    private fun qrSale() {
        val dialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Progress, "Please Wait", true)
        Handler(Looper.getMainLooper()).postDelayed({
            cardViewModel.getCardServiceBinding()?.showQR(
                "PLEASE READ THE QR CODE",
                StringHelper().getAmount(amount),
                "QR Code Test"
            ) // Shows QR on the back screen
            dialog!!.setQr(
                "QR Code Test",
                "Waiting For the QR Code to Read"
            ) // Shows the same QR on Info Dialog
            Handler(Looper.getMainLooper()).postDelayed({
                if (qrSuccess) {
                    mainActivity.showInfoDialog(
                        InfoDialog.InfoType.Confirmed,
                        "QR " + getString(R.string.trans_successful),
                        false
                    )
                    isCancelable = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        doSale(null)
                    }, 3000)
                }
            }, 5000)
        }, 2000)
        dialog!!.setDismissedListener {
            if (isCancelable) {
                qrSuccess = false
                mainActivity.setResult(Activity.RESULT_CANCELED)
                mainActivity.callbackMessage(ResponseCode.CANCELED)
            }
        }
    }

    /** Flow: Clicking Sale Button > Read Card > On Card Data Received > (if card is Contactless) -> here
     *  else -> Prepare Sale Menu > Click Sale > here
     * It creates a transaction response in Default Coroutine Thread with doInBackGround method on Transaction Service
     * after transactionResponse is returning, it calls finishSale method
     */
    fun doSale(cardData: String?) {
        if (!cardData.isNullOrEmpty()){
            card = Gson().fromJson(cardData, ICCCard::class.java)
        }
        val saleBundle = generateBundle()
        CoroutineScope(Dispatchers.Default).launch {
            transactionViewModel.transactionRoutine(card!!,transactionCode, saleBundle,
                ContentValues(), batchViewModel, mainActivity, activationViewModel.activationRepository)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        transactionViewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is TransactionViewModel.UIState.Loading -> mainActivity.showDialog(dialog)
                is TransactionViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress,"Connecting % ${state.data}")
                is TransactionViewModel.UIState.Success -> Log.i("Transaction Result: "," Success")
            }
        }
        transactionViewModel.getLiveIntent().observe(mainActivity){liveIntent ->
            mainActivity.setResult(liveIntent)
        }
    }

    /**
     * It prepares bundle with necessary parameters instead of passing parameters one by one
     */
    private fun generateBundle(): Bundle {
        val bundle = Bundle()
        val uuid = mainActivity.intent.extras?.getString("UUID")
        val zNO = mainActivity.intent.extras?.getString("ZNO")
        val receiptNo = mainActivity.intent.extras?.getString("ReceiptNo")

        //TODO Developer, check this variables from PGW.
        val isOnlinePin = false
        val isOffline = false
        val pinByPass = false
        bundle.putInt("IsOnlinePin", if (isOnlinePin) 1 else 0)
        bundle.putInt("IsOffline", if (isOffline) 1 else 0)
        bundle.putInt("PinByPass", if (pinByPass) 1 else 0)
        bundle.putString("UUID", uuid)
        if (zNO != null && receiptNo != null) {
            bundle.putString("ZNO", zNO)
            bundle.putString("ReceiptNo", receiptNo)
        }
        if (installmentCount != 0){
            transactionCode = TransactionCode.INSTALLMENT_SALE.type
            bundle.putInt(ExtraKeys.INST_COUNT.name, installmentCount) // add installment count to pass onlineTransactionResponse
        } else {
            transactionCode = TransactionCode.SALE.type
        }
        return bundle
    }

    /**
     * This is a dummy response, it is doing nothing its only mission is to show how to simulate buttons for now.
     */
    private fun prepareDummyResponse (code: ResponseCode){

        var paymentType = PaymentTypes.CREDITCARD.type
        val cbMerchant = binding.cbMerchant
        val cbCustomer = binding.cbCustomer
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

    fun setAmount(amount:Int){
        this.amount = amount
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
