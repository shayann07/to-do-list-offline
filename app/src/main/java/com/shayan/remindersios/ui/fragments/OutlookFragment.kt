package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.shayan.remindersios.R
import com.shayan.remindersios.databinding.FragmentOutlookBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import com.shayan.remindersios.utils.PullToRefreshUtil
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment for displaying and managing Outlook-related tasks.
 */
class OutlookFragment : Fragment() {

    // region View Binding
    private var _binding: FragmentOutlookBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel
    private lateinit var viewModel: ViewModel
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOutlookBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView. Sets up ViewModel, UI elements, and pull-to-refresh.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupUI()
    }

    /**
     * Cleans up resources when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region ViewModel Setup
    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }
    // endregion

    // region UI Setup
    private fun setupUI() {
        setupBackButton()
        setupPullToRefresh()
    }

    /**
     * Replaces the default back-press with NavController's navigateUp().
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * Configures pull-to-refresh functionality using the Ultra Pull-To-Refresh library.
     */
    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            viewModel.fetchTotalTasks()
        }
    }
    // endregion
}