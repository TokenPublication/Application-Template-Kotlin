package com.tokeninc.sardis.application_template

import android.annotation.SuppressLint
import android.content.ContentValues
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
import com.tokeninc.sardis.application_template.*
import com.tokeninc.sardis.application_template.data.database.AppTempDB
import com.tokeninc.sardis.application_template.databinding.ActivityMainBinding
import com.tokeninc.sardis.application_template.enums.*
import com.tokeninc.sardis.application_template.ui.*
import com.tokeninc.sardis.application_template.ui.activation.ActivationViewModel
import com.tokeninc.sardis.application_template.ui.activation.SettingsFragment
import com.tokeninc.sardis.application_template.ui.posttxn.PostTxnFragment
import com.tokeninc.sardis.application_template.ui.posttxn.batch.BatchViewModel
import com.tokeninc.sardis.application_template.ui.posttxn.refund.RefundFragment
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
 *  all operations are run here because this application is designed as a single-activity architecture
 * It's @AndroidEntryPoint because, we get ViewModel inside of class,
 */
@AndroidEntryPoint
class MainActivity : TimeOutActivity() {

    //initializing bindings
    private lateinit var cardServiceBinding: CardServiceBinding

    //initializing View Models and Fragments
    private lateinit var activationViewModel : ActivationViewModel
    private lateinit var batchViewModel : BatchViewModel
    private lateinit var transactionViewModel : TransactionViewModel
    private lateinit var cardViewModel: CardViewModel
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var saleFragment : SaleFragment
    private lateinit var triggerFragment: TriggerFragment
    private lateinit var postTxnFragment: PostTxnFragment
    private lateinit var refundFragment: RefundFragment

    //initializing other variables
    var transactionCode: Int = 0
    var amount: Int = 0 //this is for holding amount
    var infoDialog: InfoDialog? = null

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
    and the appname in manifest file will be 1000TR or 400TR.
     */
    private fun buildConfigs(){
        if (BuildConfig.FLAVOR.equals("TR1000")) {
            Log.d("TR1000 APP","Application Template for 1000TR")
        }
        if(BuildConfig.FLAVOR.equals("TR400")) {
            Log.d("TR400 APP","Application Template for 400TR")
        }
    }

    /**
     * This is for starting the activity, databases which are created with respect to context and viewModels which are created
     * wrt to databases and services are created here. After that, some functions are called with respect to action of the current intent
     */
    private fun startActivity(){
        buildConfigs()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        AppTempDB.getInstance(this)

        //get ViewModels from Dagger-Hilt easily, but to make this easy you need to implement each dependency clearly
        val getActivationViewModel : ActivationViewModel by viewModels()
        activationViewModel = getActivationViewModel
        val getBatchViewModel : BatchViewModel by viewModels()
        batchViewModel = getBatchViewModel
        val getTransactionViewModel : TransactionViewModel by viewModels()
        transactionViewModel = getTransactionViewModel
        val getCardViewModel : CardViewModel by viewModels()
        cardViewModel = getCardViewModel

        saleFragment = SaleFragment(transactionViewModel,this,activationViewModel,batchViewModel,cardViewModel)
        settingsFragment = SettingsFragment(this, activationViewModel, intent)
        triggerFragment = TriggerFragment(this)
        refundFragment = RefundFragment(this, cardViewModel, transactionViewModel, batchViewModel)
        postTxnFragment = PostTxnFragment(this,transactionViewModel,refundFragment,batchViewModel,cardViewModel)
        //cardServiceBinding = CardServiceBinding(this, this)
        observeTIDMID()

        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR


        actionChanged(intent.action)
    }

    /**
     * This function calls corresponding functions whenever an action of intent is changed
     */
    private fun actionChanged(action: String?){
        when (action){
            getString(R.string.PosTxn_Action) ->  {
                replaceFragment(postTxnFragment)
            }
            getString(R.string.Sale_Action) ->  {
                saleActionReceived()
            }
            getString(R.string.Settings_Action) ->  replaceFragment(settingsFragment)
            getString(R.string.BatchClose_Action) ->  {
                if (transactionViewModel.allTransactions().isNullOrEmpty()){ //if it is empty just show no transaction dialog
                    callbackMessage(ResponseCode.ERROR)
                }else{ //else implementing batch closing and finish that activity
                    postTxnFragment.startBatchClose()
                }

            }
            getString(R.string.Parameter_Action) ->  replaceFragment(triggerFragment)
            getString(R.string.Refund_Action) ->  {
                refundActionReceived()
            }
            else ->  replaceFragment(settingsFragment)
        }
    }

