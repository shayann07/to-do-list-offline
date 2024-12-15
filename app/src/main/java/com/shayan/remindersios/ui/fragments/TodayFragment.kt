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
import com.shayan.remindersios.databinding.FragmentTodayBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

class TodayFragment : Fragment(), TaskAdapter.TaskCompletionListener {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var morningAdapter: TaskAdapter
    private lateinit var afternoonAdapter: TaskAdapter
    private lateinit var tonightAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Initialize RecyclerViews
        setupRecyclerViews()

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        // Observe LiveData
        observeLiveData()

        // Fetch today's tasks
        viewModel.fetchTodayTasks()
    }

    private fun setupRecyclerViews() {
        binding.recyclerMorning.layoutManager = LinearLayoutManager(context)
        binding.recyclerAfternoon.layoutManager = LinearLayoutManager(context)
        binding.recyclerTonight.layoutManager = LinearLayoutManager(context)

        // Initialize Adapters with click listeners
        morningAdapter = createTaskAdapter()
        afternoonAdapter = createTaskAdapter()
        tonightAdapter = createTaskAdapter()

        binding.recyclerMorning.adapter = morningAdapter
        binding.recyclerAfternoon.adapter = afternoonAdapter
        binding.recyclerTonight.adapter = tonightAdapter
    }

    private fun createTaskAdapter(): TaskAdapter {
        return TaskAdapter(completionListener = this,
            itemClickListener = object : TaskAdapter.OnItemClickListener {
                override fun onItemClick(task: Tasks) {
                    // Navigate to TaskDetailsFragment
                    val bundle = Bundle().apply {
                        putParcelable("task", task) // Pass the task object
                    }
                    findNavController().navigate(R.id.taskDetailsFragment, bundle)
                }
            },
            deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                override fun onDeleteClick(task: Tasks) {
                    // Handle task deletion
                    viewModel.deleteTask(task.roomTaskId)
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun observeLiveData() {
        viewModel.morningTasksLiveData.observe(viewLifecycleOwner) { morningTasks ->
            morningAdapter.submitList(morningTasks)
            binding.recyclerMorning.visibility =
                if (morningTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.afternoonTasksLiveData.observe(viewLifecycleOwner) { afternoonTasks ->
            afternoonAdapter.submitList(afternoonTasks)
            binding.recyclerAfternoon.visibility =
                if (afternoonTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.tonightTasksLiveData.observe(viewLifecycleOwner) { tonightTasks ->
            tonightAdapter.submitList(tonightTasks)
            binding.recyclerTonight.visibility =
                if (tonightTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clean up binding to avoid memory leaks
    }
}