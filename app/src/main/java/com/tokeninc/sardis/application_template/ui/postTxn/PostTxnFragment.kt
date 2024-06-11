package com.tokeninc.sardis.application_template.ui.postTxn

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.examples.ExampleFragment
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.demoMode.DemoFragment
import com.tokeninc.sardis.application_template.ui.postTxn.refund.RefundFragment
import com.tokeninc.sardis.application_template.ui.postTxn.slip.SlipFragment
import com.tokeninc.sardis.application_template.ui.postTxn.void_operation.VoidFragment
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.utils.BaseFragment
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the class for Post Transaction Methods.
 */
@AndroidEntryPoint
class PostTxnFragment(private val isBatch: Boolean = false) : BaseFragment() {

    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModels()
        if (isBatch){
            doBatchClose()
        } else{
            showMenu()
        }
    }

    override fun onDestroyView() {
        Log.d("PostTxnDestroying","true")
        super.onDestroyView()
    }
    /**
     * This function prepares the Post Transactions Menu that contains Void, Refund, Batch Close, Examples and Slip Repetition
     */
    private fun showMenu(){
        val menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem(getStrings(R.string.void_transaction), {
            val voidFragment = VoidFragment(false)
            replaceFragment(voidFragment)
        }))
        menuItems.add(MenuItem(getStrings(R.string.refund), {
            val refundFragment = RefundFragment()
            replaceFragment(refundFragment, true)
        }))
        menuItems.add(MenuItem(getStrings(R.string.batch_close), {
            if (transactionViewModel.allTransactions().isNullOrEmpty()){
                val infoDialog = showInfoDialog(InfoDialog.InfoType.Warning,getStrings(R.string.batch_close_not_found),false)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog.dismiss()
                }, 2000)
            }
            else{
                showConfirmationDialog(InfoDialog.InfoType.Info,getStrings(R.string.batch_close),getStrings(
                    R.string.batch_close_will_be_done
                ),InfoDialog.InfoDialogButtons.Both,1,
                    object : InfoDialogListener {
                        override fun confirmed(i: Int) {
                            doBatchClose()
                        }
                        override fun canceled(i: Int) {}
                    })
            }
        }))
        menuItems.add(MenuItem(getStrings(R.string.examples), {
            val exampleFragment = ExampleFragment()
            replaceFragment(exampleFragment, true)
        }))
        menuItems.add(MenuItem(getStrings(R.string.slip_menu),{
            val slipFragment = SlipFragment()
            replaceFragment(slipFragment, true)
        }))
        menuItems.add(MenuItem(getString(R.string.demo_mode), {
            val demoFragment = DemoFragment()
            replaceFragment(demoFragment, true)
        }))

        val menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn", true, R.drawable.token_logo_png)
        replaceFragment(menuFragment as Fragment)
    }

    /**
     * It calls batchClose routine function which close the batch, preparing the intent and printing the slip in background
     * and update UI on foreground
     */
    fun doBatchClose() {
        CoroutineScope(Dispatchers.Default).launch {
            batchViewModel.batchCloseRoutine(safeActivity,transactionViewModel,activationViewModel)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        batchViewModel.getUiState().observe(safeActivity) { state ->
            when (state) {
                is BatchViewModel.UIState.Loading -> showDialog(dialog)
                is BatchViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress, getStrings(R.string.preparing_the_receipt) + "% ${state.data}")
                is BatchViewModel.UIState.Success -> showDialog(InfoDialog.newInstance(InfoDialog.InfoType.Confirmed,getStrings(R.string.batch_close_success),true))
            }
        }
        batchViewModel.getLiveIntent().observe(safeActivity){liveIntent ->
            setResult(liveIntent)
        }
    }

}
