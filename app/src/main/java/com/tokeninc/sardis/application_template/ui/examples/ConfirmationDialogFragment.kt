package com.tokeninc.sardis.application_template.ui.examples

import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.tokeninc.sardis.application_template.R

/** It can be deleted
 * This fragment includes Confirmation Dialog methods for example activity
 */
class ConfirmationDialogFragment(private val exampleFragment: ExampleFragment) : Fragment(R.layout.fragment_confirmation_dialog), InfoDialogListener {

    private var menuItems = mutableListOf<IListMenuItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareData()
        val fragment = ListMenuFragment.newInstance(menuItems, exampleFragment.getStrings(R.string.confirmation), false, R.drawable.token_logo_png)
        exampleFragment.mainActivity.replaceFragment(fragment as Fragment)
    }

    private fun prepareData() {
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.confirmed), {
            showConfirmationDialog(
                InfoDialog.InfoType.Confirmed,
                exampleFragment.getStrings(R.string.confirmed),
                exampleFragment.getStrings(R.string.confirmation)+": "+exampleFragment.getStrings(R.string.confirmed),
                InfoDialog.InfoDialogButtons.Both,
                99,
                exampleFragment
            )
        }) )
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.warning), {
            showConfirmationDialog(
                InfoDialog.InfoType.Warning,
                exampleFragment.getStrings(R.string.warning),
                exampleFragment.getStrings(R.string.confirmation)+": "+exampleFragment.getStrings(R.string.warning),
                InfoDialog.InfoDialogButtons.Both,
                98,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.error), {
            showConfirmationDialog(
                InfoDialog.InfoType.Error,
                exampleFragment.getStrings(R.string.error),
                exampleFragment.getStrings(R.string.confirmation)+": "+exampleFragment.getStrings(R.string.error),
                InfoDialog.InfoDialogButtons.Both,
                97,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.info), {
            showConfirmationDialog(
                InfoDialog.InfoType.Info,
                exampleFragment.getStrings(R.string.info),
                exampleFragment.getStrings(R.string.confirmation)+": " +exampleFragment.getStrings(R.string.info),
                InfoDialog.InfoDialogButtons.Both,
                96,
                exampleFragment
                ) }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.declined), {
            showConfirmationDialog(
                InfoDialog.InfoType.Declined,
                exampleFragment.getStrings(R.string.declined),
                exampleFragment.getStrings(R.string.confirmation)+": " +exampleFragment.getStrings(R.string.declined),
                InfoDialog.InfoDialogButtons.Both,
                95,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.connecting), {
            showConfirmationDialog(
                InfoDialog.InfoType.Connecting,
                exampleFragment.getStrings(R.string.connecting),
                exampleFragment.getStrings(R.string.confirmation)+": " +exampleFragment.getStrings(R.string.connecting),
                InfoDialog.InfoDialogButtons.Both,
                94,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.downloading), {
            showConfirmationDialog(
                InfoDialog.InfoType.Downloading,
                exampleFragment.getStrings(R.string.downloading),
                exampleFragment.getStrings(R.string.confirmation)+": " +exampleFragment.getStrings(R.string.downloading),
                InfoDialog.InfoDialogButtons.Both,
                93,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.uploading), {
            showConfirmationDialog(
                InfoDialog.InfoType.Uploading,
                exampleFragment.getStrings(R.string.uploading),
                exampleFragment.getStrings(R.string.confirmation)+": " +exampleFragment.getStrings(R.string.uploading),
                InfoDialog.InfoDialogButtons.Both,
                92,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.processing), {
            showConfirmationDialog(
                InfoDialog.InfoType.Processing,
                exampleFragment.getStrings(R.string.processing),
                exampleFragment.getStrings(R.string.confirmation)+": " +exampleFragment.getStrings(R.string.processing),
                InfoDialog.InfoDialogButtons.Both,
                91,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.progress), {
            showConfirmationDialog(
                InfoDialog.InfoType.Progress,
                exampleFragment.getStrings(R.string.progress),
                exampleFragment.getStrings(R.string.confirmation)+": " +exampleFragment.getStrings(R.string.progress),
                InfoDialog.InfoDialogButtons.Both,
                90,
                exampleFragment
            )
        }))
        menuItems.add(MenuItem(exampleFragment.getStrings(R.string.none), {
            showConfirmationDialog(
                InfoDialog.InfoType.None,
                exampleFragment.getStrings(R.string.none),
                exampleFragment.getStrings(R.string.confirmation)+": " + exampleFragment.getStrings(R.string.none),
                InfoDialog.InfoDialogButtons.Both,
                89,
                exampleFragment
            ) }))
    }

    /**
     * Shows a dialog to the user which asks for a confirmation.
     * Dialog will be dismissed automatically when user taps on to confirm/cancel button.
     * See {@link InfoDialog#newInstance(InfoDialog.InfoType, String, String, InfoDialog.InfoDialogButtons, int, InfoDialogListener)}
     */
    private fun showConfirmationDialog(
        type: InfoDialog.InfoType,
        title: String,
        info: String,
        buttons: InfoDialog.InfoDialogButtons,
        arg: Int,
        listener: InfoDialogListener
    ): InfoDialog? {
        //created a dialog with InfoDialog newInstance method
        val dialog = InfoDialog.newInstance(type, title, info, buttons, arg, listener)
        dialog.show(parentFragmentManager, "")
        return dialog
    }

    private fun showInfoDialog(
        type: InfoDialog.InfoType,
        text: String,
        isCancelable: Boolean
    ): InfoDialog? {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(parentFragmentManager, "")
        return fragment
    }

    override fun confirmed(arg: Int) {
        if (arg == 99) {
            showInfoDialog(InfoDialog.InfoType.Confirmed, exampleFragment.getStrings(R.string.confirmed)+"!", true)
        }
        //else if (arg == ***) { Do something else... }
        //else if (arg == ***) { Do something else... }
        if (arg == 98) {
            showInfoDialog(InfoDialog.InfoType.Warning, exampleFragment.getStrings(R.string.warning)+"!", true)
        }
        if (arg == 97) {
            showInfoDialog(InfoDialog.InfoType.Error, exampleFragment.getStrings(R.string.error)+"!", true)
        }
        if (arg == 96) {
            showInfoDialog(InfoDialog.InfoType.Info, exampleFragment.getStrings(R.string.info)+"!", true)
        }
        if (arg == 95) {
            showInfoDialog(InfoDialog.InfoType.Declined, exampleFragment.getStrings(R.string.declined)+"!", true)
        }
        if (arg == 94) {
            showInfoDialog(InfoDialog.InfoType.Connecting, exampleFragment.getStrings(R.string.connecting)+"!", true)
        }
        if (arg == 93) {
            showInfoDialog(InfoDialog.InfoType.Downloading, exampleFragment.getStrings(R.string.downloading)+"!", true)
        }
        if (arg == 92) {
            showInfoDialog(InfoDialog.InfoType.Uploading, exampleFragment.getStrings(R.string.uploading)+"!", true)
        }
        if (arg == 91) {
            showInfoDialog(InfoDialog.InfoType.Processing, exampleFragment.getStrings(R.string.processing)+"!", true)
        }
        if (arg == 90) {
            showInfoDialog(InfoDialog.InfoType.Progress, exampleFragment.getStrings(R.string.progress)+"!", true)
        }
        if (arg == 89) {
            showInfoDialog(InfoDialog.InfoType.None, exampleFragment.getStrings(R.string.none)+"!", true)
        }
    }

    override fun canceled(arg: Int) {
        if (arg <= 99 || arg >= 89) {
            showInfoDialog(InfoDialog.InfoType.Error, exampleFragment.getStrings(R.string.cancelled), true)
        }
    }
}
