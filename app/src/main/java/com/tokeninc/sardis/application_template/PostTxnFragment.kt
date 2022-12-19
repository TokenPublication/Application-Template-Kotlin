package com.tokeninc.sardis.application_template

import MenuItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.token.uicomponents.ListMenuFragment.IListMenuItem
import com.token.uicomponents.ListMenuFragment.ListMenuFragment
import com.tokeninc.sardis.application_template.databinding.FragmentPostTxnBinding
import org.json.JSONException
import org.json.JSONObject


class PostTxnFragment : Fragment() {
    private var _binding: FragmentPostTxnBinding? = null
    private val binding get() = _binding!!

    private var menuFragment: ListMenuFragment? = null
    var mainActivity: MainActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentPostTxnBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showMenu()
    }


    private fun showMenu(){
        var menuItems = mutableListOf<IListMenuItem>()
        menuItems.add(MenuItem("İptal", {
            //mainActivity!!.startVoidFragment(VoidFragment())
            // TODO ReadCard
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

    private fun readCard() {
        val obj = JSONObject()
        try {
            obj.put("forceOnline", 0)
            obj.put("zeroAmount", 1)
            obj.put("showAmount", 0)
            obj.put("partialEMV", 1)
            // TODO Developer: Check from Allowed Operations Parameter
            val isManEntryAllowed = true
            val isCVVAskedOnMoto = true
            val isFallbackAllowed = true
            val isQrAllowed = true
            obj.put("keyIn", if (isManEntryAllowed) 1 else 0)
            obj.put("askCVV", if (isCVVAskedOnMoto) 1 else 0)
            obj.put("fallback", if (isFallbackAllowed) 1 else 0)
            obj.put("qrPay", if (isQrAllowed) 1 else 0)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
       // cardServiceBinding.getCard(0, 30, obj.toString())
    }

}