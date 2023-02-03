package com.tokeninc.sardis.application_template.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.timeoutmanager.TimeOutActivity
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.*
import com.tokeninc.sardis.application_template.database.activation.ActivationDB
import com.tokeninc.sardis.application_template.database.batch.BatchDB
import com.tokeninc.sardis.application_template.database.slip.SlipDB
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB
import com.tokeninc.sardis.application_template.databinding.ActivityMainBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.*
import com.tokeninc.sardis.application_template.examples.ExampleActivity
import com.tokeninc.sardis.application_template.services.BatchCloseService
import com.tokeninc.sardis.application_template.services.TransactionService
import com.tokeninc.sardis.application_template.viewmodels.ActivationVMFactory
import com.tokeninc.sardis.application_template.viewmodels.ActivationViewModel
import com.tokeninc.sardis.application_template.viewmodels.TransactionVMFactory
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

/** This is the Main Activity class,
 *  all operations are run here because this application is designed as a single-activity architecture
 */
class MainActivity : TimeOutActivity(), CardServiceListener {


    //menu items is mutable list which we can add and delete
    var amount: Int = 0 //this is for holding amount
    //initializing bindings
    private lateinit var cardServiceBinding: CardServiceBinding
    //initializing databases
    private var actDB: ActivationDB? = null
    private var transactionDB: TransactionDB? = null
    private var batchDB: BatchDB? = null
    private var slipDB: SlipDB? = null //TODO no need to database just append string to batch database
    //initializing viewModels
    private lateinit var transactionViewModel: TransactionViewModel
    lateinit var activationViewModel: ActivationViewModel
    //initializing fragments
    private val transactionService = TransactionService()
    private val batchCloseService = BatchCloseService()
    private val postTxnFragment = PostTxnFragment()
    private val settingsFragment = SettingsFragment()
    private val refundFragment = RefundFragment()
    private val textFragment = TextFragment()
    private lateinit var dummySaleFragment : DummySaleFragment
    private val exampleActivity = ExampleActivity()

    //initializing other variables
    var transactionCode: Int = 0
    private var autoTransaction = false
    private var gibSale = false
    private var refundInfo: String? = null
    private var refNo: String? = null

    private var extraContents : ContentValues? = null

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
     * This is for starting the activity, databases which are created with respect to context and viewModels which are created
     * wrt to databases and services are created here. After that, some functions are called with respect to action of the current intent
     */
    private fun startActivity(){
        val binding = ActivityMainBinding.inflate(layoutInflater)
        actDB = ActivationDB(this).getInstance(this)
        transactionDB = TransactionDB(this).getInstance(this)
        batchDB = BatchDB(this).getInstance(this)
        slipDB = SlipDB(this).getInstance(this)
        transactionViewModel = ViewModelProvider(this,TransactionVMFactory(transactionDB!!))[TransactionViewModel::class.java]
        activationViewModel = ViewModelProvider(this,ActivationVMFactory(actDB!!))[ActivationViewModel::class.java]
        dummySaleFragment = DummySaleFragment(transactionViewModel)
        cardServiceBinding = CardServiceBinding(this, this)
        startTransactionService()
        startBatchService()
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        textFragment.mainActivity = this

        /*
        //this is for preventing some errors while using Gib with Postman
        val deviceInfo: DeviceInfo = DeviceInfo(this)
        deviceInfo.setBankParams(DeviceInfoBankParamsSetterHandler{ var1 ->
            if (var1) {
                Log.i("setBankParams", "Success")
            } else {
                Log.i("setBankParams", "Error")
            }
            deviceInfo.unbind()
        }, "12345", "12345678")
         */

        actionChanged(intent.action)
    }

