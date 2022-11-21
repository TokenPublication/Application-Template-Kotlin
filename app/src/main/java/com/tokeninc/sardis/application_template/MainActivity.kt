package com.tokeninc.sardis.application_template

import MenuItem
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.token.uicomponents.CustomInput.EditTextInputType
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialog.InfoDialogButtons
import com.token.uicomponents.infodialog.InfoDialogListener
import com.token.uicomponents.timeoutmanager.TimeOutActivity
import com.tokeninc.sardis.application_template.databinding.ActivityMainBinding
import java.lang.ref.WeakReference


class MainActivity : TimeOutActivity(), InfoDialogListener {

    //menu items is mutable list which we can add and delete
    private val menuItems = mutableListOf<IListMenuItem>()
    private var amount: Int = 0
    private val mContext: WeakReference<Context>? = null
    private val inputList = mutableListOf<CustomInputFormat>()

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR

        val textFragment = TextFragment()
        replaceFragment(R.id.container,textFragment)
        Log.w("Ne yazdiriyo","${getString(R.string.PosTxn_Action)}")
        when (intent.action){
            getString(R.string.PosTxn_Action) ->  textFragment.setActionName(getString(R.string.PosTxn_Action))
            getString(R.string.Sale_Action) ->  startDummySaleFragment(DummySaleFragment())
            getString(R.string.Settings_Action) ->  textFragment.setActionName(getString(R.string.Settings_Action))
            getString(R.string.BatchClose_Action) ->  textFragment.setActionName(getString(R.string.BatchClose_Action))
            getString(R.string.Parameter_Action) ->  textFragment.setActionName(getString(R.string.Parameter_Action))
            getString(R.string.Refund_Action) ->  textFragment.setActionName(getString(R.string.Refund_Action))
            else -> textFragment.setActionName("${intent.action}")
        }

    }


    fun startDummySaleFragment(dummySaleFragment: DummySaleFragment){
        amount = intent.extras!!.getInt("Amount")
        dummySaleFragment.setAmount(amount)
        dummySaleFragment.set_Context(this@MainActivity)
        dummySaleFragment.getNewBundle(bundleOf())
        dummySaleFragment.getNewIntent(Intent())
        replaceFragment(R.id.container,dummySaleFragment)
    }

    /**
     * This is for dummySaleFragmen's onSaleResponseRetrieved method
     * Because we use setResult method on Activities, we call this method in there.
     */
    fun dummySetResult(resultIntent: Intent){
        setResult(Activity.RESULT_OK,resultIntent)
        finish()
    }


    protected fun addCustomInputFormat(){
        inputList.add(
            CustomInputFormat(
                "Card Number",
                EditTextInputType.CreditCardNumber,
                null,
                "Invalid card number!",
                null
            )
        )
        inputList.add(
            CustomInputFormat(
                "Expire Date",
                EditTextInputType.ExpiryDate,
                null,
                null,
                null
            )
        )
        inputList.add(CustomInputFormat("CVV", EditTextInputType.CVV,
            null, null, null))

        val input = CustomInputFormat("IP", EditTextInputType.IpAddress, null, null, null)
        input.setText("123.456.789.1")

    }

    /**
     * Shows a dialog to the user which asks for a confirmation.
     * Dialog will be dismissed automatically when user taps on to confirm/cancel button.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, String, InfoDialog.InfoDialogButtons, int, InfoDialogListener)}
     */
    protected fun showConfirmationDialog(
        type: InfoDialog.InfoType,
        title: String,
        info: String,
        buttons: InfoDialog.InfoDialogButtons,
        arg: Int,
        listener: InfoDialogListener
    ):InfoDialog? {
        //created a dialog with InfoDialog newInstance method
        val dialog = InfoDialog.newInstance(type, title, info, buttons, arg, listener)
        dialog.show(supportFragmentManager, "")
        return dialog
    }

    /**
     * Shows a dialog to the user which informs the user about current progress.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, boolean)}
     * Dialog can dismissed by calling .dismiss() method of the fragment instance returned from this method.
     */
    protected fun showInfoDialog(
        type: InfoDialog.InfoType,
        text: String,
        isCancelable: Boolean
    ): InfoDialog? {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(supportFragmentManager, "")
        return fragment
    }

    /**
     * preparing some data to show in fragment, first create a list for sublist with menuitems
     * then add that sublist and currently created menuItems to menuItems mutable list
     */
    fun prepareData() {
        val subList1 = mutableListOf<IListMenuItem>()// Creating a mutable list for your sub menu items

        /* Your Sub List Items*/
        /* Your Sub List Items*/
        // adding some menu items to sublist
        subList1.add( MenuItem( "MenuItem 1", { menuItem: IListMenuItem? ->
            Toast.makeText(this, "Sub Menu 1", Toast.LENGTH_LONG).show() } )
        )

        subList1.add(
            MenuItem(
                "MenuItem 2",
                { menuItem ->
                    Toast.makeText(this, "Sub Menu 2", Toast.LENGTH_LONG).show()
                },
            ))

        subList1.add(
            MenuItem(
                "MenuItem 3",
                { menuItem ->
                    Toast.makeText(this, "Sub Menu 3", Toast.LENGTH_LONG).show()
                },
            ))

        // The menu item in to the List Menu Fragment
        // adding subList1 which we added before
        menuItems.add(
            MenuItem("Sub Menu (Error Alıyor)", subList1, null) )

        //adding some other menuitems which we defined there not before
        //a menu item which you call a confirmation dialog
        menuItems.add(MenuItem("Warning", { menuItem: IListMenuItem? ->
                    Toast.makeText(this, "Menu Item 1", Toast.LENGTH_LONG).show()
            showConfirmationDialog(
                InfoDialog.InfoType.Warning,
                "Warning",
                "Confirmation: Warning",
                InfoDialogButtons.Both,
                99,
                this@MainActivity)
                } ) )
        // a menu item which you call an info dialog
        menuItems.add(
            MenuItem("Connecting", { menuItem: IListMenuItem? ->
                    Toast.makeText(this, "Menu Item 2", Toast.LENGTH_LONG).show()
                showInfoDialog(InfoDialog.InfoType.Connecting, "Connecting", true);
                } ) )

        menuItems.add(
            MenuItem("Menu Item 3",
                {
                    Toast.makeText(this@MainActivity, "Menu Item 3", Toast.LENGTH_LONG).show()
                } ) )
    }

    /**
     * Returns time out value in seconds for activities which extend
     * @see TimeOutActivity
     * In case of any user interaction, timeout timer will be reset.
     *
     * If any activity will not have time out,
     * override this method from that activity and @return '0'.
     */
    override fun getTimeOutSec(): Int {
        return 60
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * add fragment method is for simplifying adding fragments
     * with this method you won't waste your time with supporfragment manager methods.
     */
    protected fun addFragment(@IdRes resourceId: Int, fragment: Fragment, addToBackStack: Boolean)
    {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.add(resourceId, fragment)
        if (addToBackStack) {
            ft.addToBackStack("")
        }
        ft.commit()
    }

    protected fun replaceFragment(@IdRes resourceId: Int, fragment: Fragment)
    {
        supportFragmentManager.beginTransaction().apply {
            replace(resourceId,fragment) //replacing fragment
            addToBackStack(null)  //add it to fragment stack, to return back as needed
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

    protected fun removeFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.remove(fragment)
        ft.commit()
    }

    override fun confirmed(arg: Int) {
        if (arg == 99) {
            //  Do something else...
        }
        //else if (arg == ***) { Do something else... }
    }

    override fun canceled(arg: Int) {
        if (arg == 99) {
            //  Do something else...
        }
        //else if (arg == ***) { Do something else... }
    }
}