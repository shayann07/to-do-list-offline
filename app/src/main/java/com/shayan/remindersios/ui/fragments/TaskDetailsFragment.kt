package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.shayan.remindersios.R
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentTaskDetailsBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

class TaskDetailsFragment : Fragment() {

    // View binding for accessing layout elements
    private var _binding: FragmentTaskDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViewModel()
        setupBackButton()
        displayTaskDetails()
    }

    // Initialize the ViewModel instance
    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }

    // Set up the back button's functionality
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // Display task details in the UI
    private fun displayTaskDetails() {
        val task = arguments?.getParcelable<Tasks>("task")

        if (task != null) {
            // Display task title
            binding.tvTitle.text = task.title

            // Display task notes with appropriate styling
            binding.tvNotes.apply {
                text = task.notes.takeIf { !it.isNullOrBlank() }
                    ?: getString(R.string.notes_not_available)
                setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (task.notes.isNullOrBlank()) R.color.darker_gray else R.color.grey
                    )
                )
            }

            // Display task date, time, flag, and completion status
            binding.tvDate.text = task.date ?: "Not available"
            binding.tvTime.text = task.time ?: "Not available"
            binding.tvFlag.text = if (task.flag) "Yes" else "No"
            binding.tvCompleted.text = if (task.isCompleted) "Yes" else "No"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks by clearing the binding
    }
}
