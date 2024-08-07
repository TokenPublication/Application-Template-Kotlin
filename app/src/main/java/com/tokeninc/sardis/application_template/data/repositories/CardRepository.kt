package com.tokeninc.sardis.application_template.data.repositories
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.data.model.card.CardServiceResult
import com.tokeninc.sardis.application_template.data.model.card.ICCCard
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.data.model.type.CardReadType
import com.tokeninc.sardis.application_template.data.model.type.EmvProcessType
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * This class is for managing data related card operations
 * It implements CardServiceListener interface's methods which are card service binding lib's methods.
 */
class CardRepository @Inject constructor() :
    CardServiceListener {

    private var amount = 0
    private var transactionCode = 0

    private var callBackMessage = MutableLiveData<ResponseCode>()
    private lateinit var mainActivity: AppCompatActivity
    fun getCallBackMessage(): LiveData<ResponseCode> {
        return callBackMessage
    }
    private fun setCallBackMessage(callBackMessage_: ResponseCode){
        callBackMessage.value = callBackMessage_
    }

    private var isCardServiceConnected = MutableLiveData(false)
    fun getCardServiceConnected(): LiveData<Boolean> {
        return isCardServiceConnected
    }

    private var mutableCardData =  MutableLiveData<ICCCard>()
    fun getCardData(): LiveData<ICCCard> {
        return mutableCardData
    }
    fun setCard(){
        mutableCardData = MutableLiveData<ICCCard>()
    }

    //These variables should only for storing the operation's result and intents' responses, because they won't be used
    //for UI updating they don't have to be a LiveData
    var gibSale = false
    private var isApprove = false //this is a flag for checking whether it is ICC sale (for implementing continue emv)
    private var takePin = false

    private var cardServiceBinding: CardServiceBinding? = null

    fun cardServiceBinder(activity: AppCompatActivity) {
        Log.i("cardLogs","enter cardServiceBinder")
        mainActivity = activity
        cardServiceBinding = CardServiceBinding(activity, this)
    }

    /** It is called when the main activity is finished (destroyed), because this repository doesn't end with the main Activity.
     * It only initializes the variables again, with this method, most of the controlling functions to ensure that
     * keep variables again at an initial point won't be needed.
     */
    fun onDestroyed(){
        mutableCardData =  MutableLiveData<ICCCard>()
        transactionCode = 0
        amount = 0
        callBackMessage = MutableLiveData<ResponseCode>()
        isCardServiceConnected = MutableLiveData(false)
        gibSale = false
        isApprove = false
        takePin = false
    }

    /**
     * This reads the card
     */
    fun readCard(amount: Int, transactionCode: Int) {
        val obj = JSONObject()
        try {
            this.amount = amount
            this.transactionCode = transactionCode
            if (!isApprove) { //if it is not the second readCard (reading ICC card for sale)
                // in sale and void emv process should be EmvProcessType.READ_CARD, for refunds it should be EmvProcessType.FULL_EMV
                obj.put("forceOnline", 1)
                obj.put("zeroAmount", 0)
                val isVoid = transactionCode == TransactionCode.VOID.type
                val isSale = transactionCode == TransactionCode.SALE.type
                obj.put("emvProcessType", if (isVoid || isSale) EmvProcessType.READ_CARD.ordinal else EmvProcessType.FULL_EMV.ordinal)
                obj.put("showAmount", if (isVoid) 0 else 1)
                obj.put("showCardScreen", if (gibSale) 0 else 1)
                getCard(amount,obj.toString()) // arrange allowed operations
            }
            else{
                approveCard()
            }
        } catch (e: JSONException) {
            setCallBackMessage(ResponseCode.ERROR)
            e.printStackTrace()
        }
    }

    /**
     * This method is putting some arguments to JSONObject for card read and calls getCard() method in cardService.
     */
    private fun getCard(amount: Int, config: String) {
        try {
            val obj = JSONObject(config)
            // TODO Developer: Check from Allowed Operations Parameter
            val isKeyInAllowed = true
            val isAskCVVAllowed = true
            val isFallbackAllowed = true
            val isQrPayAllowed = true
            obj.put("keyIn", if (isKeyInAllowed) 1 else 0)
            obj.put("askCVV", if (isAskCVVAllowed) 1 else 0)
            obj.put("fallback", if (isFallbackAllowed) 1 else 0)
            obj.put("qrPay", if (isQrPayAllowed) 1 else 0)
            obj.put("reqEMVData", "575A5F245F204F84959F12")
            cardServiceBinding?.getCard(amount, 30, obj.toString())
        } catch (e: Exception) {
            setCallBackMessage(ResponseCode.ERROR)
            e.printStackTrace()
        }
    }

    /** This function is called while second reading sales with ICC Card
     * It prepares card reading with Continue_Emv to ask card's password
     */
    private fun approveCard() {
        try {
            val obj = JSONObject()
            obj.put("forceOnline", 1)
            obj.put("zeroAmount", 0)
            obj.put("showAmount", 1)
            obj.put("emvProcessType", EmvProcessType.CONTINUE_EMV.ordinal)
            isApprove = false // to unbind CardService
            takePin = true // make this true for completeEmvTxn
            getCard(amount, obj.toString())
        } catch (e: java.lang.Exception) {
            setCallBackMessage(ResponseCode.ERROR)
            e.printStackTrace()
        }


        //TODO MSR ekle
    }

    private fun approveCardMSR(card: ICCCard){
        val obj = JSONObject()
        try {
            obj.put("amount", amount)
            obj.put("PAN", card.mCardNumber)
            obj.put("kmsVersion", 2)
            obj.put("keySet", 0)
            //obj.put("keyIndex", VersionParams.tpk) //for banks have host (not for app temp)
            obj.put("minLen", 4)
            obj.put("maxLen", 12)
            obj.put("timeout", 30)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val conf = obj.toString()
        cardServiceBinding!!.getOnlinePINEx(conf)
    }

    /**
     * This class is triggered after reading card, if card couldn't be read successfully a callback message is arranged
     * It will be observed where this function is called, then it will finish the mainActivity with respect to resultCode
     * After reading card operations are done, unbind the cardService.
     */
    override fun onCardDataReceived(cardData: String?) {
        try {
            Log.i("Card Data", cardData.toString())
            val card: ICCCard = Gson().fromJson(cardData, ICCCard::class.java) //get the ICC cardModel from cardData
            if (card.resultCode == CardServiceResult.USER_CANCELLED.resultCode()) { //if user pressed back button
                Log.i("CardDataReceived","Card Result Code: User Cancelled")
                setCallBackMessage(ResponseCode.CANCELED)
            }
            if (card.resultCode == CardServiceResult.ERROR_TIMEOUT.resultCode()) { //if there timeout is occurred
                Log.i("CardDataReceived","Card Result Code: TIMEOUT")
                setCallBackMessage(ResponseCode.CANCELED)
            }
            if (card.resultCode == CardServiceResult.ERROR.resultCode()) {
                setCallBackMessage(ResponseCode.ERROR)
                Log.i("CardDataReceived","Card Result Code: ERROR")
            }
            if (card.mCardReadType == CardReadType.ICC.type && transactionCode == TransactionCode.SALE.type){
                isApprove = true //make this flag true for the second reading for asking password with continue emv.
            }
            if (card.mCardReadType == CardReadType.MSR.type || card.mCardReadType == CardReadType.ICC2MSR.type){
                if (card.isOnlinePin()){
                    approveCardMSR(card)
                }
            }
            mutableCardData.postValue(card)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (!isApprove || takePin){
            val action = 0x01.toByte()
            val emvResult = cardServiceBinding!!.completeEmvTxn(action, byteArrayOf(0, 0), byteArrayOf(0, 0), 0, byteArrayOf(0, 0), 0) // this is for 330TR ICC
            cardServiceBinding!!.unBind()
            isCardServiceConnected.postValue(false)
        }
    }

    /**
     * When connecting to the card service, make this flag's value true to observe it from different classes.
     * After that call setEMVConfiguration method, it checks whether the Setup is Done before, if it is do nothing, else set EMV
     */
    override fun onCardServiceConnected() {
        Log.i("cardLogs","enter onCardServiceConnected")
        isCardServiceConnected.postValue(true)
        setEMVConfiguration()
    }

    fun getCardServiceBinding(): CardServiceBinding? {
        return cardServiceBinding
    }

    override fun onPinReceived(s: String) {}
    override fun onICCTakeOut() {}

    var toastMessage = MutableLiveData<String>()
    fun getToastMessage(): LiveData<String> {
        return toastMessage
    }


    /**
     * This function only works in installation, it calls setConfig and setCLConfig
     * It also called from onCardServiceConnected method of Card Service Library, if Configs couldn't set in first_run
     * (it is checked from sharedPreferences), again it setConfigurations, else do nothing.
     */
    fun setEMVConfiguration() {
        val sharedPreference = mainActivity.getSharedPreferences("myprefs", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        val firstTimeBoolean = sharedPreference.getBoolean("FIRST_RUN", false)
        if (!firstTimeBoolean) {
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
            val xmlStream = mainActivity.assets.open("emv_config.xml")
            val r = BufferedReader(InputStreamReader(xmlStream))
            val total = StringBuilder()
            var line: String? = r.readLine()
            while (line != null) {
                Log.d("emv_config", "conf line: $line")
                total.append(line).append('\n')
                line = r.readLine()
            }
            val setConfigResult = cardServiceBinding!!.setEMVConfiguration(total.toString())
            toastMessage.postValue("setEMVConfiguration res=$setConfigResult")
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
            val xmlCLStream = mainActivity.assets.open("emv_cl_config.xml")
            val rCL = BufferedReader(InputStreamReader(xmlCLStream))
            val totalCL = java.lang.StringBuilder()
            var line: String? = rCL.readLine()
            while (line != null) {
                Log.d("emv_config", "conf line: $line")
                totalCL.append(line).append('\n')
                line = rCL.readLine()
            }
            val setCLConfigResult: Int = cardServiceBinding!!.setEMVCLConfiguration(totalCL.toString())
            toastMessage.postValue("setEMVCLConfiguration res=$setCLConfigResult")
            Log.i("emv_config", "setEMVCLConfiguration: $setCLConfigResult")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }



}
