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
import com.shayan.remindersios.utils.PullToRefreshUtil
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment to display and manage completed tasks.
 */
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

    /**
     * Initializes UI components, sets up ViewModel, and observes data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRecyclerView()
        initializeViewModel()
        observeCompletedTasks()
        setupClearCompletedButton()
        // Setup Pull-to-Refresh
        setupPullToRefresh()
    }

    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            // Fetch data here
            viewModel.fetchCompletedTasks()
        }
    }

    /**
     * Configures the back button to navigate to the previous screen.
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    /**
     * Sets up the RecyclerView and initializes the TaskAdapter.
     */
    private fun setupRecyclerView() {
        binding.completedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            completedAdapter = createTaskAdapter()
            adapter = completedAdapter
        }
    }

    /**
     * Initializes the ViewModel for this fragment.
     */
    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchCompletedTasks()
    }

    /**
     * Observes completed tasks from the ViewModel and updates the UI.
     */
    private fun observeCompletedTasks() {
        viewModel.completedTasks.observe(viewLifecycleOwner) { completedTasks ->
            completedAdapter.submitList(completedTasks)
            handleEmptyState(completedTasks.isNullOrEmpty())
        }
    }

    /**
     * Handles the visibility of the RecyclerView and empty state message.
     *
     * @param isEmpty Whether the completed tasks list is empty.
     */
    private fun handleEmptyState(isEmpty: Boolean) {
        binding.completedRecycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    /**
     * Sets up the button to clear all completed tasks.
     */
    private fun setupClearCompletedButton() {
        binding.clearCompleted.setOnClickListener {
            viewModel.deleteCompletedTasks()
            Toast.makeText(requireContext(), "All completed tasks cleared", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Creates and returns a TaskAdapter with required listeners.
     */
    private fun createTaskAdapter(): TaskAdapter {
        return TaskAdapter(completionListener = this,
            itemClickListener = this,
            deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                override fun onDeleteClick(task: Tasks) {
                    viewModel.deleteTask(task.roomTaskId)
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Handles task completion toggle and updates the database.
     */
    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Navigates to the Task Details screen when a task is clicked.
     */
    override fun onItemClick(task: Tasks) {
        val bundle = Bundle().apply {
            putParcelable("task", task)
        }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }

    /**
     * Cleans up resources when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}