package com.tokeninc.sardis.application_template.data.repositories
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.enums.CardServiceResult
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


class CardRepository @Inject constructor() :
    CardServiceListener {



    // these variables are both updating from here and mainActivity and also observe to make some operations therefore they are LiveData
    private var transactionCode = MutableLiveData<Int>(0)
    fun getTransactionCode(): LiveData<Int> {
        return transactionCode
    }
    fun setTransactionCode(code: Int){
        transactionCode.postValue(code)
    }

    private var amount = MutableLiveData<Int>(0) //this is for holding amount
    fun getAmount(): LiveData<Int> {
        return amount
    }
    fun setAmount(transactionAmount: Int){
        amount.postValue(transactionAmount)
    }

    private var callBackMessage = MutableLiveData<ResponseCode>() //this is for holding amount
    fun getCallBackMessage(): LiveData<ResponseCode> {
        return callBackMessage
    }
    fun setCallBackMessage(callBackMessage_: ResponseCode){
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
    fun resetCard(){ card = MutableLiveData<ICCCard>()
    }

    //these variables should only for storing the operation's result and intents' responses, because they won't be used
    //for UI updating they don't have to be a LiveData
    var gibRefund = false
    var gibSale = false

    private lateinit var cardServiceBinding: CardServiceBinding

    fun cardServiceBinder(mainActivity: MainActivity) {
        cardServiceBinding = CardServiceBinding(mainActivity, this)
    }

    /** It is called when the main activity is finished (destroyed), because this repository doesn't end with the main Activity.
     * It only initializes the variables again, with this method, most of the controlling functions to ensure that
     * keep variables again at an initial point won't be needed.
     */
    fun onDestroyed(){
        transactionCode = MutableLiveData<Int>(0)
        amount = MutableLiveData<Int>(0)
        callBackMessage = MutableLiveData<ResponseCode>()
        isCardServiceConnected = MutableLiveData(false)
        card =  MutableLiveData<ICCCard>()
        gibRefund = false
        gibSale = false
    }

    /**
     * This reads the card
     */
    fun readCard() {
        val obj = JSONObject()
        try {
            obj.put("forceOnline", 0)
            obj.put("zeroAmount", 1)
            obj.put("showAmount", if (getTransactionCode().value == TransactionCode.VOID.type) 0 else 1) //amountu göstermiyor voidse
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
        cardServiceBinding.getCard(amount.value!!, 30, obj.toString())
        enters = false
    }

    //TODO niye 2 kez geliyor
    var enters = false
    override fun onCardDataReceived(cardData: String?) {
        if (!enters){
            enters = true
            try {
                val card: ICCCard = Gson().fromJson(cardData, ICCCard::class.java) //get the ICC cardModel from cardData
                if (card.resultCode == CardServiceResult.USER_CANCELLED.resultCode()) { //if user pressed back button in GiB operation
                    Log.d("CardDataReceived","Card Result Code: User Cancelled")
                    setCallBackMessage(ResponseCode.CANCELED)
                }
                if (card.resultCode == CardServiceResult.ERROR_TIMEOUT.resultCode()) { //if there is a timeout
                    Log.d("CardDataReceived","Card Result Code: TIMEOUT")
                    setCallBackMessage(ResponseCode.CANCELED)
                }
                if (card.resultCode == CardServiceResult.ERROR.resultCode()) {
                    setCallBackMessage(ResponseCode.ERROR)
                    Log.d("CardDataReceived","Card Result Code: ERROR")
                }
                this.card.postValue(card)
                if (!gibRefund) //TODO void_gibde patlıyor neden bak
                    cardServiceBinding.unBind() //unbinding the cardService
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCardServiceConnected() {
        isCardServiceConnected.value = true
    }

    override fun onPinReceived(s: String) {}
    override fun onICCTakeOut() {}
}
