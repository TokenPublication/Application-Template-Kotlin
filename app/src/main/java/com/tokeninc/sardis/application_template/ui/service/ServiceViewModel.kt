package com.tokeninc.sardis.application_template.ui.service

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.libtokenkms.KMSWrapperInterface
import com.tokeninc.libtokenkms.TokenKMS
import com.tokeninc.sardis.application_template.AppTemp
import com.tokeninc.sardis.application_template.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor() : ViewModel() {

    //this is for storing UI state, it is observed from the fragment to update UI
    private val uiState = MutableLiveData<ServiceUIState>()
    @SuppressLint("StaticFieldLeak")
    private lateinit var mainActivity: MainActivity

    fun getUiState(): LiveData<ServiceUIState> = uiState

    sealed class ServiceUIState {
        object ErrorDeviceInfo : ServiceUIState()
        object ErrorKMS : ServiceUIState()
        object Connected : ServiceUIState()
    }

    /** ServiceRoutine is a routine that ensures connecting all services
     * At first, it tries to connect DeviceInfo then the KMS Service at DeviceInfo's successful result
     */
    fun serviceRoutine(mainActivity: MainActivity){
        this.mainActivity = mainActivity
        setDeviceInfo()
    }


    /**
     * This function tries to set Device Info parameters like FiscalID, cardRedirection or deviceMode.
     * With respect to result, it updates UI state which is observed from mainActivity.
     * It has timer, so if application cannot get information in 30 seconds post Error UIState and it shows a dialog
     * then finishes the mainActivity.
     * If it finishes successfully, it calls KMS service to connect.
     */
    private fun setDeviceInfo() {
        val applicationContext = mainActivity.applicationContext
        val appTemp = applicationContext as AppTemp
        val deviceInfo = DeviceInfo(applicationContext)
        val timer: CountDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                uiState.postValue(ServiceUIState.ErrorDeviceInfo)
            }
        }
        timer.start()
        deviceInfo.getFields(
            { fields: Array<String?>? ->
                if (fields == null) {
                    uiState.postValue(ServiceUIState.ErrorDeviceInfo)
                }
                appTemp.setCurrentFiscalID(fields!![0])
                appTemp.setCurrentDeviceMode(fields[1]!!)
                appTemp.setCurrentCardRedirection(fields[2]!!)
                deviceInfo.unbind()
                timer.cancel()
                Log.i("Connected","Device Info")
                connectKMSService()
            },
            DeviceInfo.Field.FISCAL_ID,
            DeviceInfo.Field.OPERATION_MODE,
            DeviceInfo.Field.CARD_REDIRECTION
        )
    }

    /** In this method, Application Template tries to connect KMS service
     * It will update uiState with respect to result
     */
    private fun connectKMSService(){
        val applicationContext = mainActivity.applicationContext
        val tokenKMS = TokenKMS() //connecting KMS Service with this flow
        tokenKMS.init(applicationContext, object : KMSWrapperInterface.InitCallbacks {
            override fun onInitSuccess() {
                Log.i("Token KMS onInitSuccess", "KMS Init OK")
                Log.i("Connected","KMS")
                uiState.postValue(ServiceUIState.Connected)
            }

            override fun onInitFailed() {
                Log.i("Token KMS onInitFailed", "KMS Init Failed")
                uiState.postValue(ServiceUIState.ErrorKMS)
            }
        })
    }
}
