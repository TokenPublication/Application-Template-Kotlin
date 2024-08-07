package com.tokeninc.sardis.application_template.ui.postTxn.slip

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.databinding.FragmentSlipBinding
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.void_operation.TransactionAdapter
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SlipFragment() : BaseFragment() {
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
        initializeViewModels()
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
            popFragment()
        }

        binding.btnBatch.setOnClickListener {
            batchViewModel.getPreviousBatchSlip().observe(safeActivity){slip ->
                if (slip != null){
                    val infoDialog = showInfoDialog(InfoDialog.InfoType.Progress, getStrings(R.string.printing_the_receipt), false)
                    batchViewModel.printPreviousBatchSlip(safeActivity,slip)
                    batchViewModel.getIsPrinted().observe(safeActivity) { infoDialog.dismiss() }
                } else{
                    val infoDialog = showInfoDialog(InfoDialog.InfoType.Warning, getStrings(R.string.batch_close_not_found), false)
                    Handler(Looper.getMainLooper()).postDelayed({ infoDialog.dismiss() }, 2000)
                }
            }
        }

        binding.btnTransactionList.setOnClickListener {
            if (transactionViewModel.isEmpty()){
                val infoDialog = showInfoDialog(InfoDialog.InfoType.Warning, getString(R.string.trans_not_found), false)
                Handler(Looper.getMainLooper()).postDelayed({ infoDialog.dismiss() }, 2000)
            } else{
                val infoDialog = showInfoDialog(InfoDialog.InfoType.Progress, getString(R.string.printing_the_receipt), false)
                batchViewModel.printTransactionListSlip(safeActivity,activationViewModel,transactionList)
                batchViewModel.getIsPrinted().observe(safeActivity) { infoDialog.dismiss() }
            }
        }
    }

    /**
     * It shows info dialog for printing customer slip, then asks user to print merchant slip
     * If it will be printed call printing merchant slip function, else finish the function
     */

    private lateinit var observer: Observer<Boolean>
    fun prepareSlip(transaction: Transaction) {
        var transactionCode = transaction.Col_TransCode
        if (transaction.Col_IsVoid == 1) {
            transactionCode = TransactionCode.VOID.type
        }
        val infoDialog = showInfoDialog(InfoDialog.InfoType.Progress, getStrings(R.string.printing_the_customer_receipt), true)
        transactionViewModel.prepareCopyCustomerSlip(transaction,transactionCode,activationViewModel.activationRepository,safeActivity)
        observer = Observer{ isPrinted ->
            if (isPrinted) {
                infoDialog.dismiss()
                showConfirmationDialog(
                    InfoDialog.InfoType.Info,
                    getStrings(R.string.preparing_the_receipt),
                    getStrings(R.string.printing_the_merchant_receipt),
                    InfoDialog.InfoDialogButtons.Both, 1,
                    object : InfoDialogListener {
                        override fun confirmed(i: Int) {
                            prepareMerchantSlip(transaction, transactionCode)
                        }

                        override fun canceled(i: Int) {
                            initSlipLiveData()
                        }
                    })
                transactionViewModel.getIsCustomerPrinted().removeObserver(observer) // not observe twice, when prepareSlip is called again
            }
        }
        transactionViewModel.getIsCustomerPrinted().observe(safeActivity, observer)
    }

    private lateinit var merchantObserver: Observer<Boolean>


    /**
     * It prints merchant slip
     */
    private fun prepareMerchantSlip(transaction: Transaction, transactionCode: Int){
        val infoDialog = showInfoDialog(InfoDialog.InfoType.Progress, getStrings(R.string.printing_the_merchant_receipt), true)
        transactionViewModel.prepareCopyMerchantSlip(transaction,transactionCode,activationViewModel.activationRepository,safeActivity)
        merchantObserver = Observer {
            if (it) {
                infoDialog.dismiss()
                initSlipLiveData()
                transactionViewModel.getIsPrinted().removeObserver(merchantObserver)
            }
        }
        transactionViewModel.getIsPrinted().observe(safeActivity,merchantObserver)
    }

    /**
     * It initializes slip live data in transaction view model for preventing multiple slip copying operations
     */
    private fun initSlipLiveData(){
        Handler(Looper.getMainLooper()).postDelayed({
            transactionViewModel.initSlipLiveData()
        }, 400)
    }

}