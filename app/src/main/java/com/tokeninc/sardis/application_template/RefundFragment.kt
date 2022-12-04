package com.tokeninc.sardis.application_template

import MenuItem
import android.os.Bundle
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
import java.text.SimpleDateFormat
import java.util.*


class RefundFragment : Fragment() {
    private var _binding: FragmentRefundBinding? = null
    private val binding get() = _binding!!

    private var menuFragment: ListMenuFragment? = null
    private var hostFragment: InputListFragment? = null


    companion object{
        private var inputTranDate: CustomInputFormat? = null
        private var inputOrgAmount: CustomInputFormat? = null
        private var inputRetAmount: CustomInputFormat? = null
        private var inputRefNo: CustomInputFormat? = null
        private var inputAuthCode: CustomInputFormat? = null
        private var menuItems = mutableListOf<IListMenuItem>()
        private var amount = 0
        private var installmentCount = 0
        private var data = mutableListOf<String>()
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
        prepareData()
        showMenu()
    }

    private fun prepareData() {
        menuItems.add(
            MenuItem( getString(R.string.matched_refund) , { menuItem: IListMenuItem? ->
                showMatchedReturnFragment()
        } ) )
        menuItems.add(
            MenuItem(getString(R.string.cash_refund), { menuItem: IListMenuItem? ->
            //showReturnFragment()
        } ) )
        menuItems.add(
            MenuItem(getString(R.string.installment_refund), { iListMenuItem ->
            showInstallments()
        }) )
        menuItems.add(
            MenuItem(getString(R.string.loyalty_refund), { iListMenuItem ->
            //showReturnFragment()
        } ) )
    }


    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("Eşlenikli İade", {
            showMatchedReturnFragment()
        }))
        menuItems.add(MenuItem("Peşin İade", {

        }))
        menuItems.add(MenuItem("Taksitli İade", {
            showInstallments()
        }))
        menuItems.add(MenuItem("Puan İade", {

        }))
        menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn",
            true, R.drawable.token_logo)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, menuFragment!!)
            commit()
        }
    }


    private fun showMatchedReturnFragment() { // EŞLENİKLİ İADE
        val inputList = mutableListOf<CustomInputFormat>()

         inputOrgAmount = com.token.uicomponents.CustomInput.CustomInputFormat(
            getString(R.string.original_amount),
            EditTextInputType.Amount,
            null,
            getString(R.string.invalid_amount),
        InputValidator { input: CustomInputFormat ->
            val amount = if (input.text.isEmpty()) 0 else input.text.toInt()
            amount > 0
        } )
        inputList.add(inputOrgAmount!!)

        inputRetAmount = CustomInputFormat(
            getString(R.string.refund_amount),
            EditTextInputType.Amount,
            null,
            getString(R.string.invalid_amount)
        ,InputValidator {
            val amount = if (it.text.isEmpty()) 0 else it.text.toInt()
            val original =
                if (inputOrgAmount!!.text.isEmpty()) 0 else inputOrgAmount!!.text.toInt()
            amount > 0 && amount <= original
        } )
        inputList.add(inputRetAmount!!)

        inputRefNo = CustomInputFormat(
            getString(R.string.ref_no),
            EditTextInputType.Number,
            10,
            getString(R.string.ref_no_invalid_ten_digits)
        ) { customInputFormat: CustomInputFormat ->
            !isCurrentDay(inputTranDate!!.text) || isCurrentDay(
                inputTranDate!!.text
            ) && customInputFormat.text.length == 10
        }
        inputList.add(inputRefNo!!)

        inputAuthCode = CustomInputFormat(
            getString(R.string.confirmation_code),
            EditTextInputType.Number,
            6,
            getString(R.string.confirmation_code_invalid_six_digits)
        ) { customInputFormat: CustomInputFormat -> customInputFormat.text.length == 6 }
        inputList.add(inputAuthCode!!)

        inputTranDate = CustomInputFormat(getString(R.string.tran_date),
            EditTextInputType.Date,
            null,
            getString(R.string.tran_date_invalid),
            label@
            InputValidator { customInputFormat: CustomInputFormat ->
                try {
                    val array =
                        customInputFormat.text.split("/").toTypedArray()
                    val date = array[2].substring(2) + array[1] + array[0]
                    val now = Calendar.getInstance().time
                    val sdf = SimpleDateFormat("yyMMdd")
                    //doğru mu bak
                    return@InputValidator sdf.format(now).toInt() >= date.toInt()
                } catch (e: Exception) {
                }
                false
            }
        )
        inputList.add(inputTranDate!!)

        val fragment = InputListFragment.newInstance(
            inputList, getString(R.string.refund)
        ) { list: MutableList<String> ->
            data = list
            amount = list[1].toInt()
            //readCard()
        }

        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id,fragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun showInstallments() { // TAKSİTLİ İADE
        val listener = MenuItemClickListener { menuItem: MenuItem? ->
            installmentCount = 12
            showMatchedReturnFragment()
        }
        val maxInst = 12
        val menuItems = mutableListOf<IListMenuItem>()
        for (i in 2..maxInst) {
            val menuItem = MenuItem(i.toString() + " " + getString(R.string.installment), listener)
            menuItems.add(menuItem)
        }
        instFragment = ListMenuFragment.newInstance(
            menuItems,
            getString(R.string.installment_refund),
            true,
            R.drawable.token_logo)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, instFragment!!)
            addToBackStack(null)
            commit()
        }
    }

    /*
    fun showReturnFragment() { // İADE
    }
     */

    /* Card Service binding var
    private fun readCard() {
        try {
            val obj = JSONObject()
            obj.put("forceOnline", 1)
            obj.put("zeroAmount", 0)
            obj.put("fallback", 1)
            cardServiceBinding.getCard(amount, 40, obj.toString())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private void takeOutICC() {
        cardServiceBinding.takeOutICC(40);
    }
    private void showInfoDialog()
    private void finishRefund(ResponseCode code)

     */

/*
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

 */


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