package com.tokeninc.sardis.application_template

import MenuItem
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintServiceBinding
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintService
import com.tokeninc.sardis.application_template.viewmodels.PostTxnViewModel
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostTxnFragment : Fragment() {
    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!
    var card: ICCCard? = null

    private var menuFragment: ListMenuFragment? = null
    var mainActivity: MainActivity? = null
    var transactionService: TransactionService? = null
    var refundFragment: RefundFragment? = null
    private var printService = PrintServiceBinding()
    private lateinit var viewModel: PostTxnViewModel
    var transactionViewModel: TransactionViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(this)[PostTxnViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMenu()
    }

    private fun getStrings(resID: Int): String{
        return mainActivity!!.getString(resID)
    }

    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem(getStrings(R.string.void_transaction), {
            mainActivity!!.isVoid = true
            mainActivity!!.readCard()
        }))
        menuItems.add(MenuItem(getStrings(R.string.refund), {
            startRefundFragment()
            mainActivity!!.addFragment(refundFragment!!) //burada stacke ekliyor
        }))
        menuItems.add(MenuItem(getStrings(R.string.batch_close), {

        }))
        menuItems.add(MenuItem(getStrings(R.string.examples), {
            mainActivity!!.startExampleActivity()
        }))
        viewModel.list = menuItems
        viewModel.replaceFragment(mainActivity!!)
    }

    fun addFragment( fragment: Fragment)
    {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.container,fragment) //replacing fragment
            addToBackStack("PostTxnFragment")
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

    private fun startRefundFragment(){
        refundFragment!!.mainActivity = mainActivity
        transactionService!!.mainActivity = mainActivity
        transactionService!!.batchDB = mainActivity!!.batchDB
        refundFragment!!.transactionService = transactionService
    }

    fun cardNumberReceived(mCard: ICCCard?){
        card = mCard
        val cardNumber = card!!.mCardNumber
        transactionViewModel!!.cardNumber = cardNumber
        val transactionList = TransactionList()
        transactionList.postTxnFragment = this@PostTxnFragment
        transactionList.viewModel = transactionViewModel
        mainActivity!!.replaceFragment(transactionList)
        mainActivity!!.isVoid = false //it returns false again to check correctly next operations
    }

    fun voidOperation(transaction: ContentValues?){
        CoroutineScope(Dispatchers.Default).launch {
            val transactionResponse = transactionService!!.doInBackground(mainActivity!!, transaction!!.getAsString(TransactionCol.Col_Amount.name).toInt(),
                card!!,TransactionCode.VOID,
                ContentValues(),null,false,null,false)
            finishVoid(transactionResponse!!)
        }
    }

    private fun finishVoid(transactionResponse: TransactionResponse) {
        Log.d("TransactionResponse/PostTxn", transactionResponse.contentVal.toString())
        val printService = PrintService()
        val customerSlip = printService.getFormattedText( SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, ContentValues(), transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity!!,1, 1,false)
        val merchantSlip = printService.getFormattedText( SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, ContentValues(), transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity!!,1, 1,false)
        this.printService.print(customerSlip)
        this.printService.print(merchantSlip)
        mainActivity!!.finish()
    }
}