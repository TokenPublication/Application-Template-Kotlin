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
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This is the class for Post Transaction Methods.
 */
class PostTxnFragment(private val mainActivity: MainActivity, private val transactionViewModel: TransactionViewModel,
                      private val batchViewModel: BatchViewModel, private val cardViewModel: CardViewModel,
                      private val activationViewModel: ActivationViewModel) : Fragment() {

    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMenu()
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
            val voidFragment = VoidFragment(mainActivity,transactionViewModel,batchViewModel,cardViewModel,activationViewModel,false)
            mainActivity.replaceFragment(voidFragment)
        }))
        menuItems.add(MenuItem(getStrings(R.string.refund), {
            val refundFragment = RefundFragment(mainActivity, cardViewModel, transactionViewModel, batchViewModel, activationViewModel)
            mainActivity.addFragment(refundFragment)
        }))
        menuItems.add(MenuItem(getStrings(R.string.batch_close), {
            if (transactionViewModel.allTransactions().isNullOrEmpty()){
                val infoDialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Warning,getStrings(R.string.batch_close_not_found),false)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog!!.dismiss()
                }, 2000)
            }
            else{
                mainActivity.showConfirmationDialog(InfoDialog.InfoType.Info,getStrings(R.string.batch_close),getStrings(
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
            val exampleFragment = ExampleFragment(mainActivity,cardViewModel)
            mainActivity.addFragment(exampleFragment)
        }))
        menuItems.add(MenuItem(getStrings(R.string.slip_menu),{
            val slipFragment = SlipFragment(mainActivity,activationViewModel,transactionViewModel,batchViewModel)
            mainActivity.addFragment(slipFragment)
        }))
        menuItems.add(MenuItem(getString(R.string.demo_mode), {
            val demoFragment = DemoFragment(mainActivity)
            mainActivity.addFragment(demoFragment)
        }))

        val menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn", true, R.drawable.token_logo_png)
        mainActivity.replaceFragment(menuFragment as Fragment)
    }

    /**
     * It calls batchClose routine function which close the batch, preparing the intent and printing the slip in background
     * and update UI on foreground
     */
    fun doBatchClose() {
        CoroutineScope(Dispatchers.Default).launch {
            batchViewModel.batchCloseRoutine(mainActivity,transactionViewModel,activationViewModel)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Connecting to the Server",false)
        batchViewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is BatchViewModel.UIState.Loading -> mainActivity.showDialog(dialog)
                is BatchViewModel.UIState.Connecting -> dialog.update(InfoDialog.InfoType.Progress, getStrings(R.string.preparing_the_receipt) + "% ${state.data}")
                is BatchViewModel.UIState.Success -> mainActivity.showDialog(InfoDialog.newInstance(InfoDialog.InfoType.Confirmed,getStrings(R.string.batch_close_success),true))
            }
        }
        batchViewModel.getLiveIntent().observe(mainActivity){liveIntent ->
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
