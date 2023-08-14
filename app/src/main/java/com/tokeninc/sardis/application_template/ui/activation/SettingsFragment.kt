package com.tokeninc.sardis.application_template.ui.activation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.CustomInput.CustomInputFormat
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.CustomInput.InputValidator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils

/**
 * This fragment is for Setting Configuration, it depends on Activation Database
 */
class SettingsFragment(private val mainActivity: MainActivity,
                       private val activationViewModel: ActivationViewModel) : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private var isBankActivateAction = true

    private var terminalId: String? = null
    private var merchantId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isBankActivateAction = mainActivity.intent.action != null && mainActivity.intent.action.equals("Activate_Bank")
        if (isBankActivateAction) {
            terminalId = mainActivity.intent.getStringExtra("terminalID")
            merchantId = mainActivity.intent.getStringExtra("merchantID")
        } else {
            showMenu()
        }
    }

    /**
     * This prepares menu, when user clicks Setup, Terminal ID & Merchant ID fragment will be opened
     * If user clicks Host Settings user can change IP Port configuration with opening page.
     */
    private fun showMenu(){
        val menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem(mainActivity.getString(R.string.setup), {
            addTidMidFragment()
        }))
        menuItems.add(MenuItem(mainActivity.getString(R.string.host_settings), {
            addIPFragment()
        }))
        val menuFragment = ListMenuFragment.newInstance(menuItems,"Settings", true, R.drawable.token_logo_png)
        mainActivity.replaceFragment(menuFragment as Fragment)
    }

    /**
     * This prepares IP, Port fragment with validator, it shows initial IP, Port number on the screen
     * If user changes their values, these values are updated in database
     */
    private fun addIPFragment(){

        val inputList = mutableListOf<CustomInputFormat>()
        inputList.add(CustomInputFormat(mainActivity.getString(R.string.ip),EditTextInputType.IpAddress,
        null, mainActivity.getString(R.string.invalid_ip), InputValidator {
                validate(it)
            }))

        inputList.add(CustomInputFormat(
            mainActivity.getString(R.string.port), EditTextInputType.Number, 4, mainActivity.getString(R.string.invalid_port)
        ) { customInputFormat -> customInputFormat.text.length >= 2 && customInputFormat.text.toInt() > 0 })

        inputList[0].text = activationViewModel.hostIP()
        inputList[1].text = activationViewModel.hostPort()

        val hostFragment = InputListFragment.newInstance(inputList, mainActivity.getString(R.string.save),
            InputListFragment.ButtonListener{
                val ipNo = inputList[0].text
                val portNo = inputList[1].text
                activationViewModel.updateConnection(ipNo, portNo)
                mainActivity.popFragment()
            })

        mainActivity.addFragment(hostFragment as Fragment)
    }
    private fun startActivation(){
        CoroutineScope(Dispatchers.Default).launch {
            activationViewModel.setupRoutine(mainActivity)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Processing, mainActivity.getString(R.string.starting_activation), false)
        activationViewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is ActivationViewModel.UIState.Starting -> mainActivity.showDialog(dialog)
                is ActivationViewModel.UIState.ParameterUploading -> dialog.update(InfoDialog.InfoType.Progress,mainActivity.getString(R.string.parameter_loading))
                is ActivationViewModel.UIState.MemberActCompleted -> dialog.update(InfoDialog.InfoType.Confirmed,mainActivity.getString(R.string.member_act_completed))
                is ActivationViewModel.UIState.RKLLoading -> dialog.update(InfoDialog.InfoType.Progress,mainActivity.getString(R.string.rkl_loading))
                is ActivationViewModel.UIState.RKLLoaded -> dialog.update(InfoDialog.InfoType.Confirmed,mainActivity.getString(R.string.rkl_loaded))
                is ActivationViewModel.UIState.KeyBlockLoading -> dialog.update(InfoDialog.InfoType.Progress,mainActivity.getString(R.string.key_block_loading))
                is ActivationViewModel.UIState.ActivationCompleted -> dialog.update(InfoDialog.InfoType.Confirmed,mainActivity.getString(R.string.activation_completed))
                is ActivationViewModel.UIState.Finished -> {
                    //TODO Developer: If you don't implement this your application couldn't be activated and couldn't seen in atms
                    val deviceInfo = DeviceInfo(mainActivity)
                    deviceInfo.setBankParams({ success -> //it informs atms with new terminal and merchant ID
                        if (success) {
                            mainActivity.print(PrintHelper().printSuccess())
                        } else {
                            mainActivity.print(PrintHelper().printError())
                        }
                        deviceInfo.unbind()
                    }, terminalId, merchantId)
                    dialog.dismiss()
                }
            }
        }
    }

    /**
     * This is for validating the input.
     */
    private fun validate(customInputFormat: CustomInputFormat): Boolean {
        val text = customInputFormat.text
        var isValid: Boolean =
            StringUtils.countMatches(text, ".") === 3 && text.split("\\.").toTypedArray().size == 4
        if (isValid) {
            val array = text.split("\\.").toTypedArray()
            var index = 0
            while (isValid && index < array.size) {
                isValid = StringUtils.isNumeric(array[0])
                index++
            }
        }
        return isValid
    }

    /** It shows a page with a fragment that contains Merchant and Terminal ID inputs
     * These 2 input values comes from Activation database, when user click save button, their values are updating
     * with respect to entering inputs.
     * */
    private fun addTidMidFragment() {
        val inputList = mutableListOf<CustomInputFormat>()
        inputList.add(CustomInputFormat(
            mainActivity.getString(R.string.merchant_no),
            EditTextInputType.Number,
            10,
            mainActivity.getString(R.string.invalid_merchant_no)
        ) { input -> input.text.length == 10 })

        inputList.add(CustomInputFormat(
            mainActivity.getString(R.string.terminal_no),
            EditTextInputType.Text,
            8,
            mainActivity.getString(R.string.invalid_terminal_no)
        ) { input -> input.text.length == 8 })

        inputList[0].text = activationViewModel.merchantID()
        inputList[1].text = activationViewModel.terminalID()
        val tidMidFragment = InputListFragment.newInstance(inputList, mainActivity.getString(R.string.save),
            ){
            merchantId = inputList[0].text
            Log.d(mainActivity.getString(R.string.merchant_no),merchantId.toString())
            terminalId = inputList[1].text
            Log.d(mainActivity.getString(R.string.terminal_no),terminalId.toString())
            activationViewModel.updateActivation(terminalId, merchantId)
            startActivation()
            mainActivity.popFragment()
        }
        mainActivity.addFragment(tidMidFragment as Fragment)
    }
}