    /**
     * This function calls corresponding functions whenever an action of intent is changed
     */
    private fun actionChanged(action: String?){
        when (action){
            getString(R.string.PosTxn_Action) ->  {
                startPostTxnFragment(postTxnFragment)
                replaceFragment(postTxnFragment)
            }
            getString(R.string.Sale_Action) ->  {
                transactionCode = TransactionCode.SALE.type
                startDummySaleFragment(dummySaleFragment)
                if (intent.extras != null){
                    val cardReadType = intent.extras!!.getInt("CardReadType")
                    if(cardReadType == CardReadType.ICC.type){
                        gibSale = true
                        readCard()
                    } else{
                        replaceFragment(dummySaleFragment)
                    }
                } else{
                    replaceFragment(dummySaleFragment)
                }
            }
            getString(R.string.Settings_Action) ->  startSettingsFragment(settingsFragment)
            getString(R.string.BatchClose_Action) ->  {
                if (transactionViewModel.getAllTransactions().isEmpty()){ //if it is empty just show no transaction dialog
                    callbackMessage(ResponseCode.ERROR)
                }else{ //else implementing batch closing and finish that activity
                    startPostTxnFragment(postTxnFragment)
                    postTxnFragment.startBatchClose()
                }

            }
            getString(R.string.Parameter_Action) ->  textFragment.setActionName(getString(R.string.Parameter_Action))
            getString(R.string.Refund_Action) ->  {
                startPostTxnFragment(postTxnFragment)
                refundActionReceived()
            }
            else ->  startSettingsFragment(settingsFragment) //textFragment.setActionName("Main")
        }
    }

    //batchclose biterken ui güncelleyince 2 kez geri basınca düz ui geliyor
    //refundda da 2 kez geri basmak lazım cancelled için ?
    //voidde kart okuturken tutar çıkmıyor doğru mu ?


    /** This function only calls whenever Refund Action is received.
     * If there is no RefundInfo on current intent, it will show info dialog with No Refund Intent for 2 seconds.
     * else -> it transforms refundInfo to JSON object to parse it easily. Then get ReferenceNo and BatchNo
     * Then it compares intent batch number with current Batch Number from database, if they are equal then start Void operation
     * else start refund operation. */
    private fun refundActionReceived(){
        if (intent.extras == null || intent.extras!!.getString("RefundInfo") == null){
            callbackMessage(ResponseCode.ERROR)
        } else{
            autoTransaction = true
            refundInfo = intent.extras!!.getString("RefundInfo")
            val json = JSONObject(refundInfo!!)
            refNo = json.getString("RefNo")
            val refAmount = json.getString("Amount")
            amount = refAmount.toInt()
            val batchNo = json.getInt("BatchNo")
            if (batchNo == batchDB!!.getBatchNo()){ //void
                transactionCode = TransactionCode.VOID.type
                readCard()
            } else{
                postTxnFragment.startRefundFragment()
                val authCode = json.getString("AuthCode")
                val installmentCount = json.getString("InstCount")
                val tranDate = SimpleDateFormat("dd-MM-yy HH:mm:ss", Locale.getDefault())
                val cardNo = json.getString("CardNo")
                extraContents = ContentValues()
                extraContents!!.put(ExtraKeys.ORG_AMOUNT.name, refAmount.toString())
                extraContents!!.put(ExtraKeys.REFUND_AMOUNT.name, refAmount.toString())
                extraContents!!.put(ExtraKeys.TRAN_DATE.name, tranDate.toString())
                extraContents!!.put(ExtraKeys.REF_NO.name,refNo)
                extraContents!!.put(ExtraKeys.AUTH_CODE.name,authCode)
                extraContents!!.put(ExtraKeys.CARD_NO.name,cardNo)
                if (installmentCount.toInt() > 0){
                    transactionCode = TransactionCode.INSTALLMENT_REFUND.type
                    extraContents!!.put(ExtraKeys.INST_COUNT.name, installmentCount)
                }
                else{
                    transactionCode = TransactionCode.MATCHED_REFUND.type
                }
                readCard()
            }
        }
    }

