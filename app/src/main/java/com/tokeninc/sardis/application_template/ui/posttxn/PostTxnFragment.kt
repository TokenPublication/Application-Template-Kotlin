package com.tokeninc.sardis.application_template.ui.posttxn

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.data.entities.responses.BatchCloseResponse
import com.tokeninc.sardis.application_template.data.entities.responses.TransactionResponse
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding
import com.tokeninc.sardis.application_template.enums.SlipType
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.services.BatchCloseService
import com.tokeninc.sardis.application_template.services.TransactionService
import com.tokeninc.sardis.application_template.ui.MenuItem
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.posttxn.refund.RefundFragment
import com.tokeninc.sardis.application_template.ui.posttxn.void_operation.TransactionList
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the class for Post Transaction Methods.
 */
class PostTxnFragment : Fragment() {

    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!
    var card: ICCCard? = null
    private lateinit var mainActivity: MainActivity
    private lateinit var transactionService: TransactionService
    private lateinit var refundFragment: RefundFragment
    private lateinit var viewModel: PostTxnViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var batchCloseService: BatchCloseService
    private lateinit var batchViewModel: BatchViewModel

    private lateinit var cardViewModel: CardViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(this)[PostTxnViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMenu()
    }

    /**
     * This is for initializing some variables on that class, it is called from mainActivity before this class is called
     */
    fun setter(mainActivity: MainActivity, transactionViewModel: TransactionViewModel, transactionService: TransactionService,
               refundFragment: RefundFragment, batchCloseService: BatchCloseService, batchViewModel: BatchViewModel,
               cardViewModel: CardViewModel
    ){
        this.mainActivity = mainActivity
        this.transactionViewModel = transactionViewModel
        this.transactionService = transactionService
        this.refundFragment = refundFragment
        this.batchCloseService = batchCloseService
        this.batchViewModel = batchViewModel
        this.cardViewModel = cardViewModel
    }

    private fun startVoidAfterConnected(){ //TODO 0.5 sn eski arkaplan oluyor kartla yapılan işlemler gelene kadar ona bak.
        cardViewModel.setTransactionCode(TransactionCode.VOID.type)
        cardViewModel.getCardLiveData().observe(mainActivity) { card -> //firstly observing cardData
            if (card != null) { //when the cardData is not null (it is updated after onCardDataReceived)
                Log.d("CardResult", card.mCardNumber.toString())
                cardNumberReceived(card) // start this operation with the card data
                cardViewModel.resetCard() // make it clear for the next operations
            }
        }
    }

