package com.tokeninc.sardis.application_template.database.entities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivationViewModel(private val activationRepository: ActivationRepository): ViewModel() {

    val merchantID = activationRepository.merchantID
    val terminalID = activationRepository.terminalID
    val hostIP = activationRepository.hostIP
    val hostPort = activationRepository.hostPort

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