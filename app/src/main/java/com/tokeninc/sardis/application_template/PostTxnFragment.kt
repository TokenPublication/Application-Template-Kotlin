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


class PostTxnFragment : Fragment(), CardServiceListener {
    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!
    private var card: ICCCard? = null

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
            readCard()
            //mainActivity!!.startVoidFragment(VoidFragment())
            // TODO ReadCard
        }))
        menuItems.add(MenuItem("İade", {
            addFragment(RefundFragment())
        }))
        menuItems.add(MenuItem("Grup Kapama", {

        }))
        menuItems.add(MenuItem("Örnekler", {

        }))
        menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn",
            true, R.drawable.token_logo)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, menuFragment!!)
            commit()
        }
    }

    fun addFragment(fragment: Fragment){
        parentFragmentManager.beginTransaction().apply { //parent fragment manager instead support since it's a fragment
            replace(binding.container.id,fragment) //replacing fragment
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

    private fun cardNumberReceived(cardNumber: String?){
        val transactionDB = mainActivity!!.transactionDB
        val transactionVM = TransactionViewModel(cardNumber)
        transactionVM.transactionDB = transactionDB
        val transactionList = TransactionList()
        transactionList.postTxnFragment = this@PostTxnFragment
        transactionList.viewModel = transactionVM
        mainActivity!!.replaceFragment(transactionList)
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


    private fun readCard() {
        val obj = JSONObject()
        try {
            obj.put("forceOnline", 0)
            obj.put("zeroAmount", 1)
            obj.put("showAmount", 0)
            obj.put("partialEMV", 1)
            // TODO Developer: Check from Allowed Operations Parameter
            val isManEntryAllowed = true
            val isCVVAskedOnMoto = true
            val isFallbackAllowed = true
            val isQrAllowed = true
            obj.put("keyIn", if (isManEntryAllowed) 1 else 0)
            obj.put("askCVV", if (isCVVAskedOnMoto) 1 else 0)
            obj.put("fallback", if (isFallbackAllowed) 1 else 0)
            obj.put("qrPay", if (isQrAllowed) 1 else 0)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val cardServiceBinding = CardServiceBinding(mainActivity !! as AppCompatActivity?,this )
        cardServiceBinding.getCard(0, 30, obj.toString())
    }

    override fun onCardServiceConnected() {

    }

    override fun onCardDataReceived(cardData: String?) {
        try {
            val json = JSONObject(cardData)
            val type = json.getInt("mCardReadType")
            card = Gson().fromJson(cardData, ICCCard::class.java)
            if (card!!.resultCode == CardServiceResult.ERROR.resultCode()) {
            }
            if (card!!.resultCode == CardServiceResult.SUCCESS.resultCode()) {
                if (type == CardReadType.QrPay.type) {
                    //QrSale()
                    return
                }
                if (type == CardReadType.CLCard.type) {
                    DummySaleFragment.cardReadType = CardReadType.CLCard.type
                    card = Gson().fromJson(cardData, ICCCard::class.java)
                } else if (type == CardReadType.ICC.type) {
                    card = Gson().fromJson(cardData, ICCCard::class.java)
                } else if (type == CardReadType.ICC2MSR.type || type == CardReadType.MSR.type || type == CardReadType.KeyIn.type) {
                    //card = Gson().fromJson(cardData, ICCCard::class.java)
                    //cardServiceBinding!!.getOnlinePIN(amount, card?.cardNumber, 0x0A01, 0, 4, 8, 30)
                }
                cardNumberReceived(card!!.mCardNumber)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onPinReceived(p0: String?) {

    }

    override fun onICCTakeOut() {

    }

}