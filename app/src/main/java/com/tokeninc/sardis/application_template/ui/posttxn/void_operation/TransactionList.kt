package com.tokeninc.sardis.application_template.ui.posttxn.void_operation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokeninc.sardis.application_template.utils.ContentValHelper
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.databinding.ListTransactionBinding
import com.tokeninc.sardis.application_template.ui.posttxn.PostTxnFragment

/**
 * This is the Fragment that holds all the Void transactions in recyclerView, transactions one by one are set in TransactionAdapter
 */
class TransactionList(var cardNumber: String?) : Fragment() {

    private lateinit var adapter: VoidAdapter
    private lateinit var binding: ListTransactionBinding
    var viewModel: TransactionViewModel? = null
    var postTxnFragment: PostTxnFragment? = null

    /**
     * Recycler view is prepared while view is creating.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =ListTransactionBinding.inflate(inflater,container,false)
        val recyclerView = binding.recyclerViewTransactions
        recyclerView.layoutManager =LinearLayoutManager(requireContext())
        val contentValHelper = ContentValHelper()
        //TODO iadede iade tutarı değil org tutarı bastırıyor
        viewModel!!.createLiveData(cardNumber) //in here list = getTransactionsByCardNo(cardNo)
        viewModel!!.list.observe(viewLifecycleOwner) {
            adapter = VoidAdapter(it.toMutableList())
            adapter.postTxnFragment = postTxnFragment
            binding.adapter = adapter
        }
        return binding.root
    }

}