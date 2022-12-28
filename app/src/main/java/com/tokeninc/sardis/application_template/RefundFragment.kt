package com.tokeninc.sardis.application_template

import MenuItem
import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.CustomInput.InputValidator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.ListMenuFragment.MenuItemClickListener
import com.tokeninc.sardis.application_template.databinding.FragmentRefundBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.ExtraKeys
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintServiceBinding
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class RefundFragment : Fragment() {
    private var _binding: FragmentRefundBinding? = null
    private val binding get() = _binding!!

    private var menuFragment: ListMenuFragment? = null
    var mainActivity: MainActivity? = null
    lateinit var card: ICCCard
    var transactionService: TransactionService? = null
    private var extraContent = ContentValues()  //at the end of every Refund we finish mainActivity so no need to delete it at everytime
    private var printService = PrintServiceBinding()
    private var stringExtraContent = ContentValues() //this is for switching customInput format type to string


    companion object{
        lateinit var inputTranDate: CustomInputFormat
        lateinit var inputOrgAmount: CustomInputFormat
        lateinit var inputRetAmount: CustomInputFormat
        lateinit var inputRefNo: CustomInputFormat
        lateinit var inputAuthCode: CustomInputFormat
        private var installmentCount = 0
        private var instFragment: ListMenuFragment? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRefundBinding.inflate(inflater,container,false)
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
        return mainActivity!!.getString(resID)
    }

    fun afterReadCard(mCard: ICCCard?, isMatched: Boolean, isInstallment: Boolean, isCash: Boolean){
        card = mCard!!
        mainActivity!!.isRefund = false
        val transactionCode = transactionCode(isMatched,isInstallment,isCash)
        CoroutineScope(Dispatchers.Default).launch {
            val transactionResponse = transactionService!!.doInBackground(mainActivity!!,stringExtraContent.getAsString(ExtraKeys.ORG_AMOUNT.name).toInt()
                    ,card, transactionCode,stringExtraContent,null,false,null,false)
            finishRefund(transactionResponse)
        }
    }

    private fun transactionCode(isMatched: Boolean, isInstallment: Boolean, isCash: Boolean):TransactionCode{
        if(isMatched)
            return TransactionCode.MATCHED_REFUND
        if(isInstallment)
            return TransactionCode.INSTALLMENT_REFUND
        else
            return TransactionCode.CASH_REFUND
    }

    private fun finishRefund(transactionResponse: TransactionResponse?){
        Log.d("TransactionResponse/Refund", transactionResponse!!.contentVal.toString())
        val printHelper = PrintService()
        val customerSlip = printHelper.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!,transactionResponse.extraContent!!, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity!!,1, 1,false)
        val merchantSlip = printHelper.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!,transactionResponse.extraContent!!, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity!!,1, 1,false)
        printService.print(customerSlip)
        printService.print(merchantSlip)
        mainActivity!!.finish()
    }

    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
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
        menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn",
            true, R.drawable.token_logo)
        mainActivity!!.replaceFragment(menuFragment as Fragment)
    }


    private fun showInstallmentRefundFragment(){
        val inputList = mutableListOf<CustomInputFormat>()
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputTranDate(inputList,extraContent)
        addFragment(inputList,getStrings(R.string.installment_refund))
    }

    private fun showMatchedReturnFragment() { // EŞLENİKLİ İADE
        val inputList = mutableListOf<CustomInputFormat>()
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputRefNo(inputList,extraContent)
        addInputAuthCode(inputList,extraContent)
        addInputTranDate(inputList,extraContent)
        addFragment(inputList,getStrings(R.string.matched_refund))
    }

    private fun showReturnFragment(){ // İADE
        val inputList: MutableList<CustomInputFormat> = mutableListOf()
        addInputAmount(inputList,extraContent)
        addFragment(inputList,getStrings(R.string.cash_refund))
    }

    private fun showInstallments() { // TAKSİTLİ İADE
        val listener = MenuItemClickListener { menuItem: MenuItem? ->
            installmentCount = 12
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
            R.drawable.token_logo)
       mainActivity!!.addFragment(instFragment as Fragment)
    }

    private fun showLoyaltyRefundFragment(){
        val inputList: MutableList<CustomInputFormat> = mutableListOf()
        addInputAmount(inputList,extraContent)
        addInputRetAmount(inputList,extraContent)
        addInputTranDate(inputList,extraContent)
        addInputRefNo(inputList,extraContent)
        addFragment(inputList,getStrings(R.string.loyalty_refund))
    }

    /**
     * Adds fragment with respect to inputlist and refund Type
     */
    private fun addFragment(inputList: MutableList<CustomInputFormat>, refundType: String){
        val fragment = InputListFragment.newInstance(
            inputList, getStrings(R.string.refund)
        ){ list: List<String?>? ->
            //TODO Eşlenikli için şimdilik sadece, hepsine uygun dinamik yap
            stringExtraContent.put(ExtraKeys.ORG_AMOUNT.name,list!![0])
            stringExtraContent.put(ExtraKeys.REFUND_AMOUNT.name,list[1])
            stringExtraContent.put(ExtraKeys.REF_NO.name,list[2])
            stringExtraContent.put(ExtraKeys.AUTH_CODE.name,list[3])
            stringExtraContent.put(ExtraKeys.TRAN_DATE.name,list[4])
            mainActivity!!.isRefund = true
            if (refundType == getStrings(R.string.matched_refund))
                mainActivity!!.isMatchedRefund = true
            if (refundType == getStrings(R.string.cash_refund))
                mainActivity!!.isCashRefund = true
            if (refundType == getStrings(R.string.installment_refund))
                mainActivity!!.isInstallmentRefund = true
            mainActivity!!.amount = stringExtraContent.getAsString(ExtraKeys.REFUND_AMOUNT.name).toInt()
            mainActivity!!.readCard()
        }
        mainActivity!!.addFragment(fragment)
    }

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

    private fun addInputRetAmount(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        inputRetAmount = CustomInputFormat(
            getStrings(R.string.refund_amount),
            EditTextInputType.Amount,
            null,
            getStrings(R.string.invalid_amount),
            InputValidator {
                val amount = if (it.text.isEmpty()) 0 else it.text.toInt()
                val original =
                    if (inputOrgAmount.text.isEmpty()) 0 else inputOrgAmount.text.toInt()
                amount in 1..original
            } )
        extraContentValues.put(ExtraKeys.REFUND_AMOUNT.name, inputRetAmount.toString())
        inputList.add(inputRetAmount)
    }

    private fun addInputRefNo(inputList: MutableList<CustomInputFormat>,extraContentValues: ContentValues){
        inputRefNo = CustomInputFormat(
            getStrings(R.string.ref_no),
            EditTextInputType.Number,
            10,
            getStrings(R.string.ref_no_invalid_ten_digits)
        ) { customInputFormat: CustomInputFormat ->
            !isCurrentDay(inputTranDate.text) || isCurrentDay(
                inputTranDate.text
            ) && customInputFormat.text.length == 10
        }
        extraContentValues.put(ExtraKeys.REF_NO.name, inputRefNo.toString())
        inputList.add(inputRefNo)
    }

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

}