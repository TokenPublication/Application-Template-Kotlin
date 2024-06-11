package com.tokeninc.sardis.application_template.ui.examples

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IAuthenticator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.token.uicomponents.ListMenuFragment.MenuItemClickListener
import com.token.uicomponents.infodialog.InfoDialog
import com.token.uicomponents.infodialog.InfoDialog.InfoType
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.databinding.FragmentInfoDialogBinding
import com.tokeninc.sardis.application_template.databinding.FragmentSettingsBinding
import com.tokeninc.sardis.application_template.utils.BaseFragment

/** It can be deleted
 * This fragment includes Info Dialog methods for example activity
 */
class InfoDialogFragment() : BaseFragment() {

    private var _binding: FragmentInfoDialogBinding? = null
    private val binding get() = _binding!!
    class InfoDialogItem: IListMenuItem {

        var mType: InfoType
        var mText: String
        private var mListener: MenuItemClickListener<*>
        private var mAuthenticator: IAuthenticator? = null

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoDialogBinding.inflate(inflater,container,false)
        return binding.root
    }

    private var menuItems = mutableListOf<IListMenuItem>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareData()
        val fragment = ListMenuFragment.newInstance(menuItems, getStrings(R.string.info_dialog), true, R.drawable.token_logo_png)
        replaceFragment(fragment as Fragment)
    }

    private fun prepareData() {
        val listener = MenuItemClickListener<InfoDialogItem>{
                item -> showPopup(item)
        }
        menuItems.add(InfoDialogItem(InfoType.Confirmed, getStrings(R.string.confirmed), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Warning, getStrings(R.string.warning), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Error, getStrings(R.string.error), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Info, getStrings(R.string.info), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Declined, getStrings(R.string.declined), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Connecting, getStrings(R.string.connecting), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Downloading, getStrings(R.string.downloading), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Uploading, getStrings(R.string.uploading), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Processing, getStrings(R.string.processing), listener, null))
        menuItems.add(InfoDialogItem(InfoType.Progress, getStrings(R.string.progress), listener, null))
        menuItems.add(InfoDialogItem(InfoType.None, getStrings(R.string.none), listener, null))
    }

    private fun showPopup(item: InfoDialogItem) {
        val dialog: InfoDialog = showInfoDialog(item.mType, item.mText, true)
        //Dismiss dialog by calling dialog.dismiss() when needed.
    }

}
