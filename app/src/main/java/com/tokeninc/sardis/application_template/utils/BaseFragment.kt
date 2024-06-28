package com.tokeninc.sardis.application_template.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.timeoutmanager.TimeOutActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel

abstract class BaseFragment : Fragment() {


    //This function is for initializing requireActivity to observe liveData
    protected val safeActivity: FragmentActivity by lazy { requireActivity() }
    protected lateinit var appCompatActivity: AppCompatActivity
    protected lateinit var activationViewModel: ActivationViewModel
    protected lateinit var transactionViewModel: TransactionViewModel
    protected lateinit var batchViewModel: BatchViewModel
    protected lateinit var cardViewModel: CardViewModel
    private val tag = "BaseFragment"

    protected fun showInfoDialog(
        type: InfoDialog.InfoType,
        text: String,
        isCancelable: Boolean,
        //fragmentManager: FragmentManager
    ): InfoDialog {
        val dialog = InfoDialog.newInstance(type, text, isCancelable)
        //dialog.show(fragmentManager,"")
        parentFragmentManager.let {
            dialog.show(it, "")
        } // Using null as a tag here, consider using a tag if needed
        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.i(tag, "onAttach")
        appCompatActivity = activity as AppCompatActivity
    }

    protected fun replaceFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        if (addToBackStack){
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.container, fragment)
                addToBackStack(null)
                commit()
            }
        } else{
            childFragmentManager.beginTransaction().apply {
                replace(R.id.container, fragment)
                commit()
            }
        }
    }


    protected fun showDialog(infoDialog: InfoDialog) {
        infoDialog.show(parentFragmentManager, "")
    }

    /**
     * Fragment couldn't use getString from res > values > strings,
     * therefore this method call that string from activity.
     */
    protected fun getStrings(resID: Int): String{
        return safeActivity.getString(resID)
    }

    /**
     * This method is for showing callBack message, it finishes the activity with given intent.
     */
    protected fun setResult(resultIntent: Intent) {
        safeActivity.setResult(TimeOutActivity.RESULT_OK, resultIntent)
        safeActivity.finish()
    }

    /**
     * This method gives the result code, then finishes the activity
     */
    protected fun setResult(resultCode: Int) {
        safeActivity.setResult(resultCode)
        safeActivity.finish()
    }

    protected fun popFragment() {
        parentFragmentManager.popBackStack()
    }

    /**
     * Shows a dialog to the user which asks for a confirmation.
     * Dialog will be dismissed automatically when user taps on to confirm/cancel button.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, String, InfoDialog.InfoDialogButtons, int, InfoDialogListener)}
     */
    fun showConfirmationDialog(type: InfoDialog.InfoType, title: String, info: String,
                               buttons: InfoDialog.InfoDialogButtons, arg: Int, listener: InfoDialogListener
    ): InfoDialog? {
        val dialog = InfoDialog.newInstance(type, title, info, buttons, arg, listener)  //created a dialog with InfoDialog newInstance method
        dialog.show(parentFragmentManager, "")
        return dialog
    }

    /**
     * It initializes viewModels for extended child classes via dependency injection,
     * instead of defining them in all fragments again
     */
    protected fun initializeViewModels(){
        val getTransactionViewModel: TransactionViewModel by viewModels()
        transactionViewModel = getTransactionViewModel
        val getCardViewModel: CardViewModel by viewModels()
        cardViewModel = getCardViewModel
        val getBatchViewModel: BatchViewModel by viewModels()
        batchViewModel = getBatchViewModel
        val getActivationViewModel: ActivationViewModel by viewModels()
        activationViewModel = getActivationViewModel
    }

    /**
     * It reads card, if it is not connected to cardService, it first connect it then read it.
     */
    protected fun readCard(amount: Int, transactionCode: Int) {
        // if binding is null (not connected) connect to the service first
        if (cardViewModel.getCardServiceBinding() == null){
            Log.i(tag, "connectCardService")
            connectCardService()
        }
        // it observes whether it's connected
        cardViewModel.getCardServiceConnected().observe(safeActivity){
            if (it){
                Log.i(tag, "getCardServiceConnected connected")
                Handler(Looper.getMainLooper()).postDelayed({
                    cardViewModel.readCard(amount,transactionCode)
                }, 500)
            }
        }
        // it observes whether it has message
        cardViewModel.getCallBackMessage().observe(safeActivity) { responseCode ->
            Log.d("Card Result Code with call back message", responseCode.name)
            if (responseCode == ResponseCode.CANCELED || responseCode == ResponseCode.ERROR) { //if it is canceled
                responseMessage(responseCode,"")
            }
        }
    }

    /**
     * This function is called whenever cardService is required.
     * If it couldn't connect to the card service after 10 seconds, it shows a dialog and finishes to the mainActivity.
     */
    protected fun connectCardService(fromActivation: Boolean = false) {
        var isCancelled = false
        //first create an Info dialog for processing, when this is showing a 10 seconds timer starts
        val timer: CountDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() { //when it's finished, (after 10 seconds)
                isCancelled = true // make isCancelled true (because cardService couldn't be connected)
                Log.i(tag, "CardService Not connected")
                val infoDialog = showInfoDialog(InfoDialog.InfoType.Declined, getString(R.string.card_service_error), false)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog.dismiss()
                }, 2000)
            }
        }

        timer.start()
        cardViewModel.initializeCardServiceBinding(appCompatActivity)

        cardViewModel.getCardServiceConnected().observe(safeActivity) { isConnected ->
            // cardService is connected before 10 seconds (which is the limit of the timer)
            if (isConnected && !isCancelled) {
                timer.cancel() // stop timer
                //if it has message show them with toast ( it comes first connection with setEMVConfiguration)
                cardViewModel.getToastMessage().observe(safeActivity){
                    Toast.makeText(safeActivity,it, Toast.LENGTH_SHORT).show()
                    // In multi banking pgw, at first installation it always shows Toast until mainActivity finishes; thus it should be reset
                    cardViewModel.resetToastMessage()
                    if (!fromActivation){
                        Toast.makeText(safeActivity,getString(R.string.config_updated), Toast.LENGTH_LONG).show()
                    }
                }
                Log.i(tag, "Connected CardService")
            }
        }
    }

    /**
     * It returns the message and finish the activity
     */
    protected fun responseMessage(responseCode: ResponseCode, message: String, resultIntent: Intent? = null) {
        val bundle = Bundle()
        val intent = Intent()
        Log.i(tag, "responseMessage: $responseCode, message: $message")
        when (responseCode) {
            ResponseCode.ERROR -> {
                if (message != "") {
                    showInfoDialog(InfoDialog.InfoType.Error, getString(R.string.error) + ": " + message, false)
                } else {
                    showInfoDialog(InfoDialog.InfoType.Error, getString(R.string.error), false)
                }
            }
            ResponseCode.CANCELED -> showInfoDialog(InfoDialog.InfoType.Warning, getString(R.string.cancelled), false)
            ResponseCode.OFFLINE_DECLINE -> showInfoDialog(InfoDialog.InfoType.Warning, getString(R.string.offline_decline), false)
            else -> showInfoDialog(InfoDialog.InfoType.Declined, getString(R.string.declined), false)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (resultIntent != null){
                setResult(resultIntent)
            } else{
                bundle.putInt("ResponseCode", responseCode.ordinal)
                intent.putExtras(bundle)
                setResult(intent)
            }
        }, 2000)
    }

}