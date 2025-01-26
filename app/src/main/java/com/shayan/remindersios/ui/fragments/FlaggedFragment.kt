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
import com.shayan.remindersios.utils.PullToRefreshUtil
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment to display and manage flagged tasks.
 */
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

    /**
     * Initializes UI components, sets up the RecyclerView, and observes ViewModel data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRecyclerView()
        initializeViewModel()
        observeFlaggedTasks()
        // Setup Pull-to-Refresh
        setupPullToRefresh()
    }

    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            // Fetch data here
            viewModel.fetchFlaggedTasks()
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
        binding.flaggedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            flaggedAdapter = createTaskAdapter()
            adapter = flaggedAdapter
        }
    }

    /**
     * Initializes the ViewModel for this fragment.
     */
    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchFlaggedTasks()
    }

    /**
     * Observes flagged tasks from the ViewModel and updates the UI.
     */
    private fun observeFlaggedTasks() {
        viewModel.flaggedTasks.observe(viewLifecycleOwner) { flaggedTasks ->
            flaggedAdapter.submitList(flaggedTasks)
            handleEmptyState(flaggedTasks.isNullOrEmpty())
        }
    }

    /**
     * Handles the visibility of the RecyclerView and empty state message.
     *
     * @param isEmpty Whether the flagged tasks list is empty.
     */
    private fun handleEmptyState(isEmpty: Boolean) {
        binding.flaggedRecycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    /**
     * Creates and returns a TaskAdapter with the required listeners.
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
     * Handles toggling the completion status of a task.
     *
     * @param roomTaskId The ID of the task to update.
     * @param isCompleted True if the task is marked as completed, false otherwise.
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
     *
     * @param task The clicked task object.
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