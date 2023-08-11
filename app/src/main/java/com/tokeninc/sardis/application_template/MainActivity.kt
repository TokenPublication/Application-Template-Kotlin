package com.tokeninc.sardis.application_template

import android.annotation.SuppressLint
import android.content.Context
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
import com.tokeninc.libtokenkms.KMSWrapperInterface.InitCallbacks
import com.tokeninc.libtokenkms.TokenKMS
import com.tokeninc.sardis.application_template.*
import com.tokeninc.sardis.application_template.databinding.ActivityMainBinding
import com.tokeninc.sardis.application_template.enums.*
import com.tokeninc.sardis.application_template.ui.*
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.activation.SettingsFragment
import com.tokeninc.sardis.application_template.ui.postTxn.PostTxnFragment
import com.tokeninc.sardis.application_template.ui.postTxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.postTxn.refund.RefundFragment
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.ui.sale.SaleFragment
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.ui.trigger.TriggerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*


/** This is the Main Activity class,
 * all operations are run here because this application is designed as a single-activity architecture
 * It's @AndroidEntryPoint because, we get ViewModel inside of class,
 */
@AndroidEntryPoint
class MainActivity : TimeOutActivity() {

    //initializing bindings
    private var cardServiceBinding: CardServiceBinding? = null

    //initializing View Models and Fragments
    private lateinit var activationViewModel: ActivationViewModel
    private lateinit var batchViewModel: BatchViewModel
    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var cardViewModel: CardViewModel
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var saleFragment: SaleFragment
    private lateinit var triggerFragment: TriggerFragment
    private lateinit var postTxnFragment: PostTxnFragment
    private lateinit var refundFragment: RefundFragment

    //initializing other variables
    var infoDialog: InfoDialog? = null
    lateinit var tokenKMS: TokenKMS

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
    Firstly, added TR1000 and TR400 configurations to build.gradle file. After that,
    related to Build Variant (400TRDebug or 1000TRDebug) the manifest file created with apk
    and the app name in manifest file will be 1000TR or 400TR.
     */
    private fun buildConfigs() {
        if (BuildConfig.FLAVOR == "TR1000") {
            Log.d("TR1000 APP", "Application Template for 1000TR")
        }
        if (BuildConfig.FLAVOR == "TR400") {
            Log.d("TR400 APP", "Application Template for 400TR")
        }
    }

