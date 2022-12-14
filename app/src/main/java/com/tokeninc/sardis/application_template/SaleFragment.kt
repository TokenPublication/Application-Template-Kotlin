package com.tokeninc.sardis.application_template

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.database.TransactionDB
import com.tokeninc.sardis.application_template.databinding.FragmentSaleBinding
import com.tokeninc.sardis.application_template.entities.ICCCard
import com.tokeninc.sardis.application_template.entities.MSRCard
import com.tokeninc.sardis.application_template.enums.CardReadType


class SaleFragment : Fragment() {
/*
    private var _binding: FragmentSaleBinding? = null
    private var amount = 0

    private var menuItemList: List<IListMenuItem>? = null
    private var cardICC: ICCCard? = null
    private var cardMSR: MSRCard? = null

    var transactionDB: TransactionDB? = null
    var saleBundle: Bundle? = null
    var saleIntent: Intent? = null

    var cardReadType = 0
    var cardData: String? = null
    var cardNumber = "**** ****"
    var cardOwner = ""

    protected var qrString = "QR Code Test"
    private val QRisSuccess = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSaleBinding.inflate(inflater,container,false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        amount = saleBundle!!.getInt("Amount")
        cardReadType = saleBundle!!.getInt("CardReadType")
        cardData = saleBundle!!.getString("CardData")

        if (cardReadType == CardReadType.NONE.type || cardReadType == CardReadType.ICC.type) {
            readCard()
        } else {
            //getCardDataFromBundle()
            prepareSaleMenu()
        }
    }

    private fun prepareSaleMenu() {
    }
 */

}