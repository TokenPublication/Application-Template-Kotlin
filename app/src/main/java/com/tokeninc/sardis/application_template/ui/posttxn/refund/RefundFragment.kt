package com.tokeninc.sardis.application_template.ui.posttxn.refund

import com.tokeninc.sardis.application_template.ui.MenuItem
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
import com.tokeninc.sardis.application_template.databinding.FragmentRefundBinding
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * This is the fragment for the Refund actions. Refund operation methods are defined here.
 */
class RefundFragment(private val mainActivity: MainActivity, private val cardViewModel: CardViewModel,
                     private val transactionViewModel: TransactionViewModel, private val batchViewModel: BatchViewModel) : Fragment() {
    private var _binding: FragmentRefundBinding? = null
    private val binding get() = _binding!!


    private lateinit var card: ICCCard
    private var extraContent = ContentValues()  //at the end of every Refund we finish mainActivity so no need to delete it at everytime
    private var stringExtraContent = ContentValues() //this is for switching customInput format type to string

    companion object{
        lateinit var inputOrgAmount: CustomInputFormat
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
     * this function needs for getting string from activity otherwise it causes an error because we update UI in mainActivity
     */
    private fun getStrings(resID: Int): String{
        return mainActivity.getString(resID)
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
        menuItems.add(MenuItem(getStrings(R.string.loyalty_refund), {
            //showLoyaltyRefundFragment()
        }))
        viewModel.list = menuItems
        viewModel.replaceFragment(mainActivity)
    }

    /**
     * It shows Installment Refund with preparing corresponding inputs and puts those inputs ->
     * Original Amount && Return Amount && Transaction Date and also installment count
     * to extraContent which will be sent to Transaction Service
     */
    private fun showInstallmentRefundFragment(){
        val inputList = mutableListOf<CustomInputFormat>()
        cardViewModel.setTransactionCode(TransactionCode.INSTALLMENT_REFUND.type)
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputTranDate(inputList)
        stringExtraContent.put(ExtraKeys.INST_COUNT.name, installmentCount)
        addFragment(inputList)
    }

    /**
     * It shows Matched Refund with preparing corresponding inputs similar to Installment Refund
     * The only difference is it contains Reference Number and Authorization Code.
     */
    private fun showMatchedReturnFragment() {
        cardViewModel.setTransactionCode(TransactionCode.MATCHED_REFUND.type)
        val inputList = mutableListOf<CustomInputFormat>()
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputRefNo(inputList,extraContent)
        addInputAuthCode(inputList,extraContent)
        addInputTranDate(inputList)
        addFragment(inputList)
    }

    /**
     * It is similar to other fragment, it only contains original amount.
     */
    private fun showReturnFragment(){
        cardViewModel.setTransactionCode(TransactionCode.CASH_REFUND.type)
        val inputList: MutableList<CustomInputFormat> = mutableListOf()
        addInputAmount(inputList,extraContent)
        addFragment(inputList)
    }

    /**
     * It prepares a menu that contains 2 to 12 installment, Installment count updating with respect to clicked item
     */
    private fun showInstallments() {
        val listener = MenuItemClickListener { menuItem: MenuItem? ->
            val itemName = menuItem!!.name.split(" ")
            installmentCount = itemName[0].toInt()
            showInstallmentRefundFragment()
        }
        val maxInst = 12
        val menuItems = mutableListOf<IListMenuItem>()
        for (i in 2..maxInst) {
            val menuItem = MenuItem(i.toString() + " " + getStrings(R.string.installment), listener)
            menuItems.add(menuItem)
        }
        val instFragment = ListMenuFragment.newInstance(
            menuItems,
            getStrings(R.string.installment_refund),
            true,
            R.drawable.token_logo
        )
       mainActivity.addFragment(instFragment as Fragment)
    }


    /**
     * It adds values to stringExtraContent to use it later. Then calls readCard operation.
     */
    private fun addFragment(inputList: MutableList<CustomInputFormat>){
        val fragment = InputListFragment.newInstance(
            inputList, getStrings(R.string.refund)
        ){ list: List<String?>? ->
            mainActivity.connectCardService()
            enterRefund = false
            startRefundAfterConnected(list)
        }

        mainActivity.addFragment(fragment)
    }

    private var enterRefund = false

    /**
     * This function is called after card Service is connected.
     * It arranges stringExtraContent with respect to refund type. After read card, it calls to refundRoutine
     */
    private fun startRefundAfterConnected(input_list: List<String?>?){
        if (!enterRefund){
            enterRefund = true
            cardViewModel.getTransactionCode().observe(mainActivity) { transactionCode ->
                when (transactionCode) {
                    // set extra contents with respect to input list
                    TransactionCode.MATCHED_REFUND.type -> {
                        stringExtraContent.put(ExtraKeys.ORG_AMOUNT.name,input_list!![0])
                        stringExtraContent.put(ExtraKeys.REFUND_AMOUNT.name,input_list[1])
                        stringExtraContent.put(ExtraKeys.REF_NO.name,input_list[2])
                        stringExtraContent.put(ExtraKeys.AUTH_CODE.name,input_list[3])
                        stringExtraContent.put(ExtraKeys.TRAN_DATE.name,input_list[4])
                    }
                    TransactionCode.INSTALLMENT_REFUND.type -> {
                        stringExtraContent.put(ExtraKeys.ORG_AMOUNT.name,input_list!![0])
                        stringExtraContent.put(ExtraKeys.REFUND_AMOUNT.name,input_list[1])
                        stringExtraContent.put(ExtraKeys.TRAN_DATE.name,input_list[2])
                    }
                    else -> {
                        stringExtraContent.put(ExtraKeys.ORG_AMOUNT.name, input_list!![0])
                    }
                }
                if (transactionCode == TransactionCode.CASH_REFUND.type){
                    cardViewModel.setAmount(stringExtraContent.getAsString(ExtraKeys.ORG_AMOUNT.name).toInt())
                } else if (transactionCode == TransactionCode.INSTALLMENT_REFUND.type ||transactionCode == TransactionCode.MATCHED_REFUND.type){
                    cardViewModel.setAmount(stringExtraContent.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt())
                }
                cardViewModel.getCardLiveData().observe(mainActivity){card ->
                    if (card != null) { //when the cardData is not null (it is updated after onCardDataReceived)
                        Log.d("CardResult", card.mCardNumber.toString())
                        refundRoutine(card,transactionCode) // start this operation with the card data
                    }
                }
            }
        }
    }

    /**
     * It is called when refund action received by gib. It checks if the card data are matching, if it is then call refundRoutine
     */
    fun gibRefund(extraContents: ContentValues){
        cardViewModel.setTransactionCode(TransactionCode.MATCHED_REFUND.type)
        transactionViewModel.extraContents = extraContents
        cardViewModel.getCardLiveData().observe(mainActivity){ cardData ->
            if (cardData != null) {
                Log.d("Card Read", cardData.mCardNumber.toString())
                if (extraContents.getAsString(ExtraKeys.CARD_NO.name).equals(cardData.mCardNumber)){
                    stringExtraContent = extraContents //initializing stringExtraContents to use it later.
                    refundRoutine(cardData,TransactionCode.MATCHED_REFUND.type)
                }
                else
                    mainActivity.callbackMessage(ResponseCode.OFFLINE_DECLINE)
            }
        }
    }

    /**
     * After reading card, refund will be added to Transaction table with this function in parallel.
     */
    private fun refundRoutine(mCard: ICCCard?, transactionCode: Int){
        card = mCard!!
        CoroutineScope(Dispatchers.Default).launch {
            transactionViewModel.transactionRoutine(stringExtraContent.getAsString(ExtraKeys.ORG_AMOUNT.name).toInt()
                ,card, transactionCode, stringExtraContent,null,false,null,false, batchViewModel, mainActivity.currentMID
                ,mainActivity.currentTID, mainActivity)
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

    /**
     * This method is for creating input amount with respect to validator.
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputAmount(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        inputOrgAmount = CustomInputFormat(
            getStrings(R.string.original_amount),
            EditTextInputType.Amount,
            null,
            getStrings(R.string.invalid_amount),
            InputValidator { input: CustomInputFormat ->
                val amount = if (input.text.isEmpty()) 0 else input.text.toInt()
                amount > 0
            } )
        extraContentValues.put(ExtraKeys.ORG_AMOUNT.name, inputOrgAmount.toString())
        inputList.add(inputOrgAmount)
    }

    /**
     * This creates return amount with respect to validator which checks whether that amount smaller than original amount
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputRetAmount(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        val inputRetAmount = CustomInputFormat(
            getStrings(R.string.refund_amount),
            EditTextInputType.Amount,
            null,
            getStrings(R.string.invalid_amount),
            InputValidator {
                val amount = if (it.text.isEmpty()) 0 else it.text.toInt()
                val original = if (inputOrgAmount.text.isEmpty()) 0 else inputOrgAmount.text.toInt()
                amount in 1..original
            } )
        extraContentValues.put(ExtraKeys.REFUND_AMOUNT.name, inputRetAmount.toString())
        inputList.add(inputRetAmount)
    }

    /**
     * This method is for creating input reference number with respect to validator which checks if reference number is not null
     * (Refund from Gib) it compares whether reference numbers are matching else -> checks for 9 digits.
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputRefNo(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        val inputRefNo = CustomInputFormat(
            getStrings(R.string.ref_no),
            EditTextInputType.Number,
            9,
            getStrings(R.string.ref_no_invalid_ten_digits)
        ) { customInputFormat: CustomInputFormat ->
            customInputFormat.text.length == 9
        }
        extraContentValues.put(ExtraKeys.REF_NO.name, inputRefNo.toString())
        inputList.add(inputRefNo)
    }

    /**
     * This method is for creating input authorization code respect to validator which checks whether there is 6 digits or not.
     * It adds input to inputList, and puts input value extraContentValues to use it later
     */
    private fun addInputAuthCode(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        val inputAuthCode = CustomInputFormat(
            getStrings(R.string.confirmation_code),
            EditTextInputType.Number,
            6,
            getStrings(R.string.confirmation_code_invalid_six_digits)
        ) { customInputFormat: CustomInputFormat -> customInputFormat.text.length == 6 }
        extraContentValues.put(ExtraKeys.AUTH_CODE.name, inputAuthCode.toString())
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

}