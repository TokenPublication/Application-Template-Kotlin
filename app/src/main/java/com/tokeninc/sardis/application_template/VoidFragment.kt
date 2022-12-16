package com.tokeninc.sardis.application_template

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.tokeninc.sardis.application_template.adapters.TransactionAdapter
import com.tokeninc.sardis.application_template.databinding.FragmentVoidBinding
import com.tokeninc.sardis.application_template.viewmodel.TransactionViewModel


class VoidFragment : Fragment() {

    private lateinit var adapter: TransactionAdapter
    private lateinit var binding: FragmentVoidBinding
    var viewModel: TransactionViewModel? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =FragmentVoidBinding.inflate(inflater,container,false)
        val recyclerView = binding.recyclerViewTransactions
        recyclerView.layoutManager =LinearLayoutManager(requireContext())

        viewModel!!.createLiveData() //in here list = getAllTransactions()
        viewModel!!.list.observe(viewLifecycleOwner) {
            adapter = TransactionAdapter(it.toMutableList())
            binding.adapter = adapter
            adapter.notifyDataSetChanged() //gerekli mi bilmiyorum
        }

        return binding.root
    }

}