package com.tokeninc.sardis.application_template.ui.examples

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.numpad.NumPadDialog
import com.token.uicomponents.numpad.NumPadListener
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentExampleBinding
import com.tokeninc.sardis.application_template.ui.sale.CardViewModel
import com.tokeninc.sardis.application_template.utils.StringHelper
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintHelper

/**
 * This is the class for showing how to simulate examples on this device to developers
 * This package can be deleted by the developer.
 */
class ExampleFragment(val mainActivity: MainActivity, private val cardViewModel: CardViewModel): Fragment(), InfoDialogListener {

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
        prepareData()
        val fragment = ListMenuFragment.newInstance(menuItems, getStrings(R.string.examples), false, R.drawable.token_logo_png)
        mainActivity.replaceFragment(fragment as Fragment)
    }

    fun print(printText: String?) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(mainActivity.applicationContext))
    }

    private fun prepareData() {
        val subList1 = mutableListOf<IListMenuItem>()
        subList1.add(MenuItem( "MenuItem 1", {
            Toast.makeText(mainActivity, "Sub Menu 1", Toast.LENGTH_LONG).show() } ))
        subList1.add(
            MenuItem(
            "MenuItem 2",
                {
                    Toast.makeText(mainActivity, "Sub Menu 2", Toast.LENGTH_LONG).show()
                },
            )
        )
        subList1.add(
            MenuItem(
            "MenuItem 3",
                {
                    Toast.makeText(mainActivity, "Sub Menu 3", Toast.LENGTH_LONG).show()
                },
            )
        )
        menuItems.add(MenuItem(getStrings(R.string.sub_menu), subList1, null) )

        menuItems.add(MenuItem(getStrings(R.string.custom_input_list), {
            val customInputListFragment = CustomInputListFragment(this)
            mainActivity.addFragment(customInputListFragment)
        }))
        menuItems.add(MenuItem(getStrings(R.string.info_dialog), {
            val infoDialogFragment = InfoDialogFragment(this)
            mainActivity.addFragment(infoDialogFragment)
        }))
        menuItems.add(MenuItem(getStrings(R.string.confirmation_dialog), {
            val confirmationDialogFragment = ConfirmationDialogFragment(this)
            mainActivity.addFragment(confirmationDialogFragment)
        }))
        menuItems.add(MenuItem(getStrings(R.string.device_info),{
            /*    [Device Info](https://github.com/TokenPublication/DeviceInfoClientApp)    */
            val deviceInfo = DeviceInfo(mainActivity.applicationContext)
            deviceInfo.getFields(
                { fields: Array<String>? ->
                    if (fields == null) return@getFields
                    Log.d("Example 0", "Fiscal ID:   " + fields[0])
                    Log.d("Example 1", "IMEI Number: " + fields[1])
                    Log.d("Example 2", "IMSI Number: " + fields[2])
                    Log.d("Example 3", "Modem Version : " + fields[3])
                    Log.d("Example 4", "LYNX Number: " + fields[4])
                    Log.d("Example 5", "POS Mode: " + fields[5])
                    mainActivity.showInfoDialog(
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
            dialog.show(mainActivity.supportFragmentManager, "Num Pad")
        }))
        menuItems.add(MenuItem(getStrings(R.string.show_qr), {
            val dialog = mainActivity.showInfoDialog(InfoDialog.InfoType.Progress, getStrings(R.string.qr_loading), true)
            // For detailed usage; SaleActivity
            cardViewModel.initializeCardServiceBinding(mainActivity)
            cardViewModel.getCardServiceConnected().observe(mainActivity) { isConnected ->
                // cardService is connected before 10 seconds (which is the limit of the timer)
                if (isConnected) {
                    cardViewModel.getCardServiceBinding()!!.showQR(getStrings(R.string.please_read_qr),
                        StringHelper().getAmount(qrAmount),
                        qrString) //TODO bak
                }
            }
            dialog!!.setQr(qrString, getStrings(R.string.waiting_qr_read)) // Shows the same QR on Info Dialog
        }))
        val subListPrint = mutableListOf<IListMenuItem>()
        subListPrint.add(MenuItem(getStrings(R.string.print_success), {
            print(PrintHelper().printSuccess()) // Message print: Load Success
        }))
        subListPrint.add(MenuItem(getStrings(R.string.print_error), {
            print(PrintHelper().printError()) // Message print: Load Error
        }))
        subListPrint.add(MenuItem("Print Contactless 32", {
            print(PrintHelper().printContactless(true,mainActivity.applicationContext))
        }))
        subListPrint.add(MenuItem("Print Contactless 64", {
            print(PrintHelper().printContactless(false,mainActivity.applicationContext))
        }))
        subListPrint.add(MenuItem("Print Visa", {
            print(PrintHelper().printVisa(mainActivity.applicationContext))
        }))
        menuItems.add(MenuItem(getStrings(R.string.print_functions), subListPrint, null))
    }

    override fun confirmed(arg: Int) {
        if (arg == 99) {
            Toast.makeText(mainActivity, getStrings(R.string.confirmed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun canceled(arg: Int) {
        if (arg == 99) {
            Toast.makeText(mainActivity, getStrings(R.string.cancelled), Toast.LENGTH_SHORT).show()
        }
    }
    fun getStrings(resID: Int): String{
        return mainActivity.getString(resID)
    }
}
