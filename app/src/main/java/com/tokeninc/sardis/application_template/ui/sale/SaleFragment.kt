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
import com.tokeninc.sardis.application_template.data.model.card.CardServiceResult
import com.tokeninc.sardis.application_template.data.model.card.ICCCard
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.data.model.type.CardReadType
import com.tokeninc.sardis.application_template.data.model.type.PaymentType
import com.tokeninc.sardis.application_template.data.model.type.SlipType
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.utils.ExtraKeys
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
    private var installmentCount = 0 // this is for tracking instalment count if it will be an instalment sale
    private var transactionCode = TransactionCode.SALE.type // this is for tracking transaction code, it can be also installment sale
    private var isICC = false
    private var isCancelable = true
    private var qrSuccess = true
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
            showInstallments()
        }))
        if (isLoyalty) menuItemList.add(MenuItem(getStrings(R.string.loyalty_sale), { }))
        if (isCampaign) menuItemList.add(MenuItem(getStrings(R.string.campaign_sale), { }))
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
            valueOf(PaymentType.CREDITCARD),
            valueOf(PaymentType.TRQRCREDITCARD),
            valueOf(PaymentType.TRQRFAST),
            valueOf(PaymentType.TRQRMOBILE),
            valueOf(PaymentType.TRQROTHER),
            valueOf(PaymentType.OTHER)
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
        val transactionCode = if (installmentCount == 0) TransactionCode.SALE.type
        else TransactionCode.INSTALLMENT_SALE.type

        mainActivity.readCard(amount,transactionCode)
        cardViewModel.getCardLiveData().observe(mainActivity) { cardData -> //firstly observing cardData
            if (cardData != null && cardData.resultCode == CardServiceResult.SUCCESS.resultCode()) { //when the cardData is not null (it is updated after onCardDataReceived)
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

    /**
     * This method for shows the QR and perform dummy QR sale.
     * It only updates the UI, not performing any sale operation.
     */
    private fun qrSale() {
        val dialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Progress, "Please Wait", true)
        Handler(Looper.getMainLooper()).postDelayed({
            cardViewModel.getCardServiceBinding()?.showQR(getStrings(R.string.waiting_qr_read), StringHelper().getAmount(amount),
                "QR Code Test"
            ) // Shows QR on the back screen
            dialog!!.setQr("QR Code Test", getStrings(R.string.waiting_qr_read)) // Shows the same QR on Info Dialog
            Handler(Looper.getMainLooper()).postDelayed({
                if (qrSuccess) {
                    mainActivity.showInfoDialog(
                        InfoDialog.InfoType.Confirmed,
                        "QR " + getString(R.string.trans_successful),
                        false
                    )
                    isCancelable = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        mainActivity.finish()
                    }, 3000)
                }
            }, 5000)
        }, 2000)
        dialog!!.setDismissedListener {
            if (isCancelable) {
                qrSuccess = false
                mainActivity.setResult(Activity.RESULT_CANCELED)
                mainActivity.responseMessage(ResponseCode.CANCELED,"")
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
                is TransactionViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress,getStrings(R.string.connecting)+" %"+state.data)
                is TransactionViewModel.UIState.Success -> dialog.update(InfoDialog.InfoType.Confirmed, getStrings(R.string.confirmation_code)+": "+state.message)
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
        if (uuid != null) {
            Log.i("Sale UUID",uuid)
        }
        val zNO = mainActivity.intent.extras?.getString("ZNO")
        val receiptNo = mainActivity.intent.extras?.getString("ReceiptNo")

        bundle.putString("UUID", uuid)
        if (zNO != null && receiptNo != null) { // zNo and receiptNo comes sales in 1000TR
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

        var paymentType = PaymentType.CREDITCARD.type
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
            when (spinner.selectedItem.toString()) {
                valueOf(PaymentType.TRQRCREDITCARD) -> paymentType = PaymentType.TRQRCREDITCARD.type
                valueOf(PaymentType.TRQRFAST) -> paymentType = PaymentType.TRQRFAST.type
                valueOf(PaymentType.TRQRMOBILE) -> paymentType = PaymentType.TRQRMOBILE.type
                valueOf(PaymentType.TRQROTHER) -> paymentType = PaymentType.TRQROTHER.type
                valueOf(PaymentType.OTHER) -> paymentType = PaymentType.OTHER.type
            }
        }
        onSaleResponseRetrieved(amount, code, slipType, paymentType)
    }

    /**
     * This method for only dummy sale that did in Sale UI (success, error, declined)
     */
    private fun onSaleResponseRetrieved(
        price: Int,
        code: ResponseCode,
        slipType: SlipType,
        paymentType: Int
    ) {

        transactionViewModel.prepareDummyResponse(
           price, code, slipType, paymentType,activationViewModel.merchantID(),activationViewModel.terminalID(),batchViewModel)
        transactionViewModel.getLiveIntent().observe(viewLifecycleOwner) { resultIntent ->
            if (code === ResponseCode.SUCCESS) {
                mainActivity.showInfoDialog(
                    InfoDialog.InfoType.Confirmed,
                    getString(R.string.trans_successful),
                    false
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    mainActivity.setResult(Activity.RESULT_OK, resultIntent)
                    mainActivity.finish()
                }, 2000)
            } else {
                mainActivity.responseMessage(code, "",resultIntent)
            }
        }
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
