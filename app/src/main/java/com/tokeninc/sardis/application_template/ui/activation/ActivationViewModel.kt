package com.tokeninc.sardis.application_template.ui.activation

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
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

    val merchantID = activationRepository.merchantID
    val terminalID = activationRepository.terminalID
    val hostIP = activationRepository.hostIP
    val hostPort = activationRepository.hostPort

    var menuItemList = mutableListOf<IListMenuItem>()

    fun replaceFragment(mainActivity: MainActivity){
        val menuFragment = ListMenuFragment.newInstance(menuItemList,"Settings",
            true, R.drawable.token_logo_png)
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
    private suspend fun updateUIState(uistate: UIState){
        coroutineScope.launch(Dispatchers.Main) { //update UI in a dummy way
            uiState.postValue(uistate)
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
                Log.d("WithContextThread: ",Thread.currentThread().name)
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