    /**
     * This is for showing infoDialog
     * @param infoDialog info dialog that will be showed
     */
    fun showDialog(infoDialog: InfoDialog){
        infoDialog.show(supportFragmentManager,"")
    }

    /**
     * This function is setting a variable in example activity and start that activity with intents
     * example activity class is for showing some examples on the device it can be deleted by developer
     */
    fun startExampleActivity(){ //it can be deleted
        exampleActivity.setter(this@MainActivity)
        startActivity(Intent(this@MainActivity, exampleActivity::class.java))
    }

    /**
     * This function is setting some variables in fragment
     */
    private fun startPostTxnFragment(postTxnFragment: PostTxnFragment){
        postTxnFragment.setter(this,transactionViewModel,transactionService,refundFragment,batchCloseService,slipDB!!)
    }

    /**
     * This is for initializing some variables in Transaction Service
     */
    private fun startTransactionService(){
        transactionService.setter(this,batchDB!!,transactionViewModel)
    }

    /**
     * This is for initializing some variables in Batch Service
     */
    private fun startBatchService(){
        batchCloseService.setter(this,batchDB!!,transactionViewModel,slipDB!!)
    }

    /** This function is getting amount from intent and pass that amount and other variables to fragment.
     * Then replace that fragment to current view.
     */
    private fun startDummySaleFragment(dummySaleFragment: DummySaleFragment){
        amount = intent.extras!!.getInt("Amount")
        dummySaleFragment.setter(this, bundleOf(),Intent(),activationViewModel,batchDB!!, transactionService,amount)
    }

