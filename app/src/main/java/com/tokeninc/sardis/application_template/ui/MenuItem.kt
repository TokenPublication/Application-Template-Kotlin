package com.tokeninc.sardis.application_template.ui

import com.token.uicomponents.ListMenuFragment.IAuthenticator
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.MenuItemClickListener

/** thanks to @JvmOverloads we don't need to define different 3 constructor for cases one
of parameters is null. So in main constructor we defined our nullable parameters' defined values as null
 and with this way, if user call this class without corresponding values there will be assigned as null.
 I changed sub menu item list type from list to mutable list to write and delete elements
 I also inherited get methods not necessarily because this class inherited from IListMenuItem
 * @param title is string title not null
 * @param listener nullable listener
 * @param subMenuItemList nullable item list of sub menu
 * @param authenticator nullable authenticator
 */
class MenuItem @JvmOverloads constructor(
    private val title: String,
    private val listener: MenuItemClickListener<*>?,
    private val subMenuItemList: MutableList<IListMenuItem>? = null,
    private val authenticator: IAuthenticator? = null
) : IListMenuItem {

    //this is for cases only the sub menu item list is null, if we don't define there
    //in cases with only 1 null item, kotlin behaves like this item was authenticator not the itemlist
    constructor(
        title: String,
        listener: MenuItemClickListener<*>?,
        authenticator: IAuthenticator?
    ): this(title, listener, null, authenticator){
    }

    //for cases when listener is null
    constructor(
        title: String,
        subMenuItemList : MutableList<IListMenuItem>?,
        authenticator: IAuthenticator?
    ): this(title, null,subMenuItemList ,authenticator) {
    }

    override fun getName(): String {
       return title
    }

    override fun getSubMenuItemList(): MutableList<IListMenuItem>? {
        return subMenuItemList
    }

    override fun getClickListener(): MenuItemClickListener<*>? {
        return listener
    }

    override fun getAuthenticator(): IAuthenticator? {
        return authenticator
    }

}