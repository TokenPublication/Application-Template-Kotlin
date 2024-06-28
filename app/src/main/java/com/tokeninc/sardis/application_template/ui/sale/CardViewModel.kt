package com.tokeninc.sardis.application_template.ui.sale

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
    fun getCallBackMessage(): LiveData<ResponseCode>  = cardRepository.getCallBackMessage()
    fun getCardLiveData(): LiveData<ICCCard> = cardRepository.getCardData()
    fun getCardServiceConnected() = cardRepository.getCardServiceConnected()
    fun getCardServiceBinding(): CardServiceBinding? { return cardRepository.getCardServiceBinding() }

    //these functions only updating from UI, they don't need to be observed
    fun setGibSale(isGibSale: Boolean) { cardRepository.gibSale = isGibSale }
    fun resetCard(){ cardRepository.setCard()}

    fun onDestroyed(){ cardRepository.onDestroyed()}

    fun initializeCardServiceBinding(activity: AppCompatActivity) {
        cardRepository.cardServiceBinder(activity)
    }

    fun setEMVConfiguration(){
        cardRepository.setEMVConfiguration()
    }

    fun getToastMessage(): LiveData<String> {
        return cardRepository.getToastMessage()
    }
    fun resetToastMessage() {
        cardRepository.toastMessage = MutableLiveData<String>()
    }

    fun readCard(amount: Int, transactionCode: Int) { cardRepository.readCard(amount,transactionCode) }

}
