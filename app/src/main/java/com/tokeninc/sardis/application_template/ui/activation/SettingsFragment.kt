package com.tokeninc.sardis.application_template.ui.activation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.utils.BaseFragment
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils

/**
 * This fragment is for Setting Configuration, it depends on Activation Database
 */
@AndroidEntryPoint
class SettingsFragment(private val mainIntent: Intent) : BaseFragment() {

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
        initializeViewModels()
        isBankActivateAction = mainIntent.action != null && mainIntent.action.equals("Activate_Bank")
        if (isBankActivateAction) {
            terminalId = mainIntent.getStringExtra("terminalID")
            merchantId = mainIntent.getStringExtra("merchantID")
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
        menuItems.add(MenuItem(getStrings(R.string.setup), {
            addTidMidFragment()
        }))
        menuItems.add(MenuItem(getStrings(R.string.host_settings), {
            addIPFragment()
        }))
        val menuFragment = ListMenuFragment.newInstance(menuItems,"Settings", true, R.drawable.token_logo_png)
        replaceFragment(menuFragment as Fragment)
    }

    /**
     * This prepares IP, Port fragment with validator, it shows initial IP, Port number on the screen
     * If user changes their values, these values are updated in database
     */
    private fun addIPFragment(){

        val inputList = mutableListOf<CustomInputFormat>()
        inputList.add(CustomInputFormat(getStrings(R.string.ip),EditTextInputType.IpAddress,
        null, getStrings(R.string.invalid_ip), InputValidator {
                validate(it)
            }))

        inputList.add(CustomInputFormat(
            getStrings(R.string.port), EditTextInputType.Number, 4, getStrings(R.string.invalid_port)
        ) { customInputFormat -> customInputFormat.text.length >= 2 && customInputFormat.text.toInt() > 0 })

        inputList[0].text = activationViewModel.hostIP()
        inputList[1].text = activationViewModel.hostPort()

        val hostFragment = InputListFragment.newInstance(inputList, getStrings(R.string.save),
            InputListFragment.ButtonListener{
                val ipNo = inputList[0].text
                val portNo = inputList[1].text
                activationViewModel.updateConnection(ipNo, portNo)
                popFragment()
            })

        replaceFragment(hostFragment as Fragment, true)
    }

    /**
     * This method is for activation, ui parameters are dummy, but it sets emv and call deviceInfo to sync with atms
     */
    private fun startActivation(){
        activationRoutine()
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Processing, getStrings(R.string.starting_activation), false)
        activationViewModel.getUiState().observe(safeActivity) { state ->
            when (state) {
                is ActivationViewModel.UIState.Starting -> showDialog(dialog)
                is ActivationViewModel.UIState.ParameterUploading -> dialog.update(InfoDialog.InfoType.Progress,getStrings(R.string.parameter_loading))
                is ActivationViewModel.UIState.MemberActCompleted -> dialog.update(InfoDialog.InfoType.Confirmed,getStrings(R.string.member_act_completed))
                is ActivationViewModel.UIState.RKLLoading -> dialog.update(InfoDialog.InfoType.Progress,getStrings(R.string.rkl_loading))
                is ActivationViewModel.UIState.RKLLoaded -> dialog.update(InfoDialog.InfoType.Confirmed,getStrings(R.string.rkl_loaded))
                is ActivationViewModel.UIState.KeyBlockLoading -> dialog.update(InfoDialog.InfoType.Progress,getStrings(R.string.key_block_loading))
                is ActivationViewModel.UIState.ActivationCompleted -> dialog.update(InfoDialog.InfoType.Confirmed,getStrings(R.string.activation_completed))
                is ActivationViewModel.UIState.Finished -> dialog.dismiss()
            }
        }
    }

    /**
     * It first check whether it's connected to the cardService before starting the activation process
     * It's starting to routine when it's connected
     */
    private fun activationRoutine(){
        if (cardViewModel.getCardServiceBinding() == null){
            connectCardService(true)
        }
        // when it's connected call setupRoutine
        cardViewModel.getCardServiceConnected().observe(safeActivity){
            CoroutineScope(Dispatchers.Default).launch {
                activationViewModel.setupRoutine(cardViewModel,safeActivity,terminalId,merchantId)
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
            getStrings(R.string.merchant_no),
            EditTextInputType.Number,
            10,
            getStrings(R.string.invalid_merchant_no)
        ) { input -> input.text.length == 10 })

        inputList.add(CustomInputFormat(
            getStrings(R.string.terminal_no),
            EditTextInputType.Text,
            8,
            getStrings(R.string.invalid_terminal_no)
        ) { input -> input.text.length == 8 })

        inputList[0].text = activationViewModel.merchantID()
        inputList[1].text = activationViewModel.terminalID()
        val tidMidFragment = InputListFragment.newInstance(inputList, getStrings(R.string.save),
            ){
            merchantId = inputList[0].text
            Log.d(getStrings(R.string.merchant_no),merchantId.toString())
            terminalId = inputList[1].text
            Log.d(getStrings(R.string.terminal_no),terminalId.toString())
            activationViewModel.updateActivation(terminalId, merchantId)
            startActivation()
            popFragment()
        }
        replaceFragment(tidMidFragment as Fragment, true)
    }
}
