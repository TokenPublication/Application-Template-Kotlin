package com.tokeninc.sardis.application_template.ui.postTxn.void_operation

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.model.card.CardServiceResult
import com.tokeninc.sardis.application_template.data.model.card.ICCCard
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.databinding.FragmentVoidBinding
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the Fragment that holds all the Void transactions in recyclerView, transactions one by one are set in TransactionAdapter
 */
class VoidFragment(private val mainActivity: MainActivity, private val transactionViewModel: TransactionViewModel,
                   private val batchViewModel: BatchViewModel, private val cardViewModel: CardViewModel,
                   private val activationViewModel: ActivationViewModel, private val isGib: Boolean
) : Fragment() {

    private lateinit var adapter: VoidAdapter
    private lateinit var binding: FragmentVoidBinding
    private lateinit var card: ICCCard

    /**
     * Recycler view is prepared while view is creating.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =FragmentVoidBinding.inflate(inflater,container,false)
        cardReader()
        return binding.root
    }

    /**
     * It reads the card and show some dialogs or go some function with respect to result of reading card
     */
    private fun cardReader(){
        mainActivity.readCard(0,TransactionCode.VOID.type)
        cardViewModel.getCardLiveData().observe(mainActivity) { cardData ->
            if (cardData != null && cardData.resultCode != CardServiceResult.USER_CANCELLED.resultCode()) {
                Log.d("Card Read", cardData.mCardNumber.toString())
                val transactionList = transactionViewModel.getTransactionsByCardNo(cardData.mCardNumber.toString())
                if (transactionList.isEmpty()){ // if card has no transaction
                    showNoTransaction()
                } else{
                    if (isGib){ // if it is the gib operation
                        gibVoid(cardData)
                    }
                    else{
                        this.card = cardData
                        setView()
                    }
                }
            }
        }
    }

    /**
     * It shows no transaction found dialog then finishes the mainActivity
     */
    private fun showNoTransaction(){
        val infoDialog = mainActivity.showInfoDialog(
            InfoDialog.InfoType.Info,
            getString(R.string.no_trans_found),
            false
        )
        Handler(Looper.getMainLooper()).postDelayed({
            infoDialog?.dismiss()
            mainActivity.setResult(Activity.RESULT_CANCELED)
            mainActivity.finish()
        }, 2000)
    }

    /**
     * It prepares the view with the recyclerview
     */
    private fun setView(){
        val recyclerView = binding.recyclerViewTransactions
        recyclerView.layoutManager =LinearLayoutManager(requireContext())
        transactionViewModel.createLiveData(card.mCardNumber) //list = getTransactionsByCardNo(cardNo)
        transactionViewModel.list.observe(viewLifecycleOwner) {
            adapter = VoidAdapter(it.toMutableList())
            adapter.voidFragment = this
            binding.adapter = adapter
        }
    }

    /**
     * It arranges the variables then if cardNumbers are match do transactionRoutine for the void operation
     */
    private fun gibVoid(mCard: ICCCard) {
        val refNo = transactionViewModel.refNo
        val transactionList = transactionViewModel.getTransactionsByRefNo(refNo)
        val transaction = if (transactionList != null) transactionList[0] else null
        Log.d("Void Gib", mainActivity.getString(R.string.void_transaction)+" $transaction")
        if (transaction != null) {
            if (mCard.mCardNumber == transaction.Col_PAN) {
                this.card = mCard
                doVoid(transaction)
            } else {
                mainActivity.responseMessage(ResponseCode.OFFLINE_DECLINE,"")
            }
        }else{
            mainActivity.responseMessage(ResponseCode.ERROR,getString(R.string.no_trans_found))
        }
    }



    /**
     * It starts void operation in parallel with TransactionRoutine method.
     */
    fun doVoid(transaction: Transaction){
        val contentValHelper = ContentValHelper()
        CoroutineScope(Dispatchers.Default).launch {
            transactionViewModel.transactionRoutine(card, TransactionCode.VOID.type,  Bundle(),
                contentValHelper.getContentVal(transaction),batchViewModel, mainActivity, activationViewModel.activationRepository)
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

}
