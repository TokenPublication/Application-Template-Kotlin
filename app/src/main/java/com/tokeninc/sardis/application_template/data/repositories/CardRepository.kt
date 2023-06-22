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
import com.tokeninc.sardis.application_template.enums.CardReadResult
import com.tokeninc.sardis.application_template.enums.CardReadType
import com.tokeninc.sardis.application_template.enums.CardServiceResult
import com.tokeninc.sardis.application_template.enums.ResponseCode
import com.tokeninc.sardis.application_template.enums.TransactionCode
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


class CardRepository @Inject constructor() :
    CardServiceListener {



    //setValue or .code = for mainThreads, postValue for any threads in MutableLiveData
    // lifecycle'ı farklı olduğundan mainActivity finish olunca buradaki variablelar ilk hallerine dönmezler, bunu handle et!
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

    private var cardReadResult = MutableLiveData<CardReadResult>()
    fun getCardReadResult(): LiveData<CardReadResult> {
        return cardReadResult
    }

    private var cardContents = MutableLiveData<ContentValues>()
    fun getCardContents(): LiveData<ContentValues> {
        return cardContents
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
    var refundInfo: String? = null
    lateinit var refNo: String
    var extraContents : ContentValues? = null

    private lateinit var cardServiceBinding: CardServiceBinding

    fun cardServiceBinder(mainActivity: MainActivity) {
        cardServiceBinding = CardServiceBinding(mainActivity, this)
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

    //mainActivity onCardDataReceievedi mi çalıştı ?
    var enters = false
    override fun onCardDataReceived(cardData: String?) {
        if (!enters){
            enters = true
            try {
                val json = JSONObject(cardData!!) //convert cardData to JSON format
                val card: ICCCard = Gson().fromJson(cardData, ICCCard::class.java) //get the ICC cardModel from cardData

                if (card.resultCode == CardServiceResult.USER_CANCELLED.resultCode()) { //if user pressed back button in GiB operation
                    Log.d("CardDataReceived","Card Result Code: User Cancelled")
                    setCallBackMessage(ResponseCode.CANCELED)
                }

                if (card.resultCode == CardServiceResult.ERROR_TIMEOUT.resultCode()) { //if there is a timeout
                    Log.d("CardDataReceived","Card Result Code: TIMEOUT")
                    setCallBackMessage(ResponseCode.CANCELED)
                }

                val type = json.getInt("mCardReadType") //get type
                if (card.resultCode == CardServiceResult.ERROR.resultCode()) {
                    Log.d("CardDataReceived","Card Result Code: ERROR")
                }
                if (card.resultCode == CardServiceResult.SUCCESS.resultCode()) { //if card reads is successful
                    when (type) { // implementing methods with respect to card read types
                        CardReadType.QrPay.type -> {
                            cardReadResult.postValue(CardReadResult.QR_PAY)
                        }
                        CardReadType.CLCard.type -> { //if it is contactless
                            if (getTransactionCode().value == TransactionCode.SALE.type && !gibSale){ //if the transaction is sale and its not a gib operation
                                transactionCode.postValue(0) //not enter this again (it's a bug)
                                cardReadResult.postValue(CardReadResult.SALE_NOT_GIB_CL)
                            }
                        }
                        CardReadType.ICC.type -> {  //if it is ICC
                            if (getTransactionCode().value == TransactionCode.SALE.type && !gibSale){ ////if the transaction is sale and its not a gib operation
                                cardReadResult.postValue(CardReadResult.SALE_NOT_GIB_ICC)
                            }
                        }
                        CardReadType.ICC2MSR.type, CardReadType.MSR.type, CardReadType.KeyIn.type -> {
                            //card = Gson().fromJson(cardData, MSRCard::class.java)
                            //cardServiceBinding!!.getOnlinePIN(amount, card?.cardNumber, 0x0A01, 0, 4, 8, 30)
                        }
                    }
                    if (getTransactionCode().value == TransactionCode.VOID.type){ //if the transaction Code is VOID
                        if (gibRefund) { // If it is GIB operation
                            gibRefund = false
                            setTransactionCode(0)
                            cardReadResult.postValue(CardReadResult.VOID_GIB)
                        }
                        else { //// If it is not GIB operation
                            cardReadResult.postValue(CardReadResult.VOID_NOT_GIB)
                        }
                    }
                    else if (gibSale){ // If it is a GIB sale
                        gibSale = false
                        setTransactionCode(0)
                        cardReadResult.postValue(CardReadResult.SALE_GIB)
                    }
                    else if (getTransactionCode().value == TransactionCode.MATCHED_REFUND.type || getTransactionCode().value == TransactionCode.INSTALLMENT_REFUND.type || getTransactionCode().value == TransactionCode.CASH_REFUND.type){
                        // If it is a refund
                        if (gibRefund){ //If it is a GIB refund
                            gibRefund = false
                            cardReadResult.postValue(CardReadResult.REFUND_GIB)
                        } else //If it is not a GIB refund
                            cardReadResult.postValue(CardReadResult.REFUND_NOT_GIB)
                    }

                }
                this.card.postValue(card)
                // cardServiceBinding.unBind()
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
