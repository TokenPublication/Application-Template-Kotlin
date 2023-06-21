package com.tokeninc.sardis.application_template.ui.posttxn.refund

import com.tokeninc.sardis.application_template.ui.MenuItem
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
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
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentRefundBinding
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintServiceBinding
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintService
import com.tokeninc.sardis.application_template.data.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.services.TransactionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * This is the fragment for the Refund actions. Refund operation methods are defined here.
 */
class RefundFragment : Fragment() {
    private var _binding: FragmentRefundBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivity: MainActivity
    private lateinit var transactionService: TransactionService

    private lateinit var card: ICCCard
    private var extraContent = ContentValues()  //at the end of every Refund we finish mainActivity so no need to delete it at everytime
    private var printService = PrintServiceBinding()
    private var stringExtraContent = ContentValues() //this is for switching customInput format type to string
    var amount = 0 //TODO sonra 0 la ?
    var refNo : String? = null

    companion object{
        lateinit var inputTranDate: CustomInputFormat
        lateinit var inputOrgAmount: CustomInputFormat
        lateinit var inputRetAmount: CustomInputFormat
        lateinit var inputRefNo: CustomInputFormat
        lateinit var inputAuthCode: CustomInputFormat
        private var installmentCount = 0
        private var instFragment: ListMenuFragment? = null
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
     * After reading card, refund transaction will be added Transaction table with this function in parallel.
     */
    fun afterReadCard(mCard: ICCCard?, transactionCode: Int, mExtraContent: ContentValues?){
        if (mExtraContent != null){
            stringExtraContent = mExtraContent
        }
        mainActivity.transactionCode = 0
        card = mCard!!
        CoroutineScope(Dispatchers.Default).launch {
            val transactionResponse = transactionService.doInBackground(mainActivity,stringExtraContent.getAsString(ExtraKeys.ORG_AMOUNT.name).toInt()
                    ,card, transactionCode, stringExtraContent,null,false,null,false)
            finishRefund(transactionResponse!!)
        }
    }

    /**
     * It passes the response code as a result to mainActivity and finishes the refund via printing slip.
     */
    private fun finishRefund(transactionResponse: TransactionResponse){
        Log.d("TransactionResponse/Refund", "responseCode:${transactionResponse.responseCode} ContentVals: ${transactionResponse.contentVal}")
        val printHelper = PrintService()
        val customerSlip = printHelper.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!,transactionResponse.extraContent!!, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        val merchantSlip = printHelper.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!,transactionResponse.extraContent!!, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        printService.print(customerSlip)
        printService.print(merchantSlip)
        val responseCode = transactionResponse.responseCode
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)
        mainActivity.setResult(intent)
    }

    /**
     * It prepares list menu item and shows it to the screen.
     */
    private fun showMenu(){ //recycler view gibi buna eklenebilir belki ? viewmodel
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
        mainActivity.transactionCode = TransactionCode.INSTALLMENT_REFUND.type
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputTranDate(inputList,extraContent)
        stringExtraContent.put(ExtraKeys.INST_COUNT.name, installmentCount)
        addFragment(inputList,getStrings(R.string.installment_refund))
    }

    /**
     * It shows Matched Refund with preparing corresponding inputs similar to Installment Refund
     * The only difference is it contains Reference Number and Authorization Code.
     */
    private fun showMatchedReturnFragment() { // EŞLENİKLİ İADE
        mainActivity.transactionCode = TransactionCode.MATCHED_REFUND.type
        val inputList = mutableListOf<CustomInputFormat>()
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputRefNo(inputList,extraContent)
        addInputAuthCode(inputList,extraContent)
        addInputTranDate(inputList,extraContent)
        addFragment(inputList,getStrings(R.string.matched_refund))
    }

    /**
     * It is similar to other fragment, it only contains original amount.
     */
    private fun showReturnFragment(){ // İADE
        mainActivity.transactionCode = TransactionCode.CASH_REFUND.type
        val inputList: MutableList<CustomInputFormat> = mutableListOf()
        addInputAmount(inputList,extraContent)
        addFragment(inputList,getStrings(R.string.cash_refund))
    }