    override fun onDestroyView() {
        Log.d("PostTxnDestroying","HEY")
        super.onDestroyView()
    }
    /**
     * This function prepares the Post Transactions Menu that contains Void, Refund, Batch Close, Examples and Slip Repetition
     */
    private fun showMenu(){
        val menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem(getStrings(R.string.void_transaction), {
            mainActivity.connectCardService()
            startVoidAfterConnected()
        }))
        menuItems.add(MenuItem(getStrings(R.string.refund), {
            startRefundFragment()
            mainActivity.addFragment(refundFragment) //burada stacke ekliyor
        }))
        menuItems.add(MenuItem(getStrings(R.string.batch_close), {
            if (transactionViewModel.allTransactions() == null){
                val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Warning,getStrings(
                    R.string.batch_empty
                ),false)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog!!.dismiss()
                }, 2000)
            }
            else{
                mainActivity.showConfirmationDialog(InfoDialog.InfoType.Info,getStrings(R.string.batch_close),getStrings(
                    R.string.batch_close_implementing
                ),InfoDialog.InfoDialogButtons.Both,1,
                    object : InfoDialogListener {
                        override fun confirmed(i: Int) {
                            startBatchClose()
                        }
                        override fun canceled(i: Int) {}
                    })
            }
        }))
        menuItems.add(MenuItem(getStrings(R.string.examples), {
            mainActivity.startExampleActivity()
        }))
        menuItems.add(MenuItem("Slip Tekrarı",{
            batchViewModel.getPreviousBatchSlip().observe(mainActivity){
                mainActivity.print(it)
            }
        }))
        viewModel.list = menuItems
        viewModel.replaceFragment(mainActivity)
    }

    fun addFragment(fragment: Fragment)
    {
        val ft: FragmentTransaction = parentFragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    /**
     * It starts batch close with batch close service which runs parallel with Coroutine
     */
    fun startBatchClose() {
        CoroutineScope(Dispatchers.Default).launch {
            val batchResponse = batchCloseService.doInBackground()
            finishBatchClose(batchResponse!!)
        }
    }

    /**
     * It finishes the batch close operation with passing the response code as a result to mainActivity and finishing mainActivity.
     */
    private fun finishBatchClose(batchCloseResponse: BatchCloseResponse){
        Log.d("finishBatch","${batchCloseResponse.batchResult}")
        val responseCode = batchCloseResponse.batchResult
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)
        mainActivity.setResult(intent)
    }

    /**
     * It starts the refund fragment with initializing variables.
     */
    fun startRefundFragment(){
        refundFragment.setter(mainActivity,transactionService, cardViewModel,transactionViewModel,batchViewModel)
    }

    /** After reading a card, this function is called only for Void operations.
     * If there was no operation with that card, it warns the user. Else ->
     * It shows transactions that has been operated with that card with recyclerview.
     */
    fun cardNumberReceived(mCard: ICCCard?){
        cardViewModel.setTransactionCode(0)
        if (transactionViewModel.getTransactionsByCardNo(mCard!!.mCardNumber.toString()) == null){
            val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Warning,getStrings(R.string.batch_empty),false)
            Handler(Looper.getMainLooper()).postDelayed({
                infoDialog!!.dismiss()
            }, 2000)
        }
        else{
            card = mCard
            val cardNumber = card!!.mCardNumber
            //transactionViewModel.cardNumber = cardNumber
            val transactionList = TransactionList(cardNumber)
            transactionList.postTxnFragment = this@PostTxnFragment
            transactionList.viewModel = transactionViewModel
            mainActivity.addFragment(transactionList)
        }
    }

    /**
     * It starts void operation in parallel
     */
    fun voidOperation(transaction: Transaction){
        val contentValHelper = ContentValHelper()
        CoroutineScope(Dispatchers.Default).launch {
            //finishVoid(transactionResponse!!)
            transactionViewModel.transactionRoutine(transaction.Col_Amount,
                card!!,TransactionCode.VOID.type,
                contentValHelper.getContentVal(transaction),null,false,null,false,batchViewModel,
                mainActivity.currentMID, mainActivity.currentTID,mainActivity)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        transactionViewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is TransactionViewModel.UIState.Loading -> mainActivity.showDialog(dialog)
                is TransactionViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress,"Connecting ${state.data}")
                is TransactionViewModel.UIState.Success -> mainActivity.showDialog(InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Printing Slip",true))
            }
        }
        transactionViewModel.getLiveIntent().observe(mainActivity){liveIntent ->
            mainActivity.setResult(liveIntent)
        }
    }

    /**
     * It finishes the void operation via printing slip with respect to achieved data and
     * passes the response code as a result to mainActivity and finishes void transaction.
     */
    private fun finishVoid(transactionResponse: TransactionResponse) {
        Log.d("TransactionResponse/PostTxn", "responseCode:${transactionResponse.responseCode} ContentVals: ${transactionResponse.contentVal}")
        val printService = PrintService()
        val customerSlip = printService.getFormattedText(SlipType.CARDHOLDER_SLIP,transactionResponse.contentVal!!, transactionResponse.extraContent, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        val merchantSlip = printService.getFormattedText(SlipType.MERCHANT_SLIP,transactionResponse.contentVal!!, transactionResponse.extraContent, transactionResponse.onlineTransactionResponse, transactionResponse.transactionCode, mainActivity,1, 1,false)
        mainActivity.print(customerSlip)
        mainActivity.print(merchantSlip)
        val responseCode = transactionResponse.responseCode
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)
        mainActivity.setResult(intent)
    }

    /**
     * Fragment couldn't use getString from res > values > strings, therefore this method call that string from mainActivity.
     */
    private fun getStrings(resID: Int): String{
        return mainActivity.getString(resID)
    }
}
