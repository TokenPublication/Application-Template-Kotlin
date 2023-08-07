package com.tokeninc.sardis.application_template.ui.trigger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.token.uicomponents.infodialog.InfoDialog
import com.tokeninc.sardis.application_template.MainActivity
import com.tokeninc.sardis.application_template.databinding.FragmentTriggerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TriggerFragment(val mainActivity: MainActivity) : Fragment() {

    private var _binding: FragmentTriggerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TriggerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTriggerBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(this)[TriggerViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startDummyParameterUploading() //TODO do it in model, just update UI
    }

    /**
     * This function uploads parameters in IO thread, and update UI in main thread dynamically while uploading parameters.
     * After parameters are uploaded successfully it sends the result with intents to finish the main activity.
     */
    private fun startDummyParameterUploading(){
        val assetManager = mainActivity.assets
        CoroutineScope(Dispatchers.Default).launch {
            viewModel.parameterRoutine(assetManager)
        }
        val dialog = InfoDialog.newInstance(InfoDialog.InfoType.Progress,"Parameters are uploading",false)
        viewModel.getUiState().observe(mainActivity) { state ->
            when (state) {
                is TriggerViewModel.TriggerUIState.Loading -> mainActivity.showDialog(dialog)
                is TriggerViewModel.TriggerUIState.Success -> dialog.update(InfoDialog.InfoType.Confirmed,"Parameters are uploaded successfully")
            }
        }
        viewModel.getLiveIntent().observe(mainActivity){liveIntent ->
            mainActivity.setResult(liveIntent)
        }
    }

    /**
     * This is for avoiding memory leak for binding
     */
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
