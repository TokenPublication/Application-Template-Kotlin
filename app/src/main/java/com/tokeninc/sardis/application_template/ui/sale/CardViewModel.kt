package com.tokeninc.sardis.application_template.ui.sale

import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.entities.card_entities.ICCCard
import com.tokeninc.sardis.application_template.data.repositories.CardRepository
import com.tokeninc.sardis.application_template.enums.CardReadResult
import com.tokeninc.sardis.application_template.enums.ResponseCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private val cardRepository: CardRepository) : ViewModel() {

    // these functions can be observed from UI and also updated from UI.
    fun getTransactionCode(): LiveData<Int>  = cardRepository.getTransactionCode()
    fun setTransactionCode(code: Int){ cardRepository.setTransactionCode(code) }
    fun getAmount(): LiveData<Int>  = cardRepository.getAmount()
    fun setAmount(amount: Int){ cardRepository.setAmount(amount) }
    fun getCallBackMessage(): LiveData<ResponseCode>  = cardRepository.getCallBackMessage()
    fun setCallBackMessage(callbackMessage: ResponseCode){ cardRepository.setCallBackMessage(callbackMessage) }

    fun getCardLiveData(): LiveData<ICCCard> = cardRepository.getCard()
    fun resetCard() = cardRepository.resetCard()
    fun getCardReadResult(): LiveData<CardReadResult> = cardRepository.getCardReadResult()
    fun getCardServiceConnected() = cardRepository.getCardServiceConnected()

    //these functions only updating from UI, they don't need to be observed
    fun setGibRefund(isAutoTransaction: Boolean){ cardRepository.gibRefund = isAutoTransaction }
    fun setGibSale(isGibSale: Boolean) { cardRepository.gibSale = isGibSale }

    fun initializeCardServiceBinding(main: MainActivity) {
        Handler(Looper.getMainLooper()).postDelayed({
            cardRepository.cardServiceBinder(main)
        }, 5)
    }

    fun readCard() { cardRepository.readCard() }

}
