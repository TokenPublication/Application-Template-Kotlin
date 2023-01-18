package com.tokeninc.sardis.application_template.adapters

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.PostTxnFragment
import com.tokeninc.sardis.application_template.R
import com.tokeninc.sardis.application_template.database.transaction.TransactionCol
import com.tokeninc.sardis.application_template.databinding.TransactionItemsBinding
import com.tokeninc.sardis.application_template.helpers.StringHelper

class TransactionAdapter(private val transactionList: MutableList<ContentValues?>): RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(val binding: TransactionItemsBinding): RecyclerView.ViewHolder(binding.root)

    var postTxnFragment: PostTxnFragment? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: TransactionItemsBinding = DataBindingUtil.inflate(inflater, R.layout.transaction_items, parent,false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        val hb = holder.binding
        hb.textCardNo.text = StringHelper().MaskTheCardNo(transaction!!.getAsString(TransactionCol.Col_PAN.name))
        hb.textDate.text = transaction.getAsString(TransactionCol.Col_TranDate.name)
        hb.textAmount.text = StringHelper().getAmount(transaction.getAsString(TransactionCol.Col_Amount.name).toInt())
        hb.textApprovalCode.text = transaction.getAsString(TransactionCol.Col_AuthCode.name)
        hb.tvSN.text = transaction.getAsString(TransactionCol.Col_GUP_SN.name)
        holder.itemView.setOnClickListener {
            postTxnFragment!!.voidOperation(transaction)
            Log.d("RecyclerView/onClick","ContentVal: ${transaction.toString()} ")
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

}