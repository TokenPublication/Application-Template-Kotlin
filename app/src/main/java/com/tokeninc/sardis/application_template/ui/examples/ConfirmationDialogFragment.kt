package com.tokeninc.sardis.application_template.ui.examples

import android.os.BaseBundle
import com.tokeninc.sardis.application_template.utils.objects.MenuItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialogListener
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentConfirmationDialogBinding
import com.tokeninc.sardis.application_template.databinding.FragmentCustomInputListBinding
import com.tokeninc.sardis.application_template.utils.BaseFragment

/** It can be deleted
 * This fragment includes Confirmation Dialog methods for example activity
 */
class ConfirmationDialogFragment() : BaseFragment(), InfoDialogListener {

    private lateinit var binding: FragmentConfirmationDialogBinding
    private var menuItems = mutableListOf<IListMenuItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentConfirmationDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareData()
        val fragment = ListMenuFragment.newInstance(menuItems, getStrings(R.string.confirmation), false, R.drawable.token_logo_png)
        replaceFragment(fragment as Fragment)
    }

    private fun prepareData() {
        menuItems.add(MenuItem(getStrings(R.string.confirmed), {
            showConfirmationDialog(
                InfoDialog.InfoType.Confirmed,
                getStrings(R.string.confirmed),
                getStrings(R.string.confirmation)+": "+getStrings(R.string.confirmed),
                InfoDialog.InfoDialogButtons.Both,
                99,
                this
            )
        }) )
        menuItems.add(MenuItem(getStrings(R.string.warning), {
            showConfirmationDialog(
                InfoDialog.InfoType.Warning,
                getStrings(R.string.warning),
                getStrings(R.string.confirmation)+": "+getStrings(R.string.warning),
                InfoDialog.InfoDialogButtons.Both,
                98,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.error), {
            showConfirmationDialog(
                InfoDialog.InfoType.Error,
                getStrings(R.string.error),
                getStrings(R.string.confirmation)+": "+getStrings(R.string.error),
                InfoDialog.InfoDialogButtons.Both,
                97,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.info), {
            showConfirmationDialog(
                InfoDialog.InfoType.Info,
                getStrings(R.string.info),
                getStrings(R.string.confirmation)+": " +getStrings(R.string.info),
                InfoDialog.InfoDialogButtons.Both,
                96,
                this
                ) }))
        menuItems.add(MenuItem(getStrings(R.string.declined), {
            showConfirmationDialog(
                InfoDialog.InfoType.Declined,
                getStrings(R.string.declined),
                getStrings(R.string.confirmation)+": " +getStrings(R.string.declined),
                InfoDialog.InfoDialogButtons.Both,
                95,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.connecting), {
            showConfirmationDialog(
                InfoDialog.InfoType.Connecting,
                getStrings(R.string.connecting),
                getStrings(R.string.confirmation)+": " +getStrings(R.string.connecting),
                InfoDialog.InfoDialogButtons.Both,
                94,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.downloading), {
            showConfirmationDialog(
                InfoDialog.InfoType.Downloading,
                getStrings(R.string.downloading),
                getStrings(R.string.confirmation)+": " +getStrings(R.string.downloading),
                InfoDialog.InfoDialogButtons.Both,
                93,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.uploading), {
            showConfirmationDialog(
                InfoDialog.InfoType.Uploading,
                getStrings(R.string.uploading),
                getStrings(R.string.confirmation)+": " +getStrings(R.string.uploading),
                InfoDialog.InfoDialogButtons.Both,
                92,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.processing), {
            showConfirmationDialog(
                InfoDialog.InfoType.Processing,
                getStrings(R.string.processing),
                getStrings(R.string.confirmation)+": " +getStrings(R.string.processing),
                InfoDialog.InfoDialogButtons.Both,
                91,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.progress), {
            showConfirmationDialog(
                InfoDialog.InfoType.Progress,
                getStrings(R.string.progress),
                getStrings(R.string.confirmation)+": " +getStrings(R.string.progress),
                InfoDialog.InfoDialogButtons.Both,
                90,
                this
            )
        }))
        menuItems.add(MenuItem(getStrings(R.string.none), {
            showConfirmationDialog(
                InfoDialog.InfoType.None,
                getStrings(R.string.none),
                getStrings(R.string.confirmation)+": " + getStrings(R.string.none),
                InfoDialog.InfoDialogButtons.Both,
                89,
                this
            ) }))
    }


    override fun confirmed(arg: Int) {
        if (arg == 99) {
            showInfoDialog(InfoDialog.InfoType.Confirmed, getStrings(R.string.confirmed)+"!", true)
        }
        //else if (arg == ***) { Do something else... }
        //else if (arg == ***) { Do something else... }
        if (arg == 98) {
            showInfoDialog(InfoDialog.InfoType.Warning, getStrings(R.string.warning)+"!", true)
        }
        if (arg == 97) {
            showInfoDialog(InfoDialog.InfoType.Error, getStrings(R.string.error)+"!", true)
        }
        if (arg == 96) {
            showInfoDialog(InfoDialog.InfoType.Info, getStrings(R.string.info)+"!", true)
        }
        if (arg == 95) {
            showInfoDialog(InfoDialog.InfoType.Declined, getStrings(R.string.declined)+"!", true)
        }
        if (arg == 94) {
            showInfoDialog(InfoDialog.InfoType.Connecting, getStrings(R.string.connecting)+"!", true)
        }
        if (arg == 93) {
            showInfoDialog(InfoDialog.InfoType.Downloading, getStrings(R.string.downloading)+"!", true)
        }
        if (arg == 92) {
            showInfoDialog(InfoDialog.InfoType.Uploading, getStrings(R.string.uploading)+"!", true)
        }
        if (arg == 91) {
            showInfoDialog(InfoDialog.InfoType.Processing, getStrings(R.string.processing)+"!", true)
        }
        if (arg == 90) {
            showInfoDialog(InfoDialog.InfoType.Progress, getStrings(R.string.progress)+"!", true)
        }
        if (arg == 89) {
            showInfoDialog(InfoDialog.InfoType.None, getStrings(R.string.none)+"!", true)
        }
    }

    override fun canceled(arg: Int) {
        if (arg <= 99 || arg >= 89) {
            showInfoDialog(InfoDialog.InfoType.Error, getStrings(R.string.cancelled), true)
        }
    }
}
