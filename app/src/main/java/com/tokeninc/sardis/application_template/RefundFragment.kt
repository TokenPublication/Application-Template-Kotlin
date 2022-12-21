package com.tokeninc.sardis.application_template

import MenuItem
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
import java.text.SimpleDateFormat
import java.util.*


class RefundFragment : Fragment() {
    private var _binding: FragmentRefundBinding? = null
    private val binding get() = _binding!!

    private var menuFragment: ListMenuFragment? = null
    private var hostFragment: InputListFragment? = null
    var mainActivity: MainActivity? = null



    companion object{
        lateinit var inputTranDate: CustomInputFormat
        lateinit var inputOrgAmount: CustomInputFormat
        lateinit var inputRetAmount: CustomInputFormat
        lateinit var inputRefNo: CustomInputFormat
        lateinit var inputAuthCode: CustomInputFormat
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

    /**
     * this function needs for getting string from activity otherwise it causes an error because we update UI in mainActivity
     */
    private fun getStrings(resID: Int): String{
        return mainActivity!!.getString(resID)
    }

    private fun prepareData() {
        menuItems.add(
            MenuItem( getStrings(R.string.matched_refund) , { menuItem: IListMenuItem? ->
                showMatchedReturnFragment()
        } ) )
        menuItems.add(
            MenuItem(getStrings(R.string.cash_refund), { menuItem: IListMenuItem? ->
            //showReturnFragment()
        } ) )
        menuItems.add(
            MenuItem(getStrings(R.string.installment_refund), { iListMenuItem ->
            showInstallments()
        }) )
        menuItems.add(
            MenuItem(getStrings(R.string.loyalty_refund), { iListMenuItem ->
            //showReturnFragment()
        } ) )
    }


    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("Eşlenikli İade", {
            showMatchedReturnFragment()
        }))
        menuItems.add(MenuItem("Peşin İade", {
            showReturnFragment()
        }))
        menuItems.add(MenuItem("Taksitli İade", {
            showInstallments()
        }))
        menuItems.add(MenuItem("Puan İade", {
            showReturnFragment()
        }))
        menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn",
            true, R.drawable.token_logo)
        mainActivity!!.replaceFragment(menuFragment as Fragment)
    }


    private fun showMatchedReturnFragment() { // EŞLENİKLİ İADE
        val inputList = mutableListOf<CustomInputFormat>()

         inputOrgAmount = CustomInputFormat(
            getStrings(R.string.original_amount),
            EditTextInputType.Amount,
            null,
            getStrings(R.string.invalid_amount),
        InputValidator { input: CustomInputFormat ->
            val amount = if (input.text.isEmpty()) 0 else input.text.toInt()
            amount > 0
        } )
        inputList.add(inputOrgAmount)

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
        inputList.add(inputRetAmount)

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
        inputList.add(inputRefNo)

        inputAuthCode = CustomInputFormat(
            getStrings(R.string.confirmation_code),
            EditTextInputType.Number,
            6,
            getStrings(R.string.confirmation_code_invalid_six_digits)
        ) { customInputFormat: CustomInputFormat -> customInputFormat.text.length == 6 }
        inputList.add(inputAuthCode)

        inputTranDate = CustomInputFormat(getStrings(R.string.tran_date),
            EditTextInputType.Date,
            null,
            getStrings(R.string.tran_date_invalid),
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
        inputList.add(inputTranDate)

        val fragment = InputListFragment.newInstance(
            inputList, getStrings(R.string.refund)
        ) { list: MutableList<String> ->
            Log.d("Refund/Eşlenikli","$list")
            data = list
            amount = list[1].toInt()
            mainActivity!!.isRefund = true
            //readCard()
            mainActivity!!.isRefund = false
        }

        mainActivity!!.addFragment(fragment)
    }

    private fun showReturnFragment(){ // İADE
        val inputList: MutableList<CustomInputFormat> = mutableListOf()
        inputList.add(CustomInputFormat(
            getStrings(R.string.refund_amount),
            EditTextInputType.Amount,
            null,
            getStrings(R.string.invalid_amount)
        ) { input: CustomInputFormat ->
            val ListAmount = if (input.text.isEmpty()) 0 else input.text.toInt()
            try {
                amount = ListAmount
            } catch (n: NumberFormatException) {
                n.printStackTrace()
            }
            ListAmount > 0
        })
        val fragment = InputListFragment.newInstance(
            inputList, getStrings(R.string.refund)
        ){ list: List<String?>? ->
            Log.d("Refund/İade","$list")
            mainActivity!!.isRefund = true
            //readCard()
            mainActivity!!.isRefund = false
        }
        mainActivity!!.addFragment(fragment)
    }

    private fun showInstallments() { // TAKSİTLİ İADE
        val listener = MenuItemClickListener { menuItem: MenuItem? ->
            installmentCount = 12
            showMatchedReturnFragment()
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


    //intenti salesa yap satışta ama burada iade de var iptal de o yüzden yemez
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