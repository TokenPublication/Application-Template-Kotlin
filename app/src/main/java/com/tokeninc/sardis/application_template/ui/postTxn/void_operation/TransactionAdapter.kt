package com.tokeninc.sardis.application_template.ui.postTxn.void_operation

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.data.database.transaction.Transaction
import com.tokeninc.sardis.application_template.data.model.resultCode.TransactionCode
import com.tokeninc.sardis.application_template.databinding.TransactionItemsBinding
import com.tokeninc.sardis.application_template.ui.postTxn.slip.SlipFragment
import com.tokeninc.sardis.application_template.utils.StringHelper

/**
 * This adapter arranges Void transactions one by one.
 */
class TransactionAdapter(private val transactionList: MutableList<Transaction?>, private val isVoid: Boolean): RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(val binding: TransactionItemsBinding): RecyclerView.ViewHolder(binding.root)

    lateinit var voidFragment: VoidFragment
    lateinit var slipFragment: SlipFragment

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: TransactionItemsBinding = DataBindingUtil.inflate(inflater, R.layout.transaction_items, parent,false)
        return TransactionViewHolder(binding)
    }

    /** In here their views are set
     * If the user clicks one of those items, selected Void transaction are started to occur.
     */
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        val hb = holder.binding
        hb.textCardNo.text = StringHelper().maskTheCardNo(transaction!!.Col_PAN)
        hb.textDate.text = transaction.Col_TranDate
        if (transaction.Col_TransCode == TransactionCode.MATCHED_REFUND.type || transaction.Col_TransCode == TransactionCode.INSTALLMENT_REFUND.type)
            hb.textAmount.text = StringHelper().getAmount(transaction.Col_Amount2!!)
        else
            hb.textAmount.text = StringHelper().getAmount(transaction.Col_Amount)
        hb.textApprovalCode.text = transaction.Col_AuthCode
        hb.tvSN.text = transaction.Col_GUP_SN.toString()
        holder.itemView.setOnClickListener {
            if (isVoid){
                voidFragment.doVoid(transaction)
            } else{
                slipFragment.prepareSlip(transaction)
            }
            Log.i("RecyclerView/onClick","ContentVal: $transaction ")
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }
}
