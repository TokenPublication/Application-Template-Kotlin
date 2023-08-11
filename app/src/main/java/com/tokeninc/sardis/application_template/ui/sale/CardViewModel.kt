package com.tokeninc.sardis.application_template.ui.sale

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.model.card.ICCCard
import com.tokeninc.sardis.application_template.data.repositories.CardRepository
import com.tokeninc.sardis.application_template.data.model.resultCode.ResponseCode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(private val cardRepository: CardRepository) : ViewModel() {

    // these functions can be observed from UI and also updated from UI.
    fun getTransactionCode(): LiveData<Int>  = cardRepository.getTransactionCode()
    fun setTransactionCode(code: Int){ cardRepository.setTransactionCode(code) }
    fun setAmount(amount: Int){ cardRepository.setAmount(amount) }
    fun getCallBackMessage(): LiveData<ResponseCode>  = cardRepository.getCallBackMessage()
    fun getCardLiveData(): LiveData<ICCCard> = cardRepository.getCardData()
    fun getCardServiceConnected() = cardRepository.getCardServiceConnected()
    fun getCardServiceBinding(): CardServiceBinding? { return cardRepository.getCardServiceBinding() }
    fun getTimeOut() = cardRepository.timeOut

    //these functions only updating from UI, they don't need to be observed
    fun setGibSale(isGibSale: Boolean) { cardRepository.gibSale = isGibSale }
    fun resetCard(){ cardRepository.setCard()}

    fun onDestroyed(){ cardRepository.onDestroyed()}

    fun initializeCardServiceBinding(main: MainActivity) {
        cardRepository.cardServiceBinder(main)
    }

    fun readCard() { cardRepository.readCard() }
}

