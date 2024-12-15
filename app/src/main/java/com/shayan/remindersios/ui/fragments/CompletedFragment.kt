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
import com.shayan.remindersios.databinding.FragmentCompletedBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

class CompletedFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {
    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var completedAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.completedRecycler.layoutManager = LinearLayoutManager(requireContext())
        completedAdapter = createTaskAdapter()
        binding.completedRecycler.adapter = completedAdapter

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchCompletedTasks()
        viewModel.completedTasks.observe(viewLifecycleOwner) { completedTasks ->
            completedAdapter.submitList(completedTasks)
            binding.completedRecycler.visibility =
                if (completedTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        binding.clearCompleted.setOnClickListener {
            viewModel.deleteCompletedTasks()
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