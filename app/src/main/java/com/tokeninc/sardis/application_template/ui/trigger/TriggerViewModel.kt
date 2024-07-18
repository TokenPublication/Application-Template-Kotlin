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
                val xmlCLStream: InputStream = assetManager.open("emv_cl_config.xml")
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
            val bins = "[{\"cardRangeStart\":\"1234560000000\",\"cardRangeEnd\":\"1234569999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3700000000000\",\"cardRangeEnd\":\"3799999999999\",\"OwnerShip\":\"BRAND\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3744210000000\",\"cardRangeEnd\":\"3744219999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3744220000000\",\"cardRangeEnd\":\"3744229999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3744230000000\",\"cardRangeEnd\":\"3744239999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3744240000000\",\"cardRangeEnd\":\"3744249999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3744270000000\",\"cardRangeEnd\":\"3744279999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3744280000000\",\"cardRangeEnd\":\"3744289999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3756220000000\",\"cardRangeEnd\":\"3756229999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3756230000000\",\"cardRangeEnd\":\"3756239999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3756240000000\",\"cardRangeEnd\":\"3756249999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3756280000000\",\"cardRangeEnd\":\"3756289999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3756290000000\",\"cardRangeEnd\":\"3756299999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3756310000000\",\"cardRangeEnd\":\"3756319999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3771370000000\",\"cardRangeEnd\":\"3771379999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3775960000000\",\"cardRangeEnd\":\"3775969999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3775970000000\",\"cardRangeEnd\":\"3775979999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3775980000000\",\"cardRangeEnd\":\"3775989999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3775990000000\",\"cardRangeEnd\":\"3775999999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3793690000000\",\"cardRangeEnd\":\"3793699999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3793700000000\",\"cardRangeEnd\":\"3793709999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"3793710000000\",\"cardRangeEnd\":\"3793719999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4017380000000\",\"cardRangeEnd\":\"4017389999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4036660000000\",\"cardRangeEnd\":\"4036669999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4043080000000\",\"cardRangeEnd\":\"4043089999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4050510000000\",\"cardRangeEnd\":\"4050519999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4050900000000\",\"cardRangeEnd\":\"4050909999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4066550000000\",\"cardRangeEnd\":\"4066559999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4092190000000\",\"cardRangeEnd\":\"4092199999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4205570000000\",\"cardRangeEnd\":\"4205579999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4268860000000\",\"cardRangeEnd\":\"4268869999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4268870000000\",\"cardRangeEnd\":\"4268879999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4268890000000\",\"cardRangeEnd\":\"4268899999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4273140000000\",\"cardRangeEnd\":\"4273149999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4273150000000\",\"cardRangeEnd\":\"4273159999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4282200000000\",\"cardRangeEnd\":\"4282209999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4282210000000\",\"cardRangeEnd\":\"4282219999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4329520000000\",\"cardRangeEnd\":\"4329529999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4329530000000\",\"cardRangeEnd\":\"4329539999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4329540000000\",\"cardRangeEnd\":\"4329549999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4484720000000\",\"cardRangeEnd\":\"4484729999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4616680000000\",\"cardRangeEnd\":\"4616689999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4672930000000\",\"cardRangeEnd\":\"4672939999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4672940000000\",\"cardRangeEnd\":\"4672949999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4672950000000\",\"cardRangeEnd\":\"4672959999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4796600000000\",\"cardRangeEnd\":\"4796609999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4796610000000\",\"cardRangeEnd\":\"4796619999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4796620000000\",\"cardRangeEnd\":\"4796629999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4796820000000\",\"cardRangeEnd\":\"4796829999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4824890000000\",\"cardRangeEnd\":\"4824899999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4824900000000\",\"cardRangeEnd\":\"4824909999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4824910000000\",\"cardRangeEnd\":\"4824919999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4870740000000\",\"cardRangeEnd\":\"4870749999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4870750000000\",\"cardRangeEnd\":\"4870759999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4894550000000\",\"cardRangeEnd\":\"4894559999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"4894780000000\",\"cardRangeEnd\":\"4894789999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"4921870000000\",\"cardRangeEnd\":\"4921879999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5149150000000\",\"cardRangeEnd\":\"5149159999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5169430000000\",\"cardRangeEnd\":\"5169439999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5170400000000\",\"cardRangeEnd\":\"5170409999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5170410000000\",\"cardRangeEnd\":\"5170419999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5170420000000\",\"cardRangeEnd\":\"5170429999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5170480000000\",\"cardRangeEnd\":\"5170489999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5170490000000\",\"cardRangeEnd\":\"5170499999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5184900000000\",\"cardRangeEnd\":\"5184929999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5200970000000\",\"cardRangeEnd\":\"5200979999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5209400000000\",\"cardRangeEnd\":\"5209409999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5209880000000\",\"cardRangeEnd\":\"5209889999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5218240000000\",\"cardRangeEnd\":\"5218249999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5218250000000\",\"cardRangeEnd\":\"5218259999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5222040000000\",\"cardRangeEnd\":\"5222049999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5246590000000\",\"cardRangeEnd\":\"5246599999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5269550000000\",\"cardRangeEnd\":\"5269559999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5289390000000\",\"cardRangeEnd\":\"5289399999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5289560000000\",\"cardRangeEnd\":\"5289569999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5331690000000\",\"cardRangeEnd\":\"5331699999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5342610000000\",\"cardRangeEnd\":\"5342619999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5354290000000\",\"cardRangeEnd\":\"5354299999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5354880000000\",\"cardRangeEnd\":\"5354889999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5368360000000\",\"cardRangeEnd\":\"5368369999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5378290000000\",\"cardRangeEnd\":\"5378299999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5381210000000\",\"cardRangeEnd\":\"5381219999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5381240000000\",\"cardRangeEnd\":\"5381249999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5381390000000\",\"cardRangeEnd\":\"5381399999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5381960000000\",\"cardRangeEnd\":\"5381969999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5400370000000\",\"cardRangeEnd\":\"5400379999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5406690000000\",\"cardRangeEnd\":\"5406699999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5407090000000\",\"cardRangeEnd\":\"5407099999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5418650000000\",\"cardRangeEnd\":\"5418659999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5440780000000\",\"cardRangeEnd\":\"5440789999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5496040000000\",\"cardRangeEnd\":\"5496049999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5499970000000\",\"cardRangeEnd\":\"5499979999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5531300000000\",\"cardRangeEnd\":\"5531309999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5542530000000\",\"cardRangeEnd\":\"5542539999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5542540000000\",\"cardRangeEnd\":\"5542549999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5549600000000\",\"cardRangeEnd\":\"5549609999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"5571420000000\",\"cardRangeEnd\":\"5571429999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"5893180000000\",\"cardRangeEnd\":\"5893189999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"6034800000000\",\"cardRangeEnd\":\"6034809999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"6224030000000\",\"cardRangeEnd\":\"6224039999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"6573660000000\",\"cardRangeEnd\":\"6573669999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"6706060000000\",\"cardRangeEnd\":\"6706069999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"6762550000000\",\"cardRangeEnd\":\"6762559999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"6766510000000\",\"cardRangeEnd\":\"6766519999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"6768270000000\",\"cardRangeEnd\":\"6768279999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"B\"}," +
                        "{\"cardRangeStart\":\"9792050000000\",\"cardRangeEnd\":\"9792059999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"9792290000000\",\"cardRangeEnd\":\"9792299999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"9792990000000\",\"cardRangeEnd\":\"9792999999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}," +
                        "{\"cardRangeStart\":\"9792360000000\",\"cardRangeEnd\":\"9792369999999\",\"OwnerShip\":\"ISSUER\",\"CardType\":\"A\"}]"

            val allowedOperations = "{" + "\"QrAllowed\"" + ":" + "0" + "," + "\"KeyInAllowed\"" + ":" + "1" + "}"
            val supportedAIDs = "[A0000000041010, A0000000031010, A0000000043060, A0000000032010, A0000000651010, A00000002501, A000000333010101, A000000333010102, A000000333010103, A000000333010106, A0000006723010, A0000006723020, A0000001523010, A0000001524010, A0000003241010, A000000152301091, A000000152301092, A000000672301001, A0000000250402]"

            bundle.putString("BINS", bins)
            bundle.putString("AllowedOperations", allowedOperations)
            bundle.putString("SupportedAIDs", supportedAIDs)


            resultIntent.putExtras(bundle)
            withContext(Dispatchers.Main){
                triggerUiState.postValue(TriggerUIState.Success) // update the ui as an success
            }
            delay(2000L)
            liveIntent.postValue(resultIntent)
        }
    }
}