    /**
     * This is for starting the activity, databases which are created with respect to context and viewModels which are created
     * wrt to databases and services are created here. After that, some functions are called with respect to action of the current intent
     */
    private fun startActivity() {
        buildConfigs()
        val binding = ActivityMainBinding.inflate(layoutInflater)

        //get ViewModels from Dagger-Hilt easily, but to make this easy you need to implement each dependency clearly
        val getActivationViewModel: ActivationViewModel by viewModels()
        activationViewModel = getActivationViewModel
        val getBatchViewModel: BatchViewModel by viewModels()
        batchViewModel = getBatchViewModel
        val getTransactionViewModel: TransactionViewModel by viewModels()
        transactionViewModel = getTransactionViewModel
        val getCardViewModel: CardViewModel by viewModels()
        cardViewModel = getCardViewModel

        saleFragment = SaleFragment(transactionViewModel, this, batchViewModel, cardViewModel, activationViewModel)
        settingsFragment = SettingsFragment(this, activationViewModel, intent)
        triggerFragment = TriggerFragment(this)
        refundFragment = RefundFragment(this, cardViewModel, transactionViewModel, batchViewModel, activationViewModel)
        postTxnFragment = PostTxnFragment(this, transactionViewModel, refundFragment, batchViewModel, cardViewModel, activationViewModel)
        connectCardService()

        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        tokenKMS = TokenKMS() //connecting KMS Service with this flow
        tokenKMS.init(applicationContext, object : InitCallbacks {
            override fun onInitSuccess() {
                Log.i("Token KMS onInitSuccess", "KMS Init OK")
                actionChanged(intent.action)
            }

            override fun onInitFailed() {
                Log.i("Token KMS onInitFailed", "KMS Init Failed")
                infoDialog = showInfoDialog(InfoDialog.InfoType.Error, "KMS Servis Hatası", false)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog?.dismiss()
                    finish()
                }, 2000)
            }
        })
    }

    /** This function checks activation by checking MID and TID parameters
     *  If they are empty, warns the customer to activate application then finishes the mainActivity     *
     */
    private fun activationWarning(isNull: Boolean, finish: Boolean) {
        if (isNull) {
            val infoDialog = showInfoDialog(
                InfoDialog.InfoType.Warning,
                "You must activate the application template!",
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
                if (transactionViewModel.allTransactions()
                        .isNullOrEmpty()
                ) { //if it is empty just show no transaction dialog
                    callbackMessage(ResponseCode.ERROR)
                } else { //else implementing batch closing and finish that activity
                    postTxnFragment.doBatchClose()
                }
            }

            getString(R.string.Parameter_Action) -> replaceFragment(triggerFragment)
            getString(R.string.Refund_Action) -> refundActionReceived()
            else -> replaceFragment(settingsFragment)
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
        cardViewModel.setAmount(amount)
        saleFragment.setAmount(amount)
        //controlling whether the request coming from gib
        val isGIB = (this.applicationContext as AppTemp).getCurrentDeviceMode() == DeviceInfo.PosModeEnum.GIB.name
        val bundle = intent.extras
        val cardData: String? = bundle?.getString("CardData")
        // when sale operation is called from pgw which has multi bank and app temp is the only issuer of this card
        if (!isGIB && cardData != null && cardData != "CardData" && cardData != " ") {
            replaceFragment(saleFragment)
            saleFragment.doSale(cardData)
        }
        // when sale request comes from GIB
        else if (intent.extras != null) {
            val cardReadType = intent.extras!!.getInt("CardReadType")
            if (cardReadType == CardReadType.ICC.type) {
                cardViewModel.setGibSale(true)
                saleFragment.cardReader(true)
            } else {
                replaceFragment(saleFragment)
            }
        } else { // when user select application template as a banking application
            replaceFragment(saleFragment)
        }
    }

    /**
     * This function is called whenever cardService is required.
     * If it couldn't connect to the card service after 10 seconds, it shows a dialog and finishes to the mainActivity.
     */
    private fun connectCardService() {
        var isCancelled = false
        //first create an Info dialog for processing, when this is showing a 10 seconds timer starts
        val timer: CountDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() { //when it's finished, (after 10 seconds)
                isCancelled = true //make isCancelled true (because cardService couldn't be connected)
                showInfoDialog(InfoDialog.InfoType.Declined, "Connect Failed", false)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog?.dismiss()
                    finish()
                }, 2000)
            }
        }
        timer.start()
        cardViewModel.initializeCardServiceBinding(this)
        cardViewModel.getCardServiceConnected().observe(this) { isConnected ->
            // cardService is connected before 10 seconds (which is the limit of the timer)
            if (isConnected && !isCancelled) {
                timer.cancel() // stop timer
                cardServiceBinding = cardViewModel.getCardServiceBinding()
                infoDialog?.dismiss()
                setEMVConfiguration(true) //setEMVConfig when connected
            }
        }
    }

    fun readCard() {
        if (cardServiceBinding != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                cardViewModel.readCard()
            }, 500)
            //when read card is cancelled (on back pressed) finish the main activity
            cardViewModel.getCallBackMessage().observe(this) { responseCode ->
                Log.d("Card Result Code with call back message", responseCode.name)
                if (responseCode == ResponseCode.CANCELED || responseCode == ResponseCode.ERROR) { //if it is canceled
                    callbackMessage(responseCode)
                }
            }
        } else{
            connectCardService()
            Handler(Looper.getMainLooper()).postDelayed({
                readCard() // wait for connecting cardService, if it doesn't wait it enters recursive loop
            }, 400)
        }
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

    /** This function only calls whenever Refund Action is received.
     * If there is no RefundInfo on current intent, it will show info dialog with No Refund Intent for 2 seconds.
     * else -> it transforms refundInfo to JSON object to parse it easily. Then get ReferenceNo and BatchNo
     * Then it compares intent batch number with current Batch Number from database, if they are equal then start Void operation
     * else start refund operation. */
    private fun refundActionReceived() {
        if (intent.extras == null || intent.extras!!.getString("RefundInfo") == null) {
            callbackMessage(ResponseCode.ERROR)
        } else {
            val refundInfo = intent.extras!!.getString("RefundInfo")
            val json = JSONObject(refundInfo!!)
            val refNo = json.getString("RefNo")
            transactionViewModel.refNo = refNo
            val refAmount = json.getString("Amount")
            val amount = refAmount.toInt()
            cardViewModel.setAmount(amount)
            val transactionBatchNo = json.getInt("BatchNo")
            val currentBatchNo = batchViewModel.getBatchNo()
            if (transactionBatchNo == currentBatchNo) { // GIB Void Operation
                cardViewModel.setTransactionCode(TransactionCode.VOID.type)
                readCard()
                postTxnFragment.voidAfterReadCard(true)
            } else{ // GIB Refund Operation (because refund request is received after closing batch
                val authCode = json.getString("AuthCode")
                val tranDate = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault())
                val cardNo = json.getString("CardNo")
                val refundBundle = Bundle()
                refundBundle.putString(ExtraKeys.ORG_AMOUNT.name, refAmount.toString())
                refundBundle.putString(ExtraKeys.REFUND_AMOUNT.name, refAmount.toString())
                refundBundle.putString(ExtraKeys.TRAN_DATE.name, tranDate.toString())
                refundBundle.putString(ExtraKeys.REF_NO.name, refNo)
                refundBundle.putString(ExtraKeys.AUTH_CODE.name, authCode)
                refundBundle.putString(ExtraKeys.CARD_NO.name, cardNo)
                cardViewModel.setTransactionCode(TransactionCode.MATCHED_REFUND.type)
                readCard()
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
    fun showConfirmationDialog(
        type: InfoDialog.InfoType,
        title: String,
        info: String,
        buttons: InfoDialog.InfoDialogButtons,
        arg: Int,
        listener: InfoDialogListener
    ): InfoDialog? {
        //created a dialog with InfoDialog newInstance method
        val dialog = InfoDialog.newInstance(type, title, info, buttons, arg, listener)
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
    fun showInfoDialog(
        type: InfoDialog.InfoType, text: String, isCancelable: Boolean
    ): InfoDialog? {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(supportFragmentManager, "")
        return fragment
    }


    /**
     * This function only works in installation, it calls setConfig and setCLConfig
     * It also called from onCardServiceConnected method of Card Service Library, if Configs couldn't set in first_run
     * (it is checked from sharedPreferences), again it setConfigurations, else do nothing.
     */
    fun setEMVConfiguration(fromCardService: Boolean) {
        val sharedPreference = getSharedPreferences("myprefs", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        val firstTimeBoolean = sharedPreference.getBoolean("FIRST_RUN", false)
        if (!firstTimeBoolean) {
            if (fromCardService) {
                Toast.makeText(
                    applicationContext,
                    "Config Dosyaları Güncellendi, Lütfen Banka Kurulumu Yapınız",
                    Toast.LENGTH_LONG
                ).show()
            }
            setConfig()
            setCLConfig()
            editor.putBoolean("FIRST_RUN", true)
            Log.d("setEMVConfiguration", "ok")
            editor.apply()
        }
    }

    /**
     * It sets Config.xml
     */
    private fun setConfig() {
        try {
            val xmlStream = applicationContext.assets.open("emv_config.xml")
            val r = BufferedReader(InputStreamReader(xmlStream))
            val total = StringBuilder()
            var line: String? = r.readLine()
            while (line != null) {
                Log.d("emv_config", "conf line: $line")
                total.append(line).append('\n')
                line = r.readLine()
            }
            val setConfigResult = cardServiceBinding!!.setEMVConfiguration(total.toString())
            Toast.makeText(
                applicationContext,
                "setEMVConfiguration res=$setConfigResult",
                Toast.LENGTH_SHORT
            ).show()
            Log.i("emv_config", "setEMVConfiguration: $setConfigResult")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * It sets cl_config.xml
     */
    private fun setCLConfig() {
        try {
            val xmlCLStream = applicationContext.assets.open("emv_cl_config.xml")
            val rCL = BufferedReader(InputStreamReader(xmlCLStream))
            val totalCL = java.lang.StringBuilder()
            var line: String? = rCL.readLine()
            while (line != null) {
                Log.d("emv_config", "conf line: $line")
                totalCL.append(line).append('\n')
                line = rCL.readLine()
            }
            val setCLConfigResult: Int =
                cardServiceBinding!!.setEMVCLConfiguration(totalCL.toString())
            Toast.makeText(
                applicationContext,
                "setEMVCLConfiguration res=$setCLConfigResult", Toast.LENGTH_SHORT
            ).show()
            Log.i("emv_config", "setEMVCLConfiguration: $setCLConfigResult")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * It passes responseCode as a callBack message with respect to given parameter
     */
    fun callbackMessage(responseCode: ResponseCode) {
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)

        when (responseCode) { //TODO error message
            ResponseCode.OFFLINE_DECLINE -> {
                val infoDialog = showInfoDialog(InfoDialog.InfoType.Warning, "Card Numbers are Mismatching", true)
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog!!.dismiss()
                    setResult(intent)
                }, 2000)
            }
            ResponseCode.CANCELED -> {
                val infoDialog = showInfoDialog(InfoDialog.InfoType.Warning, "İşlem İptal Edildi", false)
                if (cardViewModel.getTimeOut()){
                    infoDialog?.update(InfoDialog.InfoType.Warning,"İşlem Zaman Aşımına Uğradı")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    infoDialog!!.dismiss()
                    setResult(intent)
                }, 2000)
            }
            else -> {
                setResult(intent)
            }
        }
    }

    /**
     * This function is for printing.
     */
    fun print(printText: String?) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(applicationContext))
    }
}
