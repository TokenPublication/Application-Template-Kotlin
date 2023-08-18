package com.tokeninc.sardis.application_template.ui.activation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.data.repositories.ActivationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * We annotate with HiltViewModel to notify Hilt (Dependency Injection) to it's our viewModel
 * lately we call this viewModel without passing its parameter thanks to  hilt like in the repository here
 * We won't call repository while we call ViewModel class because we define our repository in AppModule with Hilt.
 */
@HiltViewModel
class ActivationViewModel @Inject constructor(val activationRepository: ActivationRepository): ViewModel() {

    fun merchantID() = activationRepository.merchantID()
    fun terminalID() = activationRepository.terminalID()
    fun hostIP() = activationRepository.hostIP()
    fun hostPort() = activationRepository.hostPort()

    fun updateActivation(terminalId: String?, merchantId: String?){
        viewModelScope.launch(Dispatchers.IO){
            activationRepository.updateActivation(terminalId,merchantId)
        }
    }

    fun updateConnection(ip: String?, port: String?){
        viewModelScope.launch(Dispatchers.IO){
            activationRepository.updateConnection(ip,port)
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val uiState = MutableLiveData<UIState>()
    fun getUiState(): LiveData<UIState> = uiState

    //this is a UI state to update UI from Fragments
    sealed class UIState {
        object Starting : UIState()
        object ParameterUploading : UIState()
        object MemberActCompleted : UIState()
        object RKLLoading : UIState()
        object RKLLoaded : UIState()
        object KeyBlockLoading : UIState()
        object ActivationCompleted : UIState()
        object Finished : UIState()
    }

    /**
     * This is for not repeating each uiState again.
     */
    private suspend fun updateUIState(ui_state: UIState){
        coroutineScope.launch(Dispatchers.Main) { //update UI in a dummy way
            uiState.postValue(ui_state)
        }
        delay(2000L)
    }

    /** It runs functions in parallel while ui updating dynamically in main thread
     * Additionally, in IO coroutine thread make setEMVConfiguration method
     */
    suspend fun setupRoutine(mainActivity: MainActivity) {
        coroutineScope.launch {
            updateUIState(UIState.Starting)
            withContext(Dispatchers.IO){
                mainActivity.setEMVConfiguration(false)
            }
            updateUIState(UIState.ParameterUploading)
            updateUIState(UIState.MemberActCompleted)
            updateUIState(UIState.RKLLoading)
            updateUIState(UIState.RKLLoaded)
            updateUIState(UIState.KeyBlockLoading)
            updateUIState(UIState.ActivationCompleted)
            uiState.postValue(UIState.Finished)
        }.join() //wait that job to finish to return it
    }
}
