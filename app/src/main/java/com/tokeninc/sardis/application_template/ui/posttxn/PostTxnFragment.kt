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
import androidx.lifecycle.ViewModelProvider
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding
import com.tokeninc.sardis.application_template.enums.TransactionCode
import com.tokeninc.sardis.application_template.ui.MenuItem
import com.tokeninc.sardis.application_template.ui.examples.ExampleActivity
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.posttxn.refund.RefundFragment
import com.tokeninc.sardis.application_template.ui.posttxn.void_operation.TransactionList
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the class for Post Transaction Methods.
 */
class PostTxnFragment(private val mainActivity: MainActivity, private val transactionViewModel: TransactionViewModel,
                      private val refundFragment: RefundFragment, private val batchViewModel: BatchViewModel,
                      private val cardViewModel: CardViewModel) : Fragment() {

    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!
    var card: ICCCard? = null

    private lateinit var viewModel: PostTxnViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(this)[PostTxnViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMenu()
    }

    private fun startVoidAfterConnected(){ //TODO 0.5 sn eski arkaplan oluyor kartla yapılan işlemler gelene kadar ona bak.
        cardViewModel.setTransactionCode(TransactionCode.VOID.type)
        cardViewModel.getCardLiveData().observe(mainActivity) { card -> //firstly observing cardData
            if (card != null) { //when the cardData is not null (it is updated after onCardDataReceived)
                Log.d("CardResult", card.mCardNumber.toString())
                voidAfterCardRead(card) // start this operation with the card data
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
            cardViewModel.setTransactionCode(TransactionCode.VOID.type)
            mainActivity.connectCardService()
            startVoidAfterConnected()
        }))
        menuItems.add(MenuItem(getStrings(R.string.refund), {
            mainActivity.addFragment(refundFragment) //burada stacke ekliyor
        }))
        menuItems.add(MenuItem(getStrings(R.string.batch_close), {
            if (transactionViewModel.allTransactions().isNullOrEmpty()){
                val infoDialog = mainActivity.showInfoDialog(
                    InfoDialog.InfoType.Warning,getStrings(R.string.batch_empty),false)
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
            mainActivity.startActivity(Intent(mainActivity, ExampleActivity()::class.java))

        }))
        menuItems.add(MenuItem("Slip Tekrarı",{
            batchViewModel.getPreviousBatchSlip().observe(mainActivity){
                mainActivity.print(it)
            }
        }))
        viewModel.list = menuItems
        viewModel.replaceFragment(mainActivity)
    }

    /**
     * It starts batch close with batch close service which runs parallel with Coroutine
     */
    fun startBatchClose() {
        CoroutineScope(Dispatchers.Default).launch {
            batchViewModel.batchRoutine(mainActivity,transactionViewModel)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        batchViewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is BatchViewModel.UIState.Loading -> mainActivity.showDialog(dialog)
                is BatchViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress,"Slip Hazırlanıyor % ${state.data}")
                is BatchViewModel.UIState.Success -> mainActivity.showDialog(InfoDialog.newInstance(InfoDialog.InfoType.Confirmed,"Grup Kapama Başarılı",true))
            }
        }
        batchViewModel.getLiveIntent().observe(mainActivity){liveIntent ->
            mainActivity.setResult(liveIntent)
        }
    }

    /** After reading a card, this function is called only for Void operations.
     * If there was no operation with that card, it warns the user. Else ->
     * It shows transactions that has been operated with that card with recyclerview.
     */
    private fun voidAfterCardRead(mCard: ICCCard?){
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
            transactionViewModel.transactionRoutine(transaction.Col_Amount,
                card!!,TransactionCode.VOID.type,
                contentValHelper.getContentVal(transaction),null,false,null,false,batchViewModel,
                mainActivity.currentMID, mainActivity.currentTID,mainActivity)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        transactionViewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is TransactionViewModel.UIState.Loading -> mainActivity.showDialog(dialog)
                is TransactionViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress,"Connecting % ${state.data}")
                is TransactionViewModel.UIState.Success -> mainActivity.showDialog(InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Printing Slip",true))
            }
        }
        transactionViewModel.getLiveIntent().observe(mainActivity){liveIntent ->
            mainActivity.setResult(liveIntent)
        }
    }

    /**
     * Fragment couldn't use getString from res > values > strings, therefore this method call that string from mainActivity.
     */
    private fun getStrings(resID: Int): String{
        return mainActivity.getString(resID)
    }
}
