package com.tokeninc.sardis.application_template.adapters

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tokeninc.sardis.application_template.ui.PostTxnFragment
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.database.entities.Transaction
import com.tokeninc.sardis.application_template.databinding.TransactionItemsBinding
import com.tokeninc.sardis.application_template.helpers.StringHelper

/**
 * This adapter arranges Void transactions one by one.
 */
class TransactionAdapter(private val transactionList: MutableList<Transaction?>): RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(val binding: TransactionItemsBinding): RecyclerView.ViewHolder(binding.root)

    var postTxnFragment: PostTxnFragment? = null

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
        hb.textCardNo.text = StringHelper().MaskTheCardNo(transaction!!.Col_PAN)
        hb.textDate.text = transaction.Col_TranDate
        if (transaction.Col_TransCode == 4 || transaction.Col_TransCode == 6)
            hb.textAmount.text = StringHelper().getAmount(transaction.Col_Amount2)
        else
            hb.textAmount.text = StringHelper().getAmount(transaction.Col_Amount)
        hb.textApprovalCode.text = transaction.Col_AuthCode
        hb.tvSN.text = transaction.Col_GUP_SN.toString()
        holder.itemView.setOnClickListener {
            postTxnFragment!!.voidOperation(transaction)
            Log.d("RecyclerView/onClick","ContentVal: $transaction ")
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

}