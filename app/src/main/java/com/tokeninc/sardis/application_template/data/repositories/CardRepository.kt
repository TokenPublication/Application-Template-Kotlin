package com.tokeninc.sardis.application_template.data.repositories
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
import javax.inject.Inject

/**
 * This class is for managing data related card operations
 * It implements CardServiceListener interface's methods which are card service binding lib's methods.
 */
class CardRepository @Inject constructor() :
    CardServiceListener {

    // These variables are both updating from here and mainActivity, they also observed from different classes therefore they are LiveData
    private var transactionCode = MutableLiveData(0)
    private fun getTransactionCode(): LiveData<Int> {
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

    private var cardServiceBinding: CardServiceBinding? = null

    fun cardServiceBinder(activity: AppCompatActivity) {
        cardServiceBinding = CardServiceBinding(activity, this)
    }

    /** It is called when the main activity is finished (destroyed), because this repository doesn't end with the main Activity.
     * It only initializes the variables again, with this method, most of the controlling functions to ensure that
     * keep variables again at an initial point won't be needed.
     */
    fun onDestroyed(){
        mutableCardData =  MutableLiveData<ICCCard>()
        transactionCode = MutableLiveData(0)
        amount = MutableLiveData(0)
        callBackMessage = MutableLiveData<ResponseCode>()
        isCardServiceConnected = MutableLiveData(false)
        gibSale = false
        isApprove = false
    }

    /**
     * This reads the card
     */
    fun readCard() {
        val obj = JSONObject()
        try {
            if (!isApprove) { //if it is not the second readCard (reading ICC card for sale)
                // in sale and void emv process should be EmvProcessType.READ_CARD, for refunds it should be EmvProcessType.FULL_EMV
                obj.put("forceOnline", 1)
                obj.put("zeroAmount", 0)
                val isVoid = getTransactionCode().value == TransactionCode.VOID.type
                val isSale = getTransactionCode().value == TransactionCode.SALE.type
                obj.put("emvProcessType", if (isVoid || isSale) EmvProcessType.READ_CARD.ordinal else EmvProcessType.FULL_EMV.ordinal)
                obj.put("showAmount", if (isVoid) 0 else 1)
                obj.put("showCardScreen", if (gibSale) 0 else 1)
                getCard(amount.value!!,obj.toString()) // arrange allowed operations
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
            getCard(amount.value!!, obj.toString())
        } catch (e: java.lang.Exception) {
            setCallBackMessage(ResponseCode.ERROR)
            e.printStackTrace()
        }
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
            if (card.mCardReadType == CardReadType.ICC.type && getTransactionCode().value == TransactionCode.SALE.type){
                isApprove = true //make this flag true for the second reading for asking password with continue emv.
            }
            mutableCardData.postValue(card)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /**
     * When connecting to the card service, make this flag's value true to observe it from different classes.
     * After that call setEMVConfiguration method, it checks whether the Setup is Done before, if it is do nothing, else set EMV
     */
    override fun onCardServiceConnected() {
        isCardServiceConnected.postValue(true)
    }

    fun getCardServiceBinding(): CardServiceBinding? {
        return cardServiceBinding
    }

    override fun onPinReceived(s: String) {}
    override fun onICCTakeOut() {}
}
