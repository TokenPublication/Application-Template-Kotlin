package com.tokeninc.sardis.application_template.data.repositories
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.enums.CardServiceResult
import com.tokeninc.sardis.application_template.enums.EmvProcessType
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

/**
 * This class is for managing data related card operations
 * It implements CardServiceListener interface's methods which are card service binding lib's methods.
 */
class CardRepository @Inject constructor() :
    CardServiceListener {



    // these variables are both updating from here and mainActivity, they also observed from different classes therefore they are LiveData
    private var transactionCode = MutableLiveData(0)
    fun getTransactionCode(): LiveData<Int> {
        return transactionCode
    }
    fun setTransactionCode(code: Int){
        transactionCode.postValue(code)
    }

    private var amount = MutableLiveData(0)
    fun setAmount(transactionAmount: Int){
        amount.postValue(transactionAmount)
    }

    private var callBackMessage = MutableLiveData<ResponseCode>()
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

    private var card =  MutableLiveData<ICCCard>()
    fun getCard(): LiveData<ICCCard> {
        return card
    }

    //these variables should only for storing the operation's result and intents' responses, because they won't be used
    //for UI updating they don't have to be a LiveData
    var gibSale = false
    var mainActivity: MainActivity? = null


    private lateinit var cardServiceBinding: CardServiceBinding

    fun cardServiceBinder(mainActivity: MainActivity) {
        cardServiceBinding = CardServiceBinding(mainActivity, this)
    }

    /** It is called when the main activity is finished (destroyed), because this repository doesn't end with the main Activity.
     * It only initializes the variables again, with this method, most of the controlling functions to ensure that
     * keep variables again at an initial point won't be needed.
     */
    fun onDestroyed(){
        transactionCode = MutableLiveData(0)
        amount = MutableLiveData(0)
        callBackMessage = MutableLiveData<ResponseCode>()
        isCardServiceConnected = MutableLiveData(false)
        card =  MutableLiveData<ICCCard>()
        gibSale = false
        mainActivity = null
    }

    /**
     * This reads the card
     */
    fun readCard() {
        val obj = JSONObject()
        try {
            obj.put("forceOnline", 0)
            obj.put("zeroAmount", 1)
            val isVoid = getTransactionCode().value == TransactionCode.VOID.type
            obj.put("emvProcessType", if (isVoid) EmvProcessType.READ_CARD.ordinal else EmvProcessType.FULL_EMV.ordinal)
            obj.put("reqEMVData", "575A5F245F204F84959F12");
            obj.put("showAmount", if (isVoid) 0 else 1)
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
        cardServiceBinding.getCard(amount.value!!, 30, obj.toString())
    }

    /**
     * This class is triggered after reading card, if card couldn't be read successfully a callback message is arranged
     * It will be observed where this function is called, then it will finish the mainActivity with respect to resultCode
     * After reading card operations are done, unbind the cardService.
     */

    override fun onCardDataReceived(cardData: String?) {
        try {
            val card: ICCCard = Gson().fromJson(cardData, ICCCard::class.java) //get the ICC cardModel from cardData
            if (card.resultCode == CardServiceResult.USER_CANCELLED.resultCode()) { //if user pressed back button
                Log.d("CardDataReceived","Card Result Code: User Cancelled")
                setCallBackMessage(ResponseCode.CANCELED) //TODO ekranda mesaj yazdır
            }
            if (card.resultCode == CardServiceResult.ERROR_TIMEOUT.resultCode()) { //if there timeout is occurred
                Log.d("CardDataReceived","Card Result Code: TIMEOUT")
                setCallBackMessage(ResponseCode.CANCELED) //TODO ekranda mesaj yazdır sarı üçgen warning
            }
            if (card.resultCode == CardServiceResult.ERROR.resultCode()) {
                setCallBackMessage(ResponseCode.ERROR)
                Log.d("CardDataReceived","Card Result Code: ERROR")
            }
            this.card.postValue(card)
            cardServiceBinding.unBind() //unbinding the cardService
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * When connecting to the card service, make this flag's value true to observe it from different classes.
     * After that call setEMVConfiguration method, it checks whether the Setup is Done before, if it is do nothing, else set EMV
     */
    override fun onCardServiceConnected() {
        isCardServiceConnected.value = true
        mainActivity!!.setEMVConfiguration(true)
    }

    fun getCardServiceBinding(): CardServiceBinding {
        return cardServiceBinding
    }

    override fun onPinReceived(s: String) {}
    override fun onICCTakeOut() {}
}
