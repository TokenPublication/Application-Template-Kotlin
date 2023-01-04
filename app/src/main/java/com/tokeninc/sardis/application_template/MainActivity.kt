package com.tokeninc.sardis.application_template

import MenuItem
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialog.InfoDialogButtons
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.timeoutmanager.TimeOutActivity
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.database.activation.ActivationDB
import com.tokeninc.sardis.application_template.database.batch.BatchDB
import com.tokeninc.sardis.application_template.database.transaction.TransactionDB
import com.tokeninc.sardis.application_template.databinding.ActivityMainBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.enums.CardReadType
import com.tokeninc.sardis.application_template.enums.CardServiceResult
import com.tokeninc.sardis.application_template.helpers.printHelpers.PrintServiceBinding
import com.tokeninc.sardis.application_template.viewmodels.ActivationVMFactory
import com.tokeninc.sardis.application_template.viewmodels.ActivationViewModel
import com.tokeninc.sardis.application_template.viewmodels.TransactionVMFactory
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : TimeOutActivity(), InfoDialogListener, CardServiceListener {


    //menu items is mutable list which we can add and delete
    private val menuItems = mutableListOf<IListMenuItem>()
    var amount: Int = 0
    private val inputList = mutableListOf<CustomInputFormat>()
    private var cardServiceBinding: CardServiceBinding? = null
    private var card: ICCCard? = null
    private var printService: PrintServiceBinding? = null
    var actDB: ActivationDB? = null
    var transactionDB: TransactionDB? = null
    private val transactionService = TransactionService()
    private val postTxnFragment = PostTxnFragment()
    private lateinit var dummySaleFragment : DummySaleFragment
    private val settingsFragment = SettingsFragment()
    private val refundFragment = RefundFragment()
    private lateinit var transactionViewModel: TransactionViewModel
    lateinit var activationViewModel: ActivationViewModel
    var isVoid = false  //if readCard is for void operation it returns true from voidFragment
    var isSale = false  //if readCard is for sale operation it returns true from dummySaleFragment
    var isRefund = false //TODO TransactionCode a göre yap
    var isMatchedRefund = false //bunlara göre transactionCode yazdıracaksın.
    var isCashRefund = false
    var isInstallmentRefund = false
    var batchDB: BatchDB? = null

    // to continue where it was when the screen is rotating
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        startActivity()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity()
    }

    private fun startActivity(){
        val binding = ActivityMainBinding.inflate(layoutInflater)
        actDB = ActivationDB(this).getInstance(this) // TODO Egecan: Check not null
        transactionDB = TransactionDB(this).getInstance(this)
        batchDB = BatchDB(this).getInstance(this)
        transactionViewModel = ViewModelProvider(this,TransactionVMFactory(transactionDB!!))[TransactionViewModel::class.java]
        activationViewModel = ViewModelProvider(this,ActivationVMFactory(actDB!!))[ActivationViewModel::class.java]
        dummySaleFragment = DummySaleFragment(transactionViewModel)
        cardServiceBinding = CardServiceBinding(this, this)
        startTransactionService()
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        printService = PrintServiceBinding()
        //printService?.print(PrintHelper().PrintSuccess())
        val textFragment = TextFragment()
        //replaceFragment(textFragment)

        when (intent.action){
            getString(R.string.PosTxn_Action) ->  startPostTxnFragment(postTxnFragment)
            getString(R.string.Sale_Action) ->  startDummySaleFragment(dummySaleFragment)
            getString(R.string.Settings_Action) ->  startSettingsFragment(settingsFragment)
            getString(R.string.BatchClose_Action) ->  textFragment.setActionName(getString(R.string.BatchClose_Action))
            getString(R.string.Parameter_Action) ->  textFragment.setActionName(getString(R.string.Parameter_Action))
            getString(R.string.Refund_Action) ->  textFragment.setActionName(getString(R.string.Refund_Action))
            else -> startSettingsFragment(settingsFragment)
        }
    }

    fun showDialog(infoDialog: InfoDialog){
        infoDialog.show(supportFragmentManager,"")
    }

    private fun startPostTxnFragment(postTxnFragment: PostTxnFragment){
        postTxnFragment.mainActivity = this
        postTxnFragment.transactionViewModel = transactionViewModel
        postTxnFragment.transactionService = transactionService
        postTxnFragment.refundFragment = refundFragment
        replaceFragment(postTxnFragment)
    }

    private fun startTransactionService(){
        transactionService.mainActivity = this
        transactionService.transactionDB = transactionDB
        transactionService.transactionViewModel = transactionViewModel
    }


    private fun startDummySaleFragment(dummySaleFragment: DummySaleFragment){
        amount = intent.extras!!.getInt("Amount")
        dummySaleFragment.amount = amount
        dummySaleFragment.activityContext = this@MainActivity
        dummySaleFragment.getNewBundle(bundleOf())
        dummySaleFragment.getNewIntent(Intent())
        dummySaleFragment.transactionService = transactionService
        transactionService.transactionDB = transactionDB
        dummySaleFragment.mainActivity = this
        dummySaleFragment.saleIntent = Intent(getString(R.string.Sale_Action))
        dummySaleFragment.saleBundle = Intent(getString(R.string.Sale_Action)).extras
        dummySaleFragment.activationViewModel = activationViewModel
        dummySaleFragment.transactionDB = transactionDB
        dummySaleFragment.batchDB = batchDB
        replaceFragment(dummySaleFragment)
    }

    private fun startSettingsFragment(settingsFragment: SettingsFragment){
        settingsFragment.resultIntent = Intent()
        settingsFragment.mainActivity = this
        settingsFragment.activationViewModel = activationViewModel
        settingsFragment._context = this@MainActivity
        replaceFragment(settingsFragment)
    }

    /**
     * This is for dummySaleFragmen's onSaleResponseRetrieved method
     * Because we use setResult method on Activities, we call this method in there.
     */
    fun dummySetResult(resultIntent: Intent){
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    protected fun addCustomInputFormat(){
        inputList.add(
            CustomInputFormat(
                "Card Number",
                EditTextInputType.CreditCardNumber,
                null,
                "Invalid card number!",
                null
            )
        )
        inputList.add(
            CustomInputFormat(
                "Expire Date",
                EditTextInputType.ExpiryDate,
                null,
                null,
                null
            )
        )
        inputList.add(CustomInputFormat("CVV", EditTextInputType.CVV,
            null, null, null))

        val input = CustomInputFormat("IP", EditTextInputType.IpAddress, null, null, null)
        input.text = "123.456.789.1"

    }

    /**
     * Shows a dialog to the user which asks for a confirmation.
     * Dialog will be dismissed automatically when user taps on to confirm/cancel button.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, String, InfoDialog.InfoDialogButtons, int, InfoDialogListener)}
     */
    private fun showConfirmationDialog(
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
     * Shows a dialog to the user which informs the user about current progress.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, boolean)}
     * Dialog can dismissed by calling .dismiss() method of the fragment instance returned from this method.
     */
    private fun showInfoDialog(
        type: InfoDialog.InfoType,
        text: String,
        isCancelable: Boolean
    ): InfoDialog? {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(supportFragmentManager, "")
        return fragment
    }

    /**
     * preparing some data to show in fragment, first create a list for sublist with menuitems
     * then add that sublist and currently created menuItems to menuItems mutable list
     */
    fun prepareData() {
        val subList1 = mutableListOf<IListMenuItem>()// Creating a mutable list for your sub menu items
        /* Your Sub List Items*/
        /* Your Sub List Items*/
        // adding some menu items to sublist
        subList1.add( MenuItem( "MenuItem 1", { menuItem: IListMenuItem? ->
            Toast.makeText(this, "Sub Menu 1", Toast.LENGTH_LONG).show() } )
        )

        subList1.add(
            MenuItem(
                "MenuItem 2",
                { menuItem ->
                    Toast.makeText(this, "Sub Menu 2", Toast.LENGTH_LONG).show()
                },
            ))

        subList1.add(
            MenuItem(
                "MenuItem 3",
                { menuItem ->
                    Toast.makeText(this, "Sub Menu 3", Toast.LENGTH_LONG).show()
                },
            ))

        // The menu item in to the List Menu Fragment
        // adding subList1 which we added before
        menuItems.add(
            MenuItem("Sub Menu (Error Alıyor)", subList1, null) )

        //adding some other menuitems which we defined there not before
        //a menu item which you call a confirmation dialog
        menuItems.add(MenuItem("Warning", { menuItem: IListMenuItem? ->
                    Toast.makeText(this, "Menu Item 1", Toast.LENGTH_LONG).show()
            showConfirmationDialog(
                InfoDialog.InfoType.Warning,
                "Warning",
                "Confirmation: Warning",
                InfoDialogButtons.Both,
                99,
                this@MainActivity)
                } ) )
        // a menu item which you call an info dialog
        menuItems.add(
            MenuItem("Connecting", { menuItem: IListMenuItem? ->
                    Toast.makeText(this, "Menu Item 2", Toast.LENGTH_LONG).show()
                showInfoDialog(InfoDialog.InfoType.Connecting, "Connecting", true)
                } ) )

        menuItems.add(
            MenuItem("Menu Item 3",
                {
                    Toast.makeText(this@MainActivity, "Menu Item 3", Toast.LENGTH_LONG).show()
                } ) )
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

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * add fragment method is for simplifying adding fragments
     * with this method you won't waste your time with supporfragment manager methods.
     */
    fun addFragment(fragment: Fragment)
    {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun replaceFragment( fragment: Fragment)
    {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container,fragment) //replacing fragment
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

    protected fun removeFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.remove(fragment)
        ft.commit()
    }

    override fun confirmed(arg: Int) {
        if (arg == 99) {
            //  Do something else...
        }
        //else if (arg == ***) { Do something else... }
    }

    override fun canceled(arg: Int) {
        if (arg == 99) {
            //  Do something else...
        }
        //else if (arg == ***) { Do something else... }
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
            editor.commit()
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
            val setConfigResult = cardServiceBinding!!.setEMVConfiguration(total.toString())
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
            val setCLConfigResult: Int = cardServiceBinding!!.setEMVCLConfiguration(totalCL.toString())
            Toast.makeText(
                applicationContext,
                "setEMVCLConfiguration res=$setCLConfigResult", Toast.LENGTH_SHORT
            ).show()
            Log.d("emv_config", "setEMVCLConfiguration: $setCLConfigResult")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun readCard() {
        val obj = JSONObject()
        try {
            obj.put("forceOnline", 0)
            obj.put("zeroAmount", 1)
            obj.put("showAmount", if (isVoid) 0 else 1)
            obj.put("partialEMV", 1)
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
        cardServiceBinding = CardServiceBinding(this ,this )
        runBlocking(Dispatchers.IO) {
            GlobalScope.launch(Dispatchers.IO){
                cardServiceBinding!!.getCard(amount, 30, obj.toString())
            }.join()
        }

    }


    override fun onCardServiceConnected() {
        setEMVConfiguration()
    }

    override fun onCardDataReceived(cardData: String?) {
        try {
            val json = JSONObject(cardData)
            val type = json.getInt("mCardReadType")
            card = Gson().fromJson(cardData, ICCCard::class.java)
            if (card!!.resultCode == CardServiceResult.ERROR.resultCode()) {
            }
            if (card!!.resultCode == CardServiceResult.SUCCESS.resultCode()) {
                if (type == CardReadType.QrPay.type) {
                    //QrSale()
                    return
                }
                if (type == CardReadType.CLCard.type) {
                    card = Gson().fromJson(cardData, ICCCard::class.java)
                } else if (type == CardReadType.ICC.type) {
                    card = Gson().fromJson(cardData, ICCCard::class.java)
                } else if (type == CardReadType.ICC2MSR.type || type == CardReadType.MSR.type || type == CardReadType.KeyIn.type) {
                    //card = Gson().fromJson(cardData, ICCCard::class.java)
                    //cardServiceBinding!!.getOnlinePIN(amount, card?.cardNumber, 0x0A01, 0, 4, 8, 30)
                }
                if (isSale)
                    dummySaleFragment.prepareSaleMenu(card)
                else if (isVoid)
                    postTxnFragment.cardNumberReceived(card)
                else if (isRefund)
                    refundFragment.afterReadCard(card,isMatchedRefund,isInstallmentRefund,isCashRefund)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onPinReceived(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun onICCTakeOut() {
        TODO("Not yet implemented")
    }

}