    /**
     * This function sets some variables in fragment and then replace that fragment.
     */
    private fun startSettingsFragment(settingsFragment: SettingsFragment){
        settingsFragment.setter(this,activationViewModel,Intent())
        replaceFragment(settingsFragment)
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
     * This function only works in installation, it calls setConfig and setCLConfig
     */
    private fun setEMVConfiguration () {
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
     * It reads
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
     * This reads the card
     */
    fun readCard() {
        val obj = JSONObject()
        try {
            obj.put("forceOnline", 0)
            obj.put("zeroAmount", 1)
            obj.put("showAmount", if (transactionCode == TransactionCode.VOID.type) 0 else 1) //amountu göstermiyor voidse
            obj.put("partialEMV", 1)
            if (gibSale)
                obj.put("showCardScreen", 0)
            // TODO Developer: Check from Allowed Operations Parameter
            val isManEntryAllowed = true
            val isCVVAskedOnMoto = true
            val isFallbackAllowed = true
            val isQrAllowed = true
            obj.put("keyIn", if (isManEntryAllowed) 1 else 0)
            obj.put("askCVV", if (isCVVAskedOnMoto) 1 else 0)
            obj.put("fallback", if (isFallbackAllowed) 1 else 0)
            obj.put("qrPay", if (isQrAllowed) 1 else 0)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        //runBlocking(Dispatchers.IO) {
            //GlobalScope.launch(Dispatchers.IO){
        cardServiceBinding.getCard(amount, 30, obj.toString())
            //}.join()
       // }

    }


    override fun onCardServiceConnected() {
        setEMVConfiguration()
    }

    /** This method is implemented after card data is received. It makes card data JSON parse easily.
     * After that get card and cardReadType from json. If the resultCode of the card is success make some operations
     * with respect to card read type. If transactionCode is sale and card read type ICC,
     * Sale & instalment Sale menu will be opened after reading the card, if it is Contactless Card this menu won't be opened.
     * After checking card type, it checks transaction Code, if it is VOID ->
     * first check whether this VOID operation is manual or automatically from GİB (with intents)
     * if it is from Gib, it gets the transaction from Reference number, if card and card data from that transaction is matching
     * it continues the void operation, else it warns the user.
     * If transaction is Refund it opens refund menu.
     */
    override fun onCardDataReceived(cardData: String?) {
        try {
            val json = JSONObject(cardData!!)
            var card: ICCCard = Gson().fromJson(cardData, ICCCard::class.java)
            if (card.resultCode == CardServiceResult.USER_CANCELLED.resultCode()) { //if user pressed back button in GiB operation
                Log.d("CardDataReceived","Card Result Code: User Cancelled")
                callbackMessage(ResponseCode.CANCELED)
            }
            if (card.resultCode == CardServiceResult.ERROR_TIMEOUT.resultCode()) { //if there is a timeout
                Log.d("CardDataReceived","Card Result Code: TIMEOUT")
                callbackMessage(ResponseCode.CANCELED)
            }
            val type = json.getInt("mCardReadType")
            if (card.resultCode == CardServiceResult.ERROR.resultCode()) {
                Log.d("CardDataReceived","Card Result Code: ERROR")
            }
            if (card.resultCode == CardServiceResult.SUCCESS.resultCode()) {
                when (type) {
                    CardReadType.QrPay.type -> {
                        //QrSale()
                        return
                    }
                    CardReadType.CLCard.type -> {
                        card = Gson().fromJson(cardData, ICCCard::class.java)
                        if (transactionCode == TransactionCode.SALE.type && !gibSale){
                            transactionCode = 0
                            dummySaleFragment.card = card
                            textFragment.setActionName("")
                            replaceFragment(textFragment)
                            dummySaleFragment.doSale()
                        }
                    }
                    CardReadType.ICC.type -> {
                        card = Gson().fromJson(cardData, ICCCard::class.java)
                        if (transactionCode == TransactionCode.SALE.type && !gibSale){
                            dummySaleFragment.prepareSaleMenu(card)
                        }
                    }
                    CardReadType.ICC2MSR.type, CardReadType.MSR.type, CardReadType.KeyIn.type -> {
                        //card = Gson().fromJson(cardData, ICCCard::class.java)
                        //cardServiceBinding!!.getOnlinePIN(amount, card?.cardNumber, 0x0A01, 0, 4, 8, 30)
                    }
                }
                if (transactionCode == TransactionCode.VOID.type){
                    if (autoTransaction) {
                        autoTransaction = false
                        transactionCode = 0
                        val transactionList: List<ContentValues?> = transactionDB!!.getTransactionsByRefNo(refNo!!)
                        val transaction = transactionList[0] //2 tane geldi??
                        Log.d("Refund Info", "Satış İptali: $transaction")
                        if (card.mCardNumber == transaction!!.getAsString(TransactionCol.Col_PAN.name)) {
                            postTxnFragment.card = card
                            postTxnFragment.voidOperation(transaction)
                        } else {
                            callbackMessage(ResponseCode.OFFLINE_DECLINE)
                        }
                    }
                     else {
                        postTxnFragment.cardNumberReceived(card)
                    }
                }
                else if (gibSale){
                    gibSale = false
                    transactionCode = 0
                    dummySaleFragment.card = card
                    dummySaleFragment.doSale()
                }
                else if (transactionCode == TransactionCode.MATCHED_REFUND.type || transactionCode == TransactionCode.INSTALLMENT_REFUND.type || transactionCode == TransactionCode.CASH_REFUND.type){
                    if (autoTransaction){
                        autoTransaction = false
                        if (extraContents!!.getAsString(ExtraKeys.CARD_NO.name).equals(card.mCardNumber))
                            refundFragment.afterReadCard(card,transactionCode,extraContents)
                        else
                            callbackMessage(ResponseCode.OFFLINE_DECLINE)
                    } else
                        refundFragment.afterReadCard(card,transactionCode,null)
                }

            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * It passes responseCode as a callBack message with respect to given parameter
     */
    private fun callbackMessage(responseCode: ResponseCode){
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

    override fun onPinReceived(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onICCTakeOut() {
        TODO("Not yet implemented")
    }



}