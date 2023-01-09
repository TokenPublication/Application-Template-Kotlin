package com.tokeninc.sardis.application_template

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tokeninc.sardis.application_template.databinding.FragmentTextBinding
import com.tokeninc.sardis.application_template.examples.ExampleActivity


class TextFragment : Fragment() {

    var mainActivity: MainActivity? = null
    private var _binding: FragmentTextBinding? = null
    private val binding get() = _binding!!

    companion object{
        var actionName : String? = "actionname"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTextBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvActionName.text = actionName
    }


    /**
     * this is for changing action name
     */
    fun setActionName(mActionName: String?){
        actionName = mActionName
    }

    /**
     * This is for avoiding memory leak for binding
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}