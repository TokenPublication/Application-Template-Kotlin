package com.tokeninc.sardis.application_template.ui.postTxn.void_operation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokeninc.sardis.application_template.ui.sale.TransactionViewModel
import com.tokeninc.sardis.application_template.databinding.ListTransactionBinding
import com.tokeninc.sardis.application_template.ui.postTxn.PostTxnFragment

/**
 * This is the Fragment that holds all the Void transactions in recyclerView, transactions one by one are set in TransactionAdapter
 */ //TODO VoidFragment ve methodları taşı buraya
class TransactionList(private var cardNumber: String?, private val viewModel: TransactionViewModel,
                      private val postTxnFragment: PostTxnFragment) : Fragment() {

    private lateinit var adapter: VoidAdapter
    private lateinit var binding: ListTransactionBinding

    /**
     * Recycler view is prepared while view is creating.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =ListTransactionBinding.inflate(inflater,container,false)
        val recyclerView = binding.recyclerViewTransactions
        recyclerView.layoutManager =LinearLayoutManager(requireContext())
        viewModel.createLiveData(cardNumber) //list = getTransactionsByCardNo(cardNo)
        viewModel.list.observe(viewLifecycleOwner) {
            adapter = VoidAdapter(it.toMutableList())
            adapter.postTxnFragment = postTxnFragment
            binding.adapter = adapter
        }
        return binding.root
    }
}
