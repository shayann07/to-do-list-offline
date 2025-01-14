package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        binding.backToHomeBtn.setOnClickListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set("refreshTasks", true)
            findNavController().navigateUp()
        }

        displayTaskDetails()
    }

    private fun displayTaskDetails() {
        val task = arguments?.getParcelable<Tasks>("task")
        task?.let {
            bindTaskDetails(it)
        } ?: showTaskNotAvailableMessage()
    }

    private fun bindTaskDetails(task: Tasks) {
        binding.apply {
            tvTitle.text = task.title
            tvNotes.text = task.notes ?: getString(R.string.not_available)
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