    /**
     * It prepares a menu that contains 2 to 12 installment, Installment count updating with respect to clicked item
     */
    private fun showInstallments() { // TAKSİTLİ İADE
        val listener = MenuItemClickListener { menuItem: MenuItem? ->
            val itemName = menuItem!!.name.toString().split(" ")
            installmentCount = itemName[0].toInt()
            showInstallmentRefundFragment()
        }
        val maxInst = 12
        val menuItems = mutableListOf<IListMenuItem>()
        for (i in 2..maxInst) {
            val menuItem = MenuItem(i.toString() + " " + getStrings(R.string.installment), listener)
            menuItems.add(menuItem)
        }
        instFragment = ListMenuFragment.newInstance(
            menuItems,
            getStrings(R.string.installment_refund),
            true,
            R.drawable.token_logo
        )
       mainActivity.addFragment(instFragment as Fragment)
    }

    /**
     * This is for bank application, not used for now.
     */
    private fun showLoyaltyRefundFragment(){
        val inputList: MutableList<CustomInputFormat> = mutableListOf()
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputTranDate(inputList,extraContent)
        addInputRefNo(inputList,extraContent)
        addFragment(inputList,getStrings(R.string.loyalty_refund))
    }

    /** It adds values to stringExtraContent to use it later. Then calls readCard operation.
     *
     */
    private fun addFragment(inputList: MutableList<CustomInputFormat>, refundType: String){
        val fragment = InputListFragment.newInstance(
            inputList, getStrings(R.string.refund)
        ){ list: List<String?>? ->
            when (mainActivity.transactionCode) {
                TransactionCode.MATCHED_REFUND.type -> {
                    stringExtraContent.put(ExtraKeys.ORG_AMOUNT.name,list!![0])
                    stringExtraContent.put(ExtraKeys.REFUND_AMOUNT.name,list[1])
                    stringExtraContent.put(ExtraKeys.REF_NO.name,list[2])
                    stringExtraContent.put(ExtraKeys.AUTH_CODE.name,list[3])
                    stringExtraContent.put(ExtraKeys.TRAN_DATE.name,list[4])
                }
                TransactionCode.INSTALLMENT_REFUND.type -> {
                    stringExtraContent.put(ExtraKeys.ORG_AMOUNT.name,list!![0])
                    stringExtraContent.put(ExtraKeys.REFUND_AMOUNT.name,list[1])
                    stringExtraContent.put(ExtraKeys.TRAN_DATE.name,list[2])
                }
                else -> {
                    stringExtraContent.put(ExtraKeys.ORG_AMOUNT.name, list!![0])
                }
            }
            if (mainActivity.transactionCode == TransactionCode.CASH_REFUND.type){
                mainActivity.amount = stringExtraContent.getAsString(ExtraKeys.ORG_AMOUNT.name).toInt()
            } else{
                mainActivity.amount = stringExtraContent.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt()
            }
            mainActivity.readCard()
        }
        mainActivity.addFragment(fragment)
    }

    /**
     * This method is for creating input amount with respect to validator.
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
     * This method is for creating input return amount with respect to validator which checks whether that amount smaller than
     * original amount or not.
     */
    private fun addInputRetAmount(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        inputRetAmount = CustomInputFormat(
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
     */
    private fun addInputRefNo(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        inputRefNo = CustomInputFormat(
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
     */
    private fun addInputAuthCode(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        inputAuthCode = CustomInputFormat(
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
     */
    @SuppressLint("SimpleDateFormat")
    private fun addInputTranDate(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        inputTranDate = CustomInputFormat(getStrings(R.string.tran_date),
            EditTextInputType.Date,
            null,
            getStrings(R.string.tran_date_invalid),
            InputValidator { customInputFormat: CustomInputFormat ->
                try {
                    val array = customInputFormat.text.split("/").toTypedArray()
                    val date = array[2].substring(2) + array[1] + array[0]
                    val now = Calendar.getInstance().time
                    val sdf = SimpleDateFormat("yyMMdd")
                    //doğru mu bak
                    return@InputValidator sdf.format(now).toInt() >= date.toInt()
                } catch (_: Exception) {
                }
                false
            }
        )
        inputList.add(inputTranDate)
    }

    private fun isCurrentDay(dateText: String): Boolean {
        if (dateText.isEmpty())
            return false
        val date = getFormattedDate(dateText)
        val sdf = SimpleDateFormat("ddMMyy")
        return sdf.format(Calendar.getInstance().time) == date
    }

    private fun getFormattedDate(dateText: String): String {
        val lst = dateText.split("/").toMutableList()
        return lst[0] + lst[1] + lst[2].substring(2)
    }

    /**
     * This is for initializing some variables on that class, it is called from PostTxn
     */
    fun setter(mainActivity: MainActivity, transactionService: TransactionService){
        this.mainActivity = mainActivity
        this.transactionService = transactionService
    }

}