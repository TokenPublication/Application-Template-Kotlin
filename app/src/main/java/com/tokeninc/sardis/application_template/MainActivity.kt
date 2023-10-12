package com.tokeninc.sardis.application_template

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.timeoutmanager.TimeOutActivity
import com.tokeninc.cardservicebinding.BuildConfig
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.*
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.data.model.type.CardReadType
import com.tokeninc.sardis.application_template.databinding.ActivityMainBinding
import com.tokeninc.sardis.application_template.ui.*
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.activation.SettingsFragment
import com.tokeninc.sardis.application_template.ui.postTxn.PostTxnFragment
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.refund.RefundFragment
import com.tokeninc.sardis.application_template.ui.postTxn.void_operation.VoidFragment
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.SaleFragment
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.ui.service.ServiceViewModel
import com.tokeninc.sardis.application_template.ui.trigger.TriggerFragment
import com.tokeninc.sardis.application_template.utils.ExtraKeys
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*


/** This is the Main Activity class,
 * all operations are run here because this application is designed as a single-activity architecture
 * It's @AndroidEntryPoint because, we get ViewModel inside of class,
 */
@AndroidEntryPoint
class MainActivity : TimeOutActivity() {

    //initializing View Models and Fragments
    private lateinit var activationViewModel: ActivationViewModel
    private lateinit var batchViewModel: BatchViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var cardViewModel: CardViewModel
    private lateinit var serviceViewModel: ServiceViewModel
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var saleFragment: SaleFragment
    private lateinit var triggerFragment: TriggerFragment
    private lateinit var postTxnFragment: PostTxnFragment

    /**
     * This function is overwritten to continue the activity where it was left when
     * the configuration is changed (i.e screen rotation)
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        startActivity()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity()
    }

    /**
     * This is for starting the activity, to start the activity it should be connected
     * Device Info, Card Service and KMS Service; if it couldn't connect any of them application shouldn't start
     * Additionally, viewModels are created here with dependency Injection, and also fragments are created here as well.
     * If everything goes well, it calls a function named actionControl, which calls another functions with respect to intent's action.
     */
    private fun startActivity() {
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        //get ViewModels from Dagger-Hilt easily, but to make this easy you need to implement each dependency clearly
        val getActivationViewModel: ActivationViewModel by viewModels()
        activationViewModel = getActivationViewModel
        val getBatchViewModel: BatchViewModel by viewModels()
        batchViewModel = getBatchViewModel
        val getTransactionViewModel: TransactionViewModel by viewModels()
        transactionViewModel = getTransactionViewModel
        val getCardViewModel: CardViewModel by viewModels()
        cardViewModel = getCardViewModel
        val getServiceViewModel: ServiceViewModel by viewModels()
        serviceViewModel = getServiceViewModel

        saleFragment = SaleFragment(transactionViewModel, this, batchViewModel, cardViewModel, activationViewModel)
        settingsFragment = SettingsFragment(this, activationViewModel,cardViewModel)
        triggerFragment = TriggerFragment(this)
        postTxnFragment = PostTxnFragment(this, transactionViewModel, batchViewModel, cardViewModel, activationViewModel)

        buildConfigs()
        connectServices()
    }

