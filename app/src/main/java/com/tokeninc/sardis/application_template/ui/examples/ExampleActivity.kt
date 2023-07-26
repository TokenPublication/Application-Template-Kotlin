package com.tokeninc.sardis.application_template.ui.examples

import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.token.printerlib.PrinterService
import com.token.printerlib.StyledString
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.numpad.NumPadDialog
import com.token.uicomponents.numpad.NumPadListener
import com.token.uicomponents.timeoutmanager.TimeOutActivity
import com.tokeninc.cardservicebinding.CardServiceBinding
import com.tokeninc.cardservicebinding.CardServiceListener
import com.tokeninc.deviceinfo.DeviceInfo
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.ui.examples.viewModels.ExampleViewModel
import com.tokeninc.sardis.application_template.utils.StringHelper
import com.tokeninc.sardis.application_template.utils.printHelpers.PrintHelper

/**
 * This is the class for showing how to simulate examples on this device to developers
 * This package can be deleted by the developer.
 */
class ExampleActivity: TimeOutActivity(), InfoDialogListener,CardServiceListener {

    private val timeOut = 60
    private var qrAmount = 100
    private var qrString = "QR Code Test"
    private var menuItems = mutableListOf<IListMenuItem>()
    private val viewModel = ExampleViewModel()
    private lateinit var cardServiceBinding: CardServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)
        prepareData()
        cardServiceBinding = CardServiceBinding(this, this)
        viewModel.list = menuItems
        viewModel.replaceFragment(this)
    }

    fun print(printText: String?) {
        val styledText = StyledString()
        styledText.addStyledText(printText)
        styledText.finishPrintingProcedure()
        styledText.print(PrinterService.getService(applicationContext))
    }

    private fun addFragment(fragment: Fragment)
    {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.container, fragment)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun replaceFragment( fragment: Fragment)
    {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container,fragment) //replacing fragment
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun prepareData() {
        val subList1 = mutableListOf<IListMenuItem>()
        subList1.add(MenuItem( "MenuItem 1", {
            Toast.makeText(this, "Sub Menu 1", Toast.LENGTH_LONG).show() } ))
        subList1.add(
            MenuItem(
            "MenuItem 2",
                {
                    Toast.makeText(this, "Sub Menu 2", Toast.LENGTH_LONG).show()
                },
            )
        )
        subList1.add(
            MenuItem(
            "MenuItem 3",
                {
                    Toast.makeText(this, "Sub Menu 3", Toast.LENGTH_LONG).show()
                },
            )
        )
        menuItems.add(MenuItem("Sub Menu", subList1, null) )

        menuItems.add(MenuItem("Custom Input List", {
            val customInputListFragment = CustomInputListFragment()
            customInputListFragment.exampleActivity = this
            addFragment(customInputListFragment)
        }))
        menuItems.add(MenuItem("Info Dialog", {
            val infoDialogFragment = InfoDialogFragment()
            infoDialogFragment.exampleActivity = this
            addFragment(infoDialogFragment)
        }))
        menuItems.add(MenuItem("Confirmation Dialog", {
            val confirmationDialogFragment = ConfirmationDialogFragment()
            confirmationDialogFragment.exampleActivity = this
            addFragment(confirmationDialogFragment)
        }))
        menuItems.add(MenuItem("Device Info",{
            /*    [Device Info](https://github.com/TokenPublication/DeviceInfoClientApp)    */
            val deviceInfo = DeviceInfo(applicationContext)
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
        menuItems.add(MenuItem("Num Pad", {
            val dialog = NumPadDialog.newInstance(object : NumPadListener {
                override fun enter(pin: String) {}
                override fun onCanceled() {
                    //Num pad canceled callback
                }
            }, "Please enter PIN", 8)
            dialog.show(supportFragmentManager, "Num Pad")
        }))
        menuItems.add(MenuItem("Show QR", {
            val dialog = showInfoDialog(InfoDialog.InfoType.Progress, "QR Loading", true)
            // For detailed usage; SaleActivity
            cardServiceBinding.showQR(
                "PLEASE READ THE QR CODE",
                StringHelper().getAmount(qrAmount),
                qrString
            ) // Shows QR on the back screen
            dialog.setQr(qrString, "WAITING FOR THE QR CODE") // Shows the same QR on Info Dialog
        }))
        val subListPrint = mutableListOf<IListMenuItem>()
        subListPrint.add(MenuItem("Print Load Success", {
            print(PrintHelper().printSuccess()) // Message print: Load Success
        }))
        subListPrint.add(MenuItem("Print Load Error", {
            print(PrintHelper().printError()) // Message print: Load Error
        }))
        menuItems.add(MenuItem("Print Functions", subListPrint, null))
    }

    /**
     * Shows a dialog to the user which informs the user about current progress.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, boolean)}
     * Dialog can dismissed by calling .dismiss() method of the fragment instance returned from this method.
     */
    private fun showInfoDialog(
        type: InfoDialog.InfoType,
        text: String,
        isCancelable: Boolean
    ): InfoDialog {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(supportFragmentManager, "")
        return fragment as InfoDialog
    }

    override fun getTimeOutSec() = timeOut

    override fun confirmed(arg: Int) {
        if (arg == 99) {
            Toast.makeText(this, "Confirmed.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun canceled(arg: Int) {
        if (arg == 99) {
            Toast.makeText(this, "Canceled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCardServiceConnected() {
    }

    override fun onCardDataReceived(p0: String?) {
    }

    override fun onPinReceived(p0: String?) {
    }

    override fun onICCTakeOut() {
    }

}