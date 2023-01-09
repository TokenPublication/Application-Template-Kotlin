package com.tokeninc.sardis.application_template.examples

import MenuItem
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.examples.viewmodels.ConfirmationDialogViewModel


class ConfirmationDialogFragment : Fragment(R.layout.fragment_confirmation_dialog), InfoDialogListener {

    private var menuItems = mutableListOf<IListMenuItem>()
    var exampleActivity: ExampleActivity? = null
    private var viewModel = ConfirmationDialogViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareData()
        viewModel.list = menuItems
        viewModel.replaceFragment(exampleActivity!!)
    }

    private fun prepareData() {
        menuItems.add(MenuItem("Confirmed", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Confirmed,
                "Confirmed",
                "Confirmation: Confirmed",
                InfoDialog.InfoDialogButtons.Both,
                99,
                exampleActivity!!
            )
        }) )
        menuItems.add(MenuItem("Warning", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Warning,
                "Warning",
                "Confirmation: Warning",
                InfoDialog.InfoDialogButtons.Both,
                98,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("Error", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Error,
                "Error",
                "Confirmation: Error",
                InfoDialog.InfoDialogButtons.Both,
                97,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("Info", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Info,
                "Info",
                "Confirmation: Info",
                InfoDialog.InfoDialogButtons.Both,
                96,
                exampleActivity!!
                ) }))
        menuItems.add(MenuItem("Declined", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Declined,
                "Declined",
                "Confirmation: Declined",
                InfoDialog.InfoDialogButtons.Both,
                95,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("Connecting", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Connecting,
                "Connecting",
                "Confirmation: Connecting",
                InfoDialog.InfoDialogButtons.Both,
                94,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("Downloading", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Downloading,
                "Downloading",
                "Confirmation: Downloading",
                InfoDialog.InfoDialogButtons.Both,
                93,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("Uploading", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Uploading,
                "Uploading",
                "Confirmation: Uploading",
                InfoDialog.InfoDialogButtons.Both,
                92,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("Processing", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Processing,
                "Processing",
                "Confirmation: Processing",
                InfoDialog.InfoDialogButtons.Both,
                91,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("Progress", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.Progress,
                "Progress",
                "Confirmation: Progress",
                InfoDialog.InfoDialogButtons.Both,
                90,
                exampleActivity!!
            )
        }))
        menuItems.add(MenuItem("None", { menuItem ->
            showConfirmationDialog(
                InfoDialog.InfoType.None,
                "None",
                "Confirmation: None",
                InfoDialog.InfoDialogButtons.Both,
                89,
                exampleActivity!!
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
            showInfoDialog(InfoDialog.InfoType.Confirmed, "Confirmed!", true)
        }
        //else if (arg == ***) { Do something else... }
        //else if (arg == ***) { Do something else... }
        if (arg == 98) {
            showInfoDialog(InfoDialog.InfoType.Warning, "Warning!", true)
        }
        if (arg == 97) {
            showInfoDialog(InfoDialog.InfoType.Error, "Error!", true)
        }
        if (arg == 96) {
            showInfoDialog(InfoDialog.InfoType.Info, "Info!", true)
        }
        if (arg == 95) {
            showInfoDialog(InfoDialog.InfoType.Declined, "Declined!", true)
        }
        if (arg == 94) {
            showInfoDialog(InfoDialog.InfoType.Connecting, "Connecting!", true)
        }
        if (arg == 93) {
            showInfoDialog(InfoDialog.InfoType.Downloading, "Downloading!", true)
        }
        if (arg == 92) {
            showInfoDialog(InfoDialog.InfoType.Uploading, "Uploading!", true)
        }
        if (arg == 91) {
            showInfoDialog(InfoDialog.InfoType.Processing, "Processing!", true)
        }
        if (arg == 90) {
            showInfoDialog(InfoDialog.InfoType.Progress, "Progress!", true)
        }
        if (arg == 89) {
            showInfoDialog(InfoDialog.InfoType.None, "None!", true)
        }
    }

    override fun canceled(arg: Int) {
        if (arg <= 99 || arg >= 89) {
            showInfoDialog(InfoDialog.InfoType.Error, "Canceled", true)
        }
    }
}