    /**
     * This function calls serviceRoutine, which firstly connects DeviceInfo service, then KMS service
     * After it connects these two services successfully, it calls connecting Card Service and without waiting connecting
     * card service it updates the UI with respect to action mainActivity has. It tries to connect cardService on background
     * If it couldn't connect KMS or deviceInfo services, it warns the user then finishes the mainActivity
     */
    private fun connectServices(){
        serviceViewModel.serviceRoutine(this)
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,getString(R.string.connect_services),false)
        serviceViewModel.getUiState().observe(this) { state ->
            when (state) {
                is ServiceViewModel.ServiceUIState.ErrorDeviceInfo -> {
                    dialog.update(InfoDialog.InfoType.Error, getString(R.string.device_info_service_Error))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        finish()
                    }, 2000)
                }
                is ServiceViewModel.ServiceUIState.ErrorKMS -> {
                    dialog.update(InfoDialog.InfoType.Error, getString(R.string.kms_service_error))
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        finish()
                    }, 2000)
                }
                is ServiceViewModel.ServiceUIState.Connected -> {
                    dialog.dismiss()
                    connectCardService()
                    actionChanged(intent.action)
                }
            }
        }
    }

    /**
    Firstly, added TR1000 and TR400 configurations to build.gradle file. After that,
    related to Build Variant (400TRDebug or 1000TRDebug) the manifest file created with apk
    and the app name in manifest file will be 1000TR or 400TR.
     */
    private fun buildConfigs() {
        when (BuildConfig.FLAVOR) {
            "TR1000" -> {
                Log.i("TR1000 APP", "Application Template for 1000TR")
            }
            "TR400" -> {
                Log.i("TR400 APP", "Application Template for 400TR")
            }
            "330" -> {
                Log.i("TR330 APP", "Application Template for 330TR")
            }
        }
    }

    /**
     * This function is called whenever cardService is required.
     * If it couldn't connect to the card service after 10 seconds, it shows a dialog and finishes to the mainActivity.
     */
    fun connectCardService(fromActivation: Boolean = false) {
        var isCancelled = false
        //first create an Info dialog for processing, when this is showing a 10 seconds timer starts
        val timer: CountDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() { //when it's finished, (after 10 seconds)
                isCancelled = true // make isCancelled true (because cardService couldn't be connected)
                Log.i("CardService","Not connected")
                val infoDialog = showInfoDialog(InfoDialog.InfoType.Declined, getString(R.string.card_service_error), false)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog?.dismiss()
                }, 2000)
            }
        }

        timer.start()
        cardViewModel.initializeCardServiceBinding(this)

        cardViewModel.getCardServiceConnected().observe(this) { isConnected ->
            // cardService is connected before 10 seconds (which is the limit of the timer)
            if (isConnected && !isCancelled) {
                timer.cancel() // stop timer
                //if it has message show them with toast ( it comes first connection with setEMVConfiguration)
                cardViewModel.getToastMessage().observe(this){
                    Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
                    // In multi banking pgw, at first installation it always shows Toast until mainActivity finishes; thus it should be reset
                    cardViewModel.resetToastMessage()
                    if (!fromActivation){
                        Toast.makeText(this,getString(R.string.config_updated),Toast.LENGTH_LONG).show()
                    }
                }

                Log.i("Connected","CardService")
            }
        }
    }

    /**
     * This function calls corresponding functions whenever an action of intent is changed
     */
    private fun actionChanged(action: String?){
        if (action.equals(getString(R.string.PosTxn_Action)) || action.equals(getString(R.string.Sale_Action)) || action.equals(getString(R.string.BatchClose_Action))
            || action.equals(getString(R.string.Parameter_Action)) || action.equals(getString(R.string.Refund_Action)) )   {
            activationWarning((activationViewModel.merchantID() == null || activationViewModel.terminalID() == null),true)
        } else {
            activationWarning((activationViewModel.merchantID() == null || activationViewModel.terminalID() == null),false)
        }
        when (action) {
            getString(R.string.PosTxn_Action) -> replaceFragment(postTxnFragment)
            getString(R.string.Sale_Action) -> saleActionReceived()
            getString(R.string.Settings_Action) -> replaceFragment(settingsFragment)
            getString(R.string.BatchClose_Action) -> {
                if (transactionViewModel.allTransactions().isNullOrEmpty()) { //if it is empty just show no transaction dialog
                    val infoDialog = showInfoDialog(InfoDialog.InfoType.Warning,getString(R.string.batch_close_not_found),false)
                    Handler(Looper.getMainLooper()).postDelayed({
                        infoDialog!!.dismiss()
                        responseMessage(ResponseCode.ERROR,getString(R.string.batch_close_not_found))
                    }, 2000)
                } else { //else implementing batch closing and finish that activity
                    postTxnFragment.doBatchClose()
                }
            }

            getString(R.string.Parameter_Action) -> replaceFragment(triggerFragment)
            getString(R.string.Refund_Action) -> refundActionReceived()
            else -> replaceFragment(settingsFragment)
        }
    }

    /** This function checks activation by checking MID and TID parameters
     *  If they are empty, warns the customer to activate application then finishes the mainActivity     *
     */
    private fun activationWarning(isNull: Boolean, finish: Boolean) {
        if (isNull) {
            val infoDialog = showInfoDialog(
                InfoDialog.InfoType.Warning,
                getString(R.string.activating_template),
                false
            )
            Handler(Looper.getMainLooper()).postDelayed({
                infoDialog!!.dismiss()
                if (finish)
                    finish()
            }, 3000)
        }
    }

    /**
     * This function is called when action == "SALE". Action could be "SALE" in 3 different scenarios
     * 1- When the customer clicks on credit card in pgw and then selects Application Template as a Banking Application
     * ( If the device has only Application Template as a Banking Application, pgw automatically directs user to Application Template when clicking credit card)
     * 2- When GiB sends a sale request
     * 3- When the card is read by payment gateway and the Application Template is the only issuer of this card, in this situation
     * payment gateway automatically directs the sale to Application Template and sale action received.
     */
    private fun saleActionReceived() {
        // get the amount from sale intent, and assign amount to corresponding classes
        val amount = intent.extras!!.getInt("Amount")
        saleFragment.setAmount(amount)

        //controlling whether the request coming from gib
        val isGIB = (this.applicationContext as AppTemp).getCurrentDeviceMode() == DeviceInfo.PosModeEnum.GIB.name
        val bundle = intent.extras
        val cardData: String? = bundle?.getString("CardData")

        // controlling whether demoMode is enabled (open)
        val sharedPreferences = getSharedPreferences("myprefs", MODE_PRIVATE)
        val isDemoMode = sharedPreferences.getBoolean("demo_mode", false)

        // when sale operation is called from pgw which has multi bank and app temp is the only issuer of this card
        if (!isGIB && cardData != null && cardData != "CardData" && cardData != " ") {
            //replaceFragment(saleFragment)
            Handler(Looper.getMainLooper()).postDelayed({
                saleFragment.doSale(cardData)
            }, 500)
        }

        // when sale request comes from GIB
        else if (intent.extras != null) {
            val cardReadType = intent.extras!!.getInt("CardReadType")
            if (cardReadType == CardReadType.ICC.type) {
                cardViewModel.setGibSale(true)
                saleFragment.cardReader(true)
            }
            else{ // it couldn't enter here if it is a gib sale because intent doesn't include cardReadType because
                // card wouldn't be read before enter the application template in a normal scenerio
                // when app temp is only banking app or user selects app temp for sale
                if (isDemoMode){
                    saleFragment.cardReader(false)
                }
                else{
                    replaceFragment(saleFragment)
                }
            }
        }
    }

    /**
     * It reads card, if it couldn't connect cardService before, first connect the cardService then reads card
     */
    fun readCard(amount: Int, transactionCode: Int) {
        if (cardViewModel.getCardServiceBinding() != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                cardViewModel.readCard(amount,transactionCode)
            }, 500)
            //when read card is cancelled (on back pressed) finish the main activity
            cardViewModel.getCallBackMessage().observe(this) { responseCode ->
                Log.d("Card Result Code with call back message", responseCode.name)
                if (responseCode == ResponseCode.CANCELED || responseCode == ResponseCode.ERROR) { //if it is canceled
                    responseMessage(responseCode,"")
                }
            }
        } else{
            connectCardService()
            Handler(Looper.getMainLooper()).postDelayed({
                readCard(amount,transactionCode) // wait for connecting cardService, if it doesn't wait it enters recursive loop
            }, 400)
        }
    }

    /** This function only calls whenever Refund Action is received.
     * If there is no RefundInfo on current intent, it will show info dialog with No Refund Intent for 2 seconds.
     * else -> it transforms refundInfo to JSON object to parse it easily. Then get ReferenceNo and BatchNo
     * Then it compares intent batch number with current Batch Number from database, if they are equal then start Void operation
     * else start refund operation. */
    private fun refundActionReceived() {
        if (intent.extras == null || intent.extras!!.getString("RefundInfo") == null) {
            responseMessage(ResponseCode.ERROR,getString(R.string.refund_info_not_found))
        } else {
            val refundInfo = intent.extras!!.getString("RefundInfo")
            val json = JSONObject(refundInfo!!)
            val refNo = json.getString("RefNo")
            transactionViewModel.refNo = refNo
            val amount = json.getString("Amount").toInt()
            val transactionBatchNo = json.getInt("BatchNo")
            val currentBatchNo = batchViewModel.getBatchNo()
            if (transactionBatchNo == currentBatchNo) { // GIB Void Operation
                readCard(amount,TransactionCode.VOID.type)
                val voidFragment = VoidFragment(this,transactionViewModel,batchViewModel,cardViewModel,activationViewModel,true)
                replaceFragment(voidFragment)
            } else{ // GIB Refund Operation (because refund request is received after closing batch
                val authCode = json.getString("AuthCode")
                val tranDate = json.getString("TranDate")
                val cardNo = json.getString("CardNo")
                val refundBundle = Bundle()
                refundBundle.putString(ExtraKeys.ORG_AMOUNT.name, amount.toString())
                refundBundle.putString(ExtraKeys.REFUND_AMOUNT.name, amount.toString())
                refundBundle.putString(ExtraKeys.TRAN_DATE.name, tranDate.toString())
                refundBundle.putString(ExtraKeys.REF_NO.name, refNo)
                refundBundle.putString(ExtraKeys.AUTH_CODE.name, authCode)
                refundBundle.putString(ExtraKeys.CARD_NO.name, cardNo)
                readCard(amount,TransactionCode.MATCHED_REFUND.type)
                val refundFragment = RefundFragment(this, cardViewModel, transactionViewModel, batchViewModel, activationViewModel)
                refundFragment.refundAfterReadCard(null,refundBundle)
            }
        }
    }

    /**
     * This method is for showing callBack message, it finishes the activity with given intent.
     */
    fun setResult(resultIntent: Intent) {
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    /**
     * This is for showing infoDialog
     * @param infoDialog info dialog that will be showed
     */
    fun showDialog(infoDialog: InfoDialog) {
        infoDialog.show(supportFragmentManager, "")
    }

    /**
     * Shows a dialog to the user which asks for a confirmation.
     * Dialog will be dismissed automatically when user taps on to confirm/cancel button.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, String, InfoDialog.InfoDialogButtons, int, InfoDialogListener)}
     */
    fun showConfirmationDialog(type: InfoDialog.InfoType, title: String, info: String,
                               buttons: InfoDialog.InfoDialogButtons, arg: Int, listener: InfoDialogListener): InfoDialog? {
        val dialog = InfoDialog.newInstance(type, title, info, buttons, arg, listener)  //created a dialog with InfoDialog newInstance method
        dialog.show(supportFragmentManager, "")
        return dialog
    }

    /**
     * Returns time out value in seconds for activities which extend
     * @see TimeOutActivity
     * In case of any user interaction, timeout timer will be reset.
     *
     * If any activity will not have time out,
     * override this method from that activity and @return '0'.
     */
    override fun getTimeOutSec(): Int {
        return 60
    }

    /** This adds fragments to the back stack, in this way user can return this fragment after the view has been changed.
     * and replace fragment.
     */
    fun addFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    /**
     * This pops the fragment from fragment stack
     */
    fun popFragment() {
        supportFragmentManager.popBackStack()
    }

    /**
     * This is for replacing the fragment without adding it to the stack
     */
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment) //replacing fragment
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

    /**
     * This shows infoDialog
     */
    fun showInfoDialog(type: InfoDialog.InfoType, text: String, isCancelable: Boolean): InfoDialog? {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(supportFragmentManager, "")
        return fragment
    }

    /**
     * It takes @param responseCode and message and control it with switch case.
     * Related to it's value, the info dialog shows in screen and activity will
     * finish with intent contains response code. Also with message parameter, the
     * error messages can seen at screen.
     */
    fun responseMessage(responseCode: ResponseCode, message: String, resultIntent: Intent? = null) {
        val bundle = Bundle()
        val intent = Intent()
        when (responseCode) {
            ResponseCode.ERROR -> {
                if (message != "") {
                    showInfoDialog(InfoDialog.InfoType.Error, getString(R.string.error) + ": " + message, true)
                } else {
                    showInfoDialog(InfoDialog.InfoType.Error, getString(R.string.error), true)
                }
            }
            ResponseCode.CANCELED -> showInfoDialog(InfoDialog.InfoType.Warning, getString(R.string.cancelled), true)
            ResponseCode.OFFLINE_DECLINE -> showInfoDialog(InfoDialog.InfoType.Warning, getString(R.string.offline_decline), true)
            else -> showInfoDialog(InfoDialog.InfoType.Declined, getString(R.string.declined), true)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            bundle.putInt("ResponseCode", responseCode.ordinal)
            if (resultIntent != null){
                val resBundle = resultIntent.extras
                val amount = resBundle!!.getInt("Amount")
                val resCode = resBundle.getInt("ResponseCode")
                val slipType = resBundle.getInt("SlipType")
                val paymentType = resBundle.getInt("PaymentType")
                bundle.putInt("Amount",amount)
                bundle.putInt("ResponseCode",resCode)
                bundle.putInt("SlipType",slipType)
                bundle.putInt("PaymentType",paymentType)
                Log.i("Dummy Response","Amount: $amount, ResponseCode: $resCode, SlipType: $slipType, PaymentType: $paymentType")
            }
            intent.putExtras(bundle)
            setResult(intent)
            setResult(Activity.RESULT_CANCELED)
        }, 2000)
    }

    /**
     * Whenever mainActivity is destroyed, it calls the onDestroy method of cardViewModel and CardRepository from there.
     * It is needed because the lifecycle of CardRepository is different than the mainActivity, it couldn't finish with the mainActivity,
     * thus some variable's values are preserved and that gives an error in the following operations.
     */
    override fun onDestroy() {
        Log.d("Main Activity On Destroy", "MainActivity is destroyed")
        cardViewModel.onDestroyed()
        super.onDestroy()
    }
}
