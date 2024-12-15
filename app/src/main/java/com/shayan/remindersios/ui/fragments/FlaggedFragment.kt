package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shayan.remindersios.R
import com.shayan.remindersios.adapters.TaskAdapter
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentFlaggedBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

class FlaggedFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {
    private var _binding: FragmentFlaggedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var flaggedAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlaggedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.flaggedRecycler.layoutManager = LinearLayoutManager(requireContext())
        flaggedAdapter = createTaskAdapter()
        binding.flaggedRecycler.adapter = flaggedAdapter

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchFlaggedTasks()
        viewModel.flaggedTasks.observe(viewLifecycleOwner) { flaggedTasks ->
            flaggedAdapter.submitList(flaggedTasks)
            binding.flaggedRecycler.visibility =
                if (flaggedTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun createTaskAdapter(): TaskAdapter {
        return TaskAdapter(
            completionListener = this,
            itemClickListener = this,
            deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                override fun onDeleteClick(task: Tasks) {
                    // Handle task deletion
                    viewModel.deleteTask(task.roomTaskId)
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }


    override fun onTaskCompletionToggled(
        roomTaskId: Int, isCompleted: Boolean
    ) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onItemClick(task: Tasks) {
        // Navigate to Task Details Fragment
        val bundle = Bundle().apply {
            putParcelable("task", task)
        }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
