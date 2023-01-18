package com.tokeninc.sardis.application_template

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokeninc.sardis.application_template.adapters.TransactionAdapter
import com.tokeninc.sardis.application_template.databinding.ListTransactionBinding
import com.tokeninc.sardis.application_template.viewmodels.TransactionViewModel


class TransactionList : Fragment() {

    private lateinit var adapter: TransactionAdapter
    private lateinit var binding: ListTransactionBinding
    var viewModel: TransactionViewModel? = null
    var postTxnFragment: PostTxnFragment? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =ListTransactionBinding.inflate(inflater,container,false)
        val recyclerView = binding.recyclerViewTransactions
        recyclerView.layoutManager =LinearLayoutManager(requireContext())

        viewModel!!.createLiveData() //in here list = getTransactionsByCardNo(cardNo)
        viewModel!!.list.observe(viewLifecycleOwner) { //TODO iadede iade tutarı değil org tutarı bastırıyor
            adapter = TransactionAdapter(it.toMutableList())
            adapter.postTxnFragment = postTxnFragment
            binding.adapter = adapter
        }
        return binding.root
    }

}