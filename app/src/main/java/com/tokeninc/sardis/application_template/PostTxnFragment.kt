package com.tokeninc.sardis.application_template

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding


class PostTxnFragment : Fragment() {
    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickButtons()
    }

    fun clickButtons(){
        binding.btnIade.setOnClickListener {
            parentFragmentManager.beginTransaction().apply { //parent fragment manager instead support since it's a fragment
                replace(binding.frcontainer.id,RefundFragment()) //replacing fragment
                addToBackStack(null)  //add it to fragment stack, to return back as needed
                commit() //call signals to the FragmentManager that all operations have been added to the transaction
            }
            }
        }

}