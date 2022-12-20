package com.tokeninc.sardis.application_template

import MenuItem
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.*
import com.tokeninc.sardis.application_template.helpers.StringHelper
import com.tokeninc.sardis.application_template.helpers.printHelpers.SalePrintHelper
import com.tokeninc.sardis.application_template.viewmodel.TransactionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class PostTxnFragment : Fragment() {
    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!
    var card: ICCCard? = null

    private var menuFragment: ListMenuFragment? = null
    var mainActivity: MainActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMenu()
    }


    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("İptal", {
            mainActivity!!.isVoid = true
            mainActivity!!.readCard()
            //mainActivity!!.startVoidFragment(VoidFragment())
            // TODO ReadCard
        }))
        menuItems.add(MenuItem("İade", {
            val refundFragment = RefundFragment()
            refundFragment.mainActivity = mainActivity
            mainActivity!!.replaceFragment(refundFragment)
        }))
        menuItems.add(MenuItem("Grup Kapama", {

        }))
        menuItems.add(MenuItem("Örnekler", {

        }))
        menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn",
            true, R.drawable.token_logo)
        mainActivity!!.replaceFragment(menuFragment as Fragment)
    }

    fun cardNumberReceived(mCard: ICCCard?){
        card = mCard
        val cardNumber = card!!.mCardNumber
        val transactionDB = mainActivity!!.transactionDB
        val transactionVM = TransactionViewModel(cardNumber)
        transactionVM.transactionDB = transactionDB
        val transactionList = TransactionList()
        transactionList.postTxnFragment = this@PostTxnFragment
        transactionList.viewModel = transactionVM
        mainActivity!!.replaceFragment(transactionList)
        mainActivity!!.isVoid = false //it returns false again to check correctly next operations
    }

    fun voidOperation(transaction: ContentValues?){
        val transactionService = mainActivity!!.transactionService
        transactionService.mainActivity = mainActivity //TODO buralar çok çirkin oldu
        transactionService.transactionDB = mainActivity!!.transactionDB
        CoroutineScope(Dispatchers.Default).launch {
            val transactionResponse = transactionService.doInBackground(mainActivity!!, transaction!!.getAsString(TransactionCol.Col_Amount.name).toInt(),
                card!!,TransactionCode.VOID,null,null,false,null,false)
            finishSale(transactionResponse!!)
        }
    }

    private fun finishSale(transactionResponse: TransactionResponse){
        Log.d("TransactionResponse/PostTxn","${transactionResponse.contentVal.toString()}")

        /**  //TODO BARIS
        val responseCode = transactionResponse.responseCode
        getNotNullBundle().putInt("ResponseCode", responseCode.ordinal)
        getNotNullBundle().putInt("PaymentStatus", 0) // #2 Payment Status
        getNotNullBundle().putInt("Amount", DummySaleFragment.amount) // #3 Amount
        getNotNullBundle().putInt("Amount2", DummySaleFragment.amount)
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
        */
    }



}