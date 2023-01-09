package com.tokeninc.sardis.application_template.examples

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IAuthenticator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.MenuItemClickListener
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialog.InfoType
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.examples.viewmodels.InfoDialogViewModel


class InfoDialogFragment : Fragment(R.layout.fragment_info_dialog) {

    var exampleActivity: ExampleActivity? = null
    private var viewModel = InfoDialogViewModel()

    class InfoDialogItem: IListMenuItem {

        lateinit var mType: InfoType
        lateinit var mText: String
        lateinit var mListener: MenuItemClickListener<*>
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
        viewModel.list = menuItems
        viewModel.replaceFragment(exampleActivity!!)
    }


    private fun prepareData() {
        val listener = MenuItemClickListener<InfoDialogItem>{
                item -> showPopup(item)
        }
        menuItems.add(InfoDialogItem(InfoType.Confirmed, "Confirmed", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Warning, "Warning", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Error, "Error", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Info, "Info", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Declined, "Declined", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Connecting, "Connecting", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Downloading, "Downloading", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Uploading, "Uploading", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Processing, "Processing", listener, null))
        menuItems.add(InfoDialogItem(InfoType.Progress, "Progress", listener, null))
        menuItems.add(InfoDialogItem(InfoType.None, "None", listener, null))
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