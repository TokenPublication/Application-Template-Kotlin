package com.tokeninc.sardis.application_template

import MenuItem
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.token.uicomponents.CustomInput.InputListFragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.databinding.FragmentDummySaleBinding
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding


class PostTxnFragment : Fragment() {
    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!

    private var menuFragment: ListMenuFragment? = null
    private var hostFragment: InputListFragment? = null

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
        showMenu()
    }


    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("İşlemler", {

        }))
        menuItems.add(MenuItem("İade", {
            addFragment(RefundFragment())
        }))
        menuItems.add(MenuItem("Grup Kapama", {

        }))
        menuItems.add(MenuItem("Örnekler", {

        }))
        menuFragment = ListMenuFragment.newInstance(menuItems,"PostTxn",
            true, R.drawable.token_logo)
        parentFragmentManager.beginTransaction().apply {
            replace(binding.container.id, menuFragment!!)
            commit()
        }
    }

    fun addFragment(fragment: Fragment){
        parentFragmentManager.beginTransaction().apply { //parent fragment manager instead support since it's a fragment
            replace(binding.container.id,fragment) //replacing fragment
            addToBackStack(null)  //add it to fragment stack, to return back as needed
            commit() //call signals to the FragmentManager that all operations have been added to the transaction
        }
    }

}