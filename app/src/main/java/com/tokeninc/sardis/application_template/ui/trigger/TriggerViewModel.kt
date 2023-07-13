package com.tokeninc.sardis.application_template.ui.trigger

import android.content.Intent
import android.content.res.AssetManager
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class TriggerViewModel: ViewModel() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val liveIntent = MutableLiveData<Intent>()

    fun getLiveIntent(): LiveData<Intent> = liveIntent

    private val triggerUiState = MutableLiveData<TriggerUIState>()

    fun getUiState(): LiveData<TriggerUIState> = triggerUiState

    //this is a UI state to update UI in mainActivity
    sealed class TriggerUIState {
        object Loading : TriggerUIState()
        object Success : TriggerUIState()
    }

    /**
     * In this routine, dummy bin tables, allowed operations, supportedAIDs and clConfigFiles are sent to pgw
     * as this template's parameters in payment gateway's trigger moment.
     */
    fun parameterRoutine(assetManager: AssetManager){
        coroutineScope.launch{
            withContext(Dispatchers.Main){//update UI
                triggerUiState.postValue(TriggerUIState.Loading)
            }
            delay(2000L)
            val resultIntent = Intent()
            val bundle = Bundle()
            var clConfigFile = ""
            try {
                val xmlCLStream: InputStream = assetManager.open("custom_emv_cl_config.xml")
                val rCL = BufferedReader(InputStreamReader(xmlCLStream))
                val totalCL = StringBuilder()
                var line: String?
                while (withContext(Dispatchers.IO) {
                        rCL.readLine()
                    }.also { line = it } != null) {
                    totalCL.append(line).append('\n')
                }
                clConfigFile = totalCL.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            bundle.putString("clConfigFile", clConfigFile)
            val bins =
                "[{\"cardRangeStart\":\"1111110000000\",\"cardRangeEnd\":\"1111119999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"C\"}," +
                        "{\"cardRangeStart\":\"2222220000000\",\"cardRangeEnd\":\"2222229999999\",\"OwnerShip\":\"NONE\",\"CardType\":\"C\"}," +
                        "{\"cardRangeStart\":\"3333330000000\",\"cardRangeEnd\":\"3333339999999\",\"OwnerShip\":\"BRAND\",\"CardType\":\"C\"}]"
            bundle.putString("BINS", bins)
            bundle.putString("AllowedOperations", "{" + "\"QrAllowed\"" + ":" + "1" + "," + "\"KeyInAllowed\"" + ":" + "1" + "}")
            bundle.putString("SupportedAIDs", "[A0000000031010, A0000000041010, A0000000032010]")
            resultIntent.putExtras(bundle)
            withContext(Dispatchers.Main){
                triggerUiState.postValue(TriggerUIState.Success) // update the ui as an success
            }
            delay(2000L)
            liveIntent.postValue(resultIntent)
        }
    }
}
