package com.tokeninc.sardis.application_template.ui.postTxn.slip

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.databinding.FragmentSlipBinding
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.void_operation.TransactionAdapter
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel


class SlipFragment(private val mainActivity: MainActivity, private val activationViewModel: ActivationViewModel,
private val transactionViewModel: TransactionViewModel, private val batchViewModel: BatchViewModel) : Fragment() {
    private lateinit var binding: FragmentSlipBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSlipBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val transactionList = transactionViewModel.allTransactions()
        setView(transactionList)
        onClickListeners(transactionList)
    }

    /**
     * This method for set recyclerView with setAdapter function related to our transactions.
     */
    private fun setView(transactionList: List<Transaction?>?){
        if (transactionList != null){
            val adapter = TransactionAdapter(transactionList.toMutableList(),false)
            adapter.slipFragment = this
            binding.rvTransaction.adapter = adapter
        }
    }

    private fun onClickListeners(transactionList: List<Transaction?>?){
        binding.backButton.setOnClickListener {
            mainActivity.popFragment()
        }

        binding.btnBatch.setOnClickListener {
            batchViewModel.getPreviousBatchSlip().observe(mainActivity){slip ->
                if (slip != null){
                    val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Progress, mainActivity.getString(R.string.printing_the_receipt), false)
                    batchViewModel.printPreviousBatchSlip(mainActivity,slip)
                    batchViewModel.getIsPrinted().observe(mainActivity) { infoDialog!!.dismiss() }
                } else{
                    val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Warning, mainActivity.getString(R.string.batch_close_not_found), false)
                    Handler(Looper.getMainLooper()).postDelayed({ infoDialog!!.dismiss() }, 2000)
                }
            }
        }

        binding.btnTransactionList.setOnClickListener {
            if (transactionViewModel.isEmpty()){
                val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Warning, mainActivity.getString(R.string.trans_not_found), false)
                Handler(Looper.getMainLooper()).postDelayed({ infoDialog!!.dismiss() }, 2000)
            } else{
                val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Progress, mainActivity.getString(R.string.printing_the_receipt), false)
                batchViewModel.printTransactionListSlip(mainActivity,activationViewModel,transactionList)
                batchViewModel.getIsPrinted().observe(mainActivity) { infoDialog!!.dismiss() }
            }
        }
    }

    fun prepareSlip(transaction: Transaction) {
        var transactionCode = transaction.Col_TransCode
        if (transaction.Col_IsVoid == 1) {
            transactionCode = TransactionCode.VOID.type
        }
        val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Progress, mainActivity.getString(R.string.printing_the_receipt), false)
        transactionViewModel.prepareCopySlip(transaction,transactionCode,activationViewModel.activationRepository,mainActivity)
        transactionViewModel.getIsPrinted().observe(mainActivity){ infoDialog!!.dismiss()}
    }

}