    private fun saleActionReceived(){
        transactionCode = TransactionCode.SALE.type
        amount = intent.extras!!.getInt("Amount")
        cardViewModel.setAmount(amount)
        saleFragment.setAmount(amount)

        val isGIB = (this.applicationContext as AppTemp).getCurrentDeviceMode().equals(
            DeviceInfo.PosModeEnum.GIB.name)
        val bundle = intent.extras
        val cardData: String? = bundle?.getString("CardData")
        // when sale operation is called from pgw which has multi bank and app temp is the only issuer of this card
        if (!isGIB && cardData != null && !cardData.equals("CardData") && !cardData.equals(" ")) {
            replaceFragment(saleFragment)
            saleFragment.doSale(cardData)
        }

        if (intent.extras != null){
            val cardReadType = intent.extras!!.getInt("CardReadType")
            if(cardReadType == CardReadType.ICC.type){
                cardViewModel.setGibSale(true)
                connectCardService()
                saleFragment.startSaleAfterConnected()
            } else{
                replaceFragment(saleFragment)
            }
        } else{
            replaceFragment(saleFragment)
        }
    }

    fun connectCardService(){
        var isCancelled = false
        //first create an Info dialog for processing, when this is showing a 10 seconds timer starts
        val timer: CountDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() { //when it's finished, (after 10 seconds)
                isCancelled = true //make isCancelled true (because cardService couldn't be connected)
                showInfoDialog(InfoDialog.InfoType.Declined, "Connect Failed", false)
                Handler(Looper.getMainLooper()).postDelayed({
                    if (infoDialog != null) {
                        infoDialog!!.dismiss()
                        finish()
                    }
                }, 2000)
            }
        }
        timer.start()
        cardViewModel.initializeCardServiceBinding(this)
        cardViewModel.getCardServiceConnected().observe(this) { isConnected ->
            // cardService is connected before 10 seconds (which is the limit of the timer)
            if (isConnected && !isCancelled) {
                timer.cancel() // stop timer
                Handler(Looper.getMainLooper()).postDelayed({
                    cardViewModel.readCard() //reads the Card
                }, 500) //to show card Service is connected.
            }
        }
        // when read card is cancelled (on back pressed) finish the main activity
        cardViewModel.getCallBackMessage().observe(this){responseCode ->
            if (responseCode == ResponseCode.CANCELED){ //if it is canceled
                Log.d("Transaction Code :",     "Canceled")
                cardViewModel.setCallBackMessage(ResponseCode.SUCCESS) //to ensure not store it always canceled.
                finish()
            }
        }
    }

    /** This function only calls whenever Refund Action is received.
     * If there is no RefundInfo on current intent, it will show info dialog with No Refund Intent for 2 seconds.
     * else -> it transforms refundInfo to JSON object to parse it easily. Then get ReferenceNo and BatchNo
     * Then it compares intent batch number with current Batch Number from database, if they are equal then start Void operation
     * else start refund operation. */

    private fun refundActionReceived(){
        if (intent.extras == null || intent.extras!!.getString("RefundInfo") == null){
            callbackMessage(ResponseCode.ERROR)
        } else{
            cardViewModel.setGibRefund(true) //update GibRefund
            val refundInfo = intent.extras!!.getString("RefundInfo")
            transactionViewModel.refundInfo = refundInfo!!
            val json = JSONObject(refundInfo)
            val refNo = json.getString("RefNo")
            transactionViewModel.refNo = refNo
            val refAmount = json.getString("Amount")
            amount = refAmount.toInt()
            cardViewModel.setAmount(amount)
            val batchNo = json.getInt("BatchNo")
            if (batchNo == batchViewModel.batchNo){ //void
                transactionCode = TransactionCode.VOID.type
                cardViewModel.setTransactionCode(transactionCode)
                connectCardService()
                postTxnFragment.startVoidAfterConnected()
            } else{
                val authCode = json.getString("AuthCode")
                val tranDate = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault())
                val cardNo = json.getString("CardNo")
                val extraContents = ContentValues()
                extraContents.put(ExtraKeys.ORG_AMOUNT.name, refAmount.toString())
                extraContents.put(ExtraKeys.REFUND_AMOUNT.name, refAmount.toString())
                extraContents.put(ExtraKeys.TRAN_DATE.name, tranDate.toString())
                extraContents.put(ExtraKeys.REF_NO.name,refNo)
                extraContents.put(ExtraKeys.AUTH_CODE.name,authCode)
                extraContents.put(ExtraKeys.CARD_NO.name,cardNo)
                connectCardService()
                refundFragment.gibRefund(extraContents)
            }
        }
    }

    /**
     * This method is for showing callBack message, it finishes the activity with given intent.
     * The activity needs to be finished else Postman surmises transaction is continuing and Transaction couldn't end.
     */
    fun setResult(resultIntent: Intent){
        setResult(RESULT_OK, resultIntent) //responseCode yeterli
        finish()
    }

    /**
     * This is for showing infoDialog
     * @param infoDialog info dialog that will be showed
     */
    fun showDialog(infoDialog: InfoDialog){
        infoDialog.show(supportFragmentManager,"")
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
    ):InfoDialog? {
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
    fun addFragment(fragment: Fragment)
    {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun popFragment(){
        supportFragmentManager.popBackStack()
    }

    /**
     * This is for replacing the fragment without adding it to the stack
     */
    fun replaceFragment( fragment: Fragment)
    {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container,fragment) //replacing fragment
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

    /**
     * This shows infoDialog
     */
    fun showInfoDialog(type: InfoDialog.InfoType, text: String, isCancelable: Boolean
    ): InfoDialog? {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(supportFragmentManager, "")
        return fragment
    }


    /**
    override fun onCardServiceConnected() {
    setEMVConfiguration() //TODO doğru yere taşınacak
    }*/

    /**
     * This function only works in installation, it calls setConfig and setCLConfig
     */
    private fun setEMVConfiguration() {
        val sharedPreference = getSharedPreferences("myprefs",Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        val firstTimeBoolean = sharedPreference.getBoolean("FIRST_RUN",false)
        if (!firstTimeBoolean){
            setConfig()
            setCLConfig()
            editor.putBoolean("FIRST_RUN",true)
            editor.apply()
        }
    }

    /**
     * It set Config.xml
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
            val setConfigResult = cardServiceBinding.setEMVConfiguration(total.toString())
            Toast.makeText(
                applicationContext,
                "setEMVConfiguration res=$setConfigResult",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("emv_config", "setEMVConfiguration: $setConfigResult")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * It set cl_config.xml
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
            val setCLConfigResult: Int = cardServiceBinding.setEMVCLConfiguration(totalCL.toString())
            Toast.makeText(
                applicationContext,
                "setEMVCLConfiguration res=$setCLConfigResult", Toast.LENGTH_SHORT
            ).show()
            Log.d("emv_config", "setEMVCLConfiguration: $setCLConfigResult")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * It passes responseCode as a callBack message with respect to given parameter
     */
    fun callbackMessage(responseCode: ResponseCode){
        val intent = Intent()
        val bundle = Bundle()
        bundle.putInt("ResponseCode", responseCode.ordinal)
        intent.putExtras(bundle)
        if (responseCode == ResponseCode.OFFLINE_DECLINE){
            val infoDialog = showInfoDialog(InfoDialog.InfoType.Warning, "Card Numbers are Mismatching", true)
            Handler(Looper.getMainLooper()).postDelayed({
                infoDialog!!.dismiss()
                setResult(intent)
            }, 2000)
        } else{
            setResult(intent)
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

    //TODO redisgn them
    //This is for holding MID and TID, Because this values are LiveData,
    // instead of writing this functions everywhere it is used call it from mainActivity.
    var currentMID: String? = ""
    var currentTID: String? = ""

    /**
     * It holds the last updated values in there
     */
    private fun setMID(MerchantID: String?) {
        currentMID = MerchantID
    }

    private fun setTID(TerminalID: String?) {
        currentTID = TerminalID
    }
    /**
     * After TID and MID is changed, it is called from there and hold data for it. It also called everytime whenever MainActivity is created.
     */
    fun observeTIDMID(){
        activationViewModel.merchantID.observe(this){
            if (it != null)
                setMID(it)
        }
        activationViewModel.terminalID.observe(this){
            if (it != null)
                setTID(it)
        }
    }
}