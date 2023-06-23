package com.tokeninc.sardis.application_template.ui.activation

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.repositories.ActivationRepository
import com.tokeninc.sardis.application_template.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * We annotate with HiltViewModel to tell Hilt (Dependency Injection) to it's our viewModel
 * lately we call this viewModel without passing its parameter thanks to  hilt like in the repository here
 * We won't call repository while we call ViewModel class because we define our repository in AppModule with Hilt.
 */
@HiltViewModel
class ActivationViewModel @Inject constructor(private val activationRepository: ActivationRepository): ViewModel() {

    val merchantID = activationRepository.merchantID
    val terminalID = activationRepository.terminalID
    val hostIP = activationRepository.hostIP
    val hostPort = activationRepository.hostPort

    var menuItemList = mutableListOf<IListMenuItem>()

    fun replaceFragment(mainActivity: MainActivity){
        val menuFragment = ListMenuFragment.newInstance(menuItemList,"Settings",
            true, R.drawable.token_logo)
        viewModelScope.launch(Dispatchers.Main) {
            mainActivity.replaceFragment(menuFragment as Fragment)
        }
    }

    fun updateActivation(terminalId: String?, merchantId: String?, ip: String?){
        viewModelScope.launch(Dispatchers.IO){
            activationRepository.updateActivation(terminalId,merchantId,ip)
        }
    }

    fun updateConnection(ip: String?, port: String?, old_ip: String?){
        viewModelScope.launch(Dispatchers.IO){
            activationRepository.updateConnection(ip,port,old_ip)
        }
    }

    fun deleteAll(){
        activationRepository.deleteAll()
    }
}