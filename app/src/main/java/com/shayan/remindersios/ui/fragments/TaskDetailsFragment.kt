package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.shayan.remindersios.R
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentTaskDetailsBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

class TaskDetailsFragment : Fragment() {

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

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }

    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun displayTaskDetails() {
        val task = arguments?.getParcelable<Tasks>("task")

        task?.let {
            bindTaskDetails(it)
        } ?: showTaskNotAvailableMessage()
    }

    private fun bindTaskDetails(task: Tasks) {
        binding.apply {
            // Set title
            tvTitle.text = task.title

            // Set notes with styling based on availability
            tvNotes.text = task.notes.takeIf { !it.isNullOrBlank() }
                ?: getString(R.string.not_available)
            tvNotes.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (task.notes.isNullOrBlank()) R.color.darker_gray else R.color.grey
                )
            )

            // Set other task details
            tvDate.text = task.date ?: getString(R.string.not_available)
            tvTime.text = task.time ?: getString(R.string.not_available)
            tvFlag.text = if (task.flag) getString(R.string.yes) else getString(R.string.no)
            tvCompleted.text =
                if (task.isCompleted) getString(R.string.yes) else getString(R.string.no)
        }
    }

    private fun showTaskNotAvailableMessage() {
        binding.apply {
            tvTitle.text = getString(R.string.not_available)
            tvNotes.text = ""
            tvDate.text = ""
            tvTime.text = ""
            tvFlag.text = ""
            tvCompleted.text = ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
