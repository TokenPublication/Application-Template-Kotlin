package com.tokeninc.sardis.application_template.ui.postTxn.refund

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.CustomInput.InputValidator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.ListMenuFragment.MenuItemClickListener
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.databinding.FragmentRefundBinding
import com.tokeninc.sardis.application_template.enums.CardServiceResult
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * This is the fragment for the Refund actions. Refund operation methods are defined here.
 */
class RefundFragment(private val mainActivity: MainActivity, private val cardViewModel: CardViewModel,
                     private val transactionViewModel: TransactionViewModel, private val batchViewModel: BatchViewModel,
private val activationViewModel: ActivationViewModel) : Fragment() {

    private var _binding: FragmentRefundBinding? = null
    private val binding get() = _binding!!
    private var transactionCode = 0
    private var refundBundle = Bundle()

    companion object{
        private var installmentCount = 0
        private lateinit var viewModel: RefundViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefundBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(this)[RefundViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMenu()
    }

    /**
     * It prepares list menu item and shows it to the screen.
     */
    private fun showMenu(){
        val menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem(getStrings(R.string.matched_refund), {
            showMatchedReturnFragment()
        }))
        menuItems.add(MenuItem(getStrings(R.string.cash_refund), {
            showReturnFragment()
        }))
        menuItems.add(MenuItem(getStrings(R.string.installment_refund), {
            showInstallments()
        }))
        menuItems.add(MenuItem(getStrings(R.string.loyalty_refund), {}))
        viewModel.list = menuItems
        viewModel.replaceFragment(mainActivity)
    }

    /**
     * It shows Matched Refund with preparing corresponding inputs similar to Installment Refund
     * The only difference is it contains Reference Number and Authorization Code.
     */
    private fun showMatchedReturnFragment() {
        transactionCode = TransactionCode.MATCHED_REFUND.type
        if (installmentCount != 0) {
            transactionCode = TransactionCode.INSTALLMENT_REFUND.type
        }
        val inputList = mutableListOf<CustomInputFormat>()
        addInputAmount(inputList)
        addInputRetAmount(inputList)
        addInputRefNo(inputList)
        addInputAuthCode(inputList)
        addInputTranDate(inputList)
        showRefundFragment(inputList)
    }

    /**
     * It is similar to other fragment, it only contains original amount.
     */
    private fun showReturnFragment(){
        transactionCode = TransactionCode.CASH_REFUND.type
        val inputList: MutableList<CustomInputFormat> = mutableListOf()
        addInputAmount(inputList)
        showRefundFragment(inputList)
    }

    /**
     * It prepares a menu that contains 2 to 12 installment, Installment count updating with respect to clicked item
     */
    private fun showInstallments() {
        val listener = MenuItemClickListener { menuItem: MenuItem? ->
            val itemName = menuItem!!.name.split(" ")
            installmentCount = itemName[0].toInt()
            showMatchedReturnFragment()
        }
        val maxInst = 12
        val menuItems = mutableListOf<IListMenuItem>()
        for (i in 2..maxInst) {
            val menuItem = MenuItem(i.toString() + " " + getStrings(R.string.installment), listener)
            menuItems.add(menuItem)
        }
        val instFragment = ListMenuFragment.newInstance(menuItems, getStrings(R.string.installment_refund), true, R.drawable.token_logo_png)
       mainActivity.addFragment(instFragment as Fragment)
    }

    /**
     * It adds values to stringExtraContent to use it later. Then calls readCard operation.
     */
    private fun showRefundFragment(inputList: MutableList<CustomInputFormat>){
        val fragment = InputListFragment.newInstance(inputList, getStrings(R.string.refund)){
            cardViewModel.setTransactionCode(transactionCode) // arrange transaction code in cardRepository to read card correctly
            // arrange amount to read card with given amount
            if (transactionCode == TransactionCode.CASH_REFUND.type){
                cardViewModel.setAmount(inputList[0].text.toInt())
            } else if (transactionCode == TransactionCode.INSTALLMENT_REFUND.type || transactionCode == TransactionCode.MATCHED_REFUND.type){
                cardViewModel.setAmount(inputList[1].text.toInt())
            }
            mainActivity.readCard()
            enterRefund = false
            refundAfterReadCard(inputList, null)
        }
        mainActivity.addFragment(fragment)
    }

    private var enterRefund = false //TODO bak buna

    /**
     * This function is called after card Service is connected.
     * It arranges stringExtraContent with respect to refund type. After read card, it calls to refundRoutine
     */
    fun refundAfterReadCard(input_list: List<CustomInputFormat>?, bundle: Bundle?){
        var isGib = false
        if (bundle != null){ //if bundle is not null, that means it comes from gib
            isGib = true
            refundBundle = bundle
            transactionCode = TransactionCode.MATCHED_REFUND.type
        } else {
            refundBundle = generateRefundBundle(transactionCode,input_list!!)
        }
        cardViewModel.getCardLiveData().observe(mainActivity){cardData ->
            if (cardData != null && cardData.resultCode != CardServiceResult.USER_CANCELLED.resultCode()) { //when the cardData is not null (it is updated after onCardDataReceived)
                Log.d("Refund Card Number", cardData.mCardNumber.toString())
                if (isGib){
                    if (refundBundle.getString(ExtraKeys.CARD_NO.name).equals(cardData.mCardNumber)){ // if cardNumbers are matching
                        doRefund(cardData,TransactionCode.MATCHED_REFUND.type)
                    }
                    else{
                        mainActivity.callbackMessage(ResponseCode.OFFLINE_DECLINE)
                    }
                } else{
                    doRefund(cardData,transactionCode) // start this operation with the card data
                }
            }
        }

    }

    /**
     * After reading card, refund will be added to Transaction table with this function in parallel.
     */
    private fun doRefund(card: ICCCard, transactionCode: Int){
        CoroutineScope(Dispatchers.Default).launch {
            transactionViewModel.transactionRoutine(card, transactionCode,refundBundle, ContentValues(),
                 batchViewModel, mainActivity, activationViewModel.activationRepository)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        transactionViewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is TransactionViewModel.UIState.Loading -> mainActivity.showDialog(dialog)
                is TransactionViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress,"Connecting % ${state.data}")
                is TransactionViewModel.UIState.Success -> mainActivity.showDialog(
                    InfoDialog.newInstance(
                        InfoDialog.InfoType.Progress,"Printing Slip",true))
            }
        }
        transactionViewModel.getLiveIntent().observe(mainActivity){liveIntent ->
            mainActivity.setResult(liveIntent)
        }
    }

    private fun generateRefundBundle(transactionCode: Int, inputList: List<CustomInputFormat>): Bundle {
        val bundle = Bundle()
        if (transactionCode == TransactionCode.MATCHED_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type) {
            bundle.putInt(ExtraKeys.ORG_AMOUNT.name, inputList[0].text.toInt())
            bundle.putInt(ExtraKeys.REFUND_AMOUNT.name, inputList[1].text.toInt())
            bundle.putString(ExtraKeys.REF_NO.name, inputList[2].text)
            bundle.putString(ExtraKeys.AUTH_CODE.name, inputList[3].text)
            bundle.putString(ExtraKeys.TRAN_DATE.name, inputList[4].text)
            if (transactionCode == TransactionCode.INSTALLMENT_REFUND.type){
                bundle.putInt(ExtraKeys.INST_COUNT.name, installmentCount)
            }
        } else if (transactionCode == TransactionCode.CASH_REFUND.type) {
            bundle.putInt(ExtraKeys.REFUND_AMOUNT.name, inputList[0].text.toInt())
        }
        //TODO Developer, check this variables from PGW.
        val isOnlinePin = false
        val isOffline = false
        val pinByPass = false
        bundle.putInt("IsOnlinePin", if (isOnlinePin) 1 else 0)
        bundle.putInt("IsOffline", if (isOffline) 1 else 0)
        bundle.putInt("PinByPass", if (pinByPass) 1 else 0)
        return bundle
    }

    /**
     * This method is for creating input amount with respect to validator.
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputAmount(inputList: MutableList<CustomInputFormat>){
        val inputOrgAmount = CustomInputFormat(
            getStrings(R.string.original_amount),
            EditTextInputType.Amount,
            null,
            getStrings(R.string.invalid_amount),
            InputValidator { input: CustomInputFormat ->
                val amount = if (input.text.isEmpty()) 0 else input.text.toInt()
                amount > 0
            } )
        inputList.add(inputOrgAmount)
    }

    /**
     * This creates return amount with respect to validator which checks whether that amount smaller than original amount
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputRetAmount(inputList: MutableList<CustomInputFormat>){
        val inputRetAmount = CustomInputFormat(
            getStrings(R.string.refund_amount),
            EditTextInputType.Amount,
            null,
            getStrings(R.string.invalid_amount),
            InputValidator {
                val amount = if (it.text.isEmpty()) 0 else it.text.toInt()
                val original = if (inputList[0].text.isEmpty()) 0 else inputList[0].text.toInt()
                amount in 1..original
            } )
        inputList.add(inputRetAmount)
    }

    /**
     * This method is for creating input reference number with respect to validator which checks if reference number is not null
     * (Refund from Gib) it compares whether reference numbers are matching else -> checks for 9 digits.
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputRefNo(inputList: MutableList<CustomInputFormat>){
        val inputRefNo = CustomInputFormat(
            getStrings(R.string.ref_no),
            EditTextInputType.Number,
            9,
            getStrings(R.string.ref_no_invalid_ten_digits)
        ) { customInputFormat: CustomInputFormat ->
            customInputFormat.text.length == 9
        }
        inputList.add(inputRefNo)
    }

    /**
     * This method is for creating input authorization code respect to validator which checks whether there is 6 digits or not.
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputAuthCode(inputList: MutableList<CustomInputFormat>){
        val inputAuthCode = CustomInputFormat(
            getStrings(R.string.confirmation_code),
            EditTextInputType.Number,
            6,
            getStrings(R.string.confirmation_code_invalid_six_digits)
        ) { customInputFormat: CustomInputFormat -> customInputFormat.text.length == 6 }
        inputList.add(inputAuthCode)
    }

    /**
     * This method is for creating input transaction date
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    @SuppressLint("SimpleDateFormat")
    private fun addInputTranDate(inputList: MutableList<CustomInputFormat>){
        val inputTranDate = CustomInputFormat(getStrings(R.string.tran_date),
            EditTextInputType.Date,
            null,
            getStrings(R.string.tran_date_invalid),
            InputValidator { customInputFormat: CustomInputFormat ->
                try {
                    val array = customInputFormat.text.split("/").toTypedArray()
                    val date = array[2].substring(2) + array[1] + array[0]
                    val now = Calendar.getInstance().time
                    val sdf = SimpleDateFormat("yyMMdd")
                    return@InputValidator sdf.format(now).toInt() >= date.toInt()
                } catch (_: Exception) {
                }
                false
            }
        )
        inputList.add(inputTranDate)
    }

    /**
     * this function needs for getting string from activity otherwise it causes an error because we update UI in mainActivity
     */
    private fun getStrings(resID: Int): String{
        return mainActivity.getString(resID)
    }
}
