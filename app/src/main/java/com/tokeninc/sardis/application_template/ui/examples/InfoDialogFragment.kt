package com.tokeninc.sardis.application_template.ui.examples

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IAuthenticator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.ListMenuFragment.MenuItemClickListener
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialog.InfoType
import com.tokeninc.sardis.application_template.R

/** It can be deleted
 * This fragment includes Info Dialog methods for example activity
 */
class InfoDialogFragment(private val exampleFragment: ExampleFragment) : Fragment(R.layout.fragment_info_dialog) {

    class InfoDialogItem: IListMenuItem {

        var mType: InfoType
        var mText: String
        var mListener: MenuItemClickListener<*>
        var mAuthenticator: IAuthenticator? = null

        constructor(type: InfoType, text: String, listener: MenuItemClickListener<*>, authenticator: IAuthenticator?) {
            mType = type
            mText = text
            mAuthenticator = authenticator
            mListener = listener
        }
        override fun getName(): String {
            return mText
        }
        override fun getSubMenuItemList(): MutableList<IListMenuItem>? {
            return null
        }
        override fun getClickListener(): MenuItemClickListener<*> {
            return mListener
        }
        override fun getAuthenticator(): IAuthenticator? {
            return mAuthenticator
        }
    }

    private var menuItems = mutableListOf<IListMenuItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareData()
        val fragment = ListMenuFragment.newInstance(menuItems, exampleFragment.getStrings(R.string.info_dialog), false, R.drawable.token_logo_png)
        exampleFragment.mainActivity.replaceFragment(fragment as Fragment)
    }

    private fun prepareData() {
        val listener = MenuItemClickListener<InfoDialogItem>{
                item -> showPopup(item)
        }
        menuItems.add(InfoDialogItem(InfoType.Confirmed, exampleFragment.getStrings(R.string.confirmed), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Warning, exampleFragment.getStrings(R.string.warning), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Error, exampleFragment.getStrings(R.string.error), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Info, exampleFragment.getStrings(R.string.info), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Declined, exampleFragment.getStrings(R.string.declined), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Connecting, exampleFragment.getStrings(R.string.connecting), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Downloading, exampleFragment.getStrings(R.string.downloading), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Uploading, exampleFragment.getStrings(R.string.uploading), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Processing, exampleFragment.getStrings(R.string.processing), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Progress, exampleFragment.getStrings(R.string.progress), listener, null))
        menuItems.add(InfoDialogItem(InfoType.None, exampleFragment.getStrings(R.string.none), listener, null))
    }

    private fun showPopup(item: InfoDialogItem) {
        val dialog: InfoDialog? = showInfoDialog(item.mType, item.mText, true)
        //Dismiss dialog by calling dialog.dismiss() when needed.
    }

    private fun showInfoDialog(type: InfoDialog.InfoType, text: String, isCancelable: Boolean
    ): InfoDialog? {
        val fragment = InfoDialog.newInstance(type, text, isCancelable)
        fragment.show(parentFragmentManager, "")
        return fragment
    }
}
