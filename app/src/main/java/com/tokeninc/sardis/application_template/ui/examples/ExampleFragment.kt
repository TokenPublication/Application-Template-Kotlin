package com.tokeninc.sardis.application_template.ui.examples

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.components330.qr_screen_330.QrScreen330
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.numpad.NumPadDialog
import com.token.uicomponents.numpad.NumPadListener
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.model.type.DeviceModel
import com.tokeninc.sardis.application_template.databinding.FragmentExampleBinding
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.utils.BaseFragment
import com.tokeninc.sardis.application_template.utils.StringHelper
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintHelper
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is the class for showing how to simulate examples on this device to developers
 * This package can be deleted by the developer.
 */
@AndroidEntryPoint
class ExampleFragment(): BaseFragment() {

    private lateinit var binding: FragmentExampleBinding

    private var qrAmount = 100
    private var qrString = "QR Code Test"
    private var menuItems = mutableListOf<IListMenuItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentExampleBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModels()
        prepareData()
        val fragment = ListMenuFragment.newInstance(menuItems, getStrings(R.string.examples), true, R.drawable.token_logo_png)
        replaceFragment(fragment as Fragment)
    }

    private fun prepareData() {
        val subList1 = mutableListOf<IListMenuItem>()
        subList1.add(MenuItem( "MenuItem 1", {
            Toast.makeText(safeActivity, "Sub Menu 1", Toast.LENGTH_LONG).show() }
            )
        )
        subList1.add(
            MenuItem("MenuItem 2", {
                    Toast.makeText(safeActivity, "Sub Menu 2", Toast.LENGTH_LONG).show()
                },
            )
        )
        subList1.add(
            MenuItem("MenuItem 3", {
                    Toast.makeText(safeActivity, "Sub Menu 3", Toast.LENGTH_LONG).show()
                },
            )
        )
        menuItems.add(MenuItem(getStrings(R.string.sub_menu), subList1, null) )

        menuItems.add(MenuItem(getStrings(R.string.custom_input_list), {
            val customInputListFragment = CustomInputListFragment()
            replaceFragment(customInputListFragment, true)
        }))
        menuItems.add(MenuItem(getStrings(R.string.info_dialog), {
            val infoDialogFragment = InfoDialogFragment()
            replaceFragment(infoDialogFragment, true)
        }))
        menuItems.add(MenuItem(getStrings(R.string.confirmation_dialog), {
            val confirmationDialogFragment = ConfirmationDialogFragment()
            replaceFragment(confirmationDialogFragment, true)
        }))
        menuItems.add(MenuItem(getStrings(R.string.device_info),{
            /*    [Device Info](https://github.com/TokenPublication/DeviceInfoClientApp)    */
            val deviceInfo = DeviceInfo(safeActivity.applicationContext)
            deviceInfo.getFields(
                { fields: Array<String>? ->
                    if (fields == null) return@getFields
                    Log.d("Example 0", "Fiscal ID:   " + fields[0])
                    Log.d("Example 1", "IMEI Number: " + fields[1])
                    Log.d("Example 2", "IMSI Number: " + fields[2])
                    Log.d("Example 3", "Modem Version : " + fields[3])
                    Log.d("Example 4", "LYNX Number: " + fields[4])
                    Log.d("Example 5", "POS Mode: " + fields[5])
                    showInfoDialog(
                        InfoDialog.InfoType.Info,
                        """
                        Fiscal ID: ${fields[0]}
                        IMEI Number: ${fields[1]}
                        IMSI Number: ${fields[2]}
                        Modem Version: ${fields[3]}
                        Lynx Version: ${fields[4]}
                        Pos Mode: ${fields[5]}
                        """.trimIndent(), true
                    )
                    deviceInfo.unbind()
                },  // write requested fields
                DeviceInfo.Field.FISCAL_ID,
                DeviceInfo.Field.IMEI_NUMBER,
                DeviceInfo.Field.IMSI_NUMBER,
                DeviceInfo.Field.MODEM_VERSION,
                DeviceInfo.Field.LYNX_VERSION,
                DeviceInfo.Field.OPERATION_MODE
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.num_pad), {
            val dialog = NumPadDialog.newInstance(object : NumPadListener {
                override fun enter(pin: String) {}
                override fun onCanceled() {
                    //Num pad canceled callback
                }
            }, getStrings(R.string.enter_pin), 8)
            dialog.show(safeActivity.supportFragmentManager, "Num Pad")
        }))
        menuItems.add(MenuItem(getStrings(R.string.show_qr), {
            //val deviceModel = mainActivity.getDeviceModel()
            //if (deviceModel == DeviceModel.TR330.name) {
            val deviceModel = Build.MODEL
            Log.i("Example QR","device model: $deviceModel")
            if (Build.MODEL.equals("330TRS")|| Build.MODEL.equals("330TR")){
                val qrScreen330 = QrScreen330.newInstance(
                    StringHelper().getAmount(qrAmount),
                    "",
                    getString(R.string.waiting_qr_read),
                    qrString
                )
                replaceFragment(qrScreen330)
            } else{
                val dialog = showInfoDialog(InfoDialog.InfoType.Progress, getStrings(R.string.qr_loading), true)
                // For detailed usage; SaleActivity
                cardViewModel.initializeCardServiceBinding(appCompatActivity)
                cardViewModel.getCardServiceConnected().observe(safeActivity) { isConnected ->
                    // cardService is connected before 10 seconds (which is the limit of the timer)
                    if (isConnected) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            cardViewModel.getCardServiceBinding()!!.showQR(getString(R.string.please_read_qr),
                                    StringHelper().getAmount(qrAmount), qrString) // Shows QR on the back screen
                            dialog.setQr(qrString, getString(R.string.waiting_qr_read))
                        }, 2000)
                    }
                }
            }


        }))
        val subListPrint = mutableListOf<IListMenuItem>()
        subListPrint.add(MenuItem(getStrings(R.string.print_success), {
            PrintHelper().printSuccess(safeActivity.applicationContext) // Message print: Load Success
        }))
        subListPrint.add(MenuItem(getStrings(R.string.print_error), {
            PrintHelper().printError(safeActivity.applicationContext) // Message print: Load Error
        }))
        subListPrint.add(MenuItem("Print Contactless 32", {
            PrintHelper().printContactless(true,safeActivity.applicationContext)
        }))
        subListPrint.add(MenuItem("Print Contactless 64", {
            PrintHelper().printContactless(false,safeActivity.applicationContext)
        }))
        subListPrint.add(MenuItem("Print Visa", {
            PrintHelper().printVisa(safeActivity.applicationContext)
        }))
        menuItems.add(MenuItem(getStrings(R.string.print_functions), subListPrint, null))
    }

}
