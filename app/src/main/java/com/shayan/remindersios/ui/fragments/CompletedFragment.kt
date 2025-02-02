package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.shayan.remindersios.utils.shortToast
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment to display and manage completed tasks.
 */
class CompletedFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    // region View Binding
    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel & Adapter
    private lateinit var viewModel: ViewModel
    private lateinit var completedAdapter: TaskAdapter
    // endregion

    // region Lifecycle Methods
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

        // 1) Initialize the ViewModel
        initViewModel()

        // 2) Set up all UI elements
        setupUI()

        // 3) Observe completed tasks from the ViewModel
        observeCompletedTasks()
    }

    /**
     * Cleans up resources when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region ViewModel
    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchCompletedTasks()
    }
    // endregion

    // region UI Setup
    /**
     * Aggregates all UI initialization calls.
     */
    private fun setupUI() {
        setupBackButton()
        setupRecyclerView()
        setupClearCompletedButton()
        setupPullToRefresh()
    }

    /**
     * Configures the back button to navigate up in the NavController stack.
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigateUp()
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
     * Sets up the button to clear all completed tasks.
     */
    private fun setupClearCompletedButton() {
        binding.clearCompleted.setOnClickListener {
            viewModel.deleteCompletedTasks()
            context?.shortToast("All completed tasks cleared")
        }
    }

    /**
     * Configures Pull-to-Refresh using Ultra Pull-To-Refresh library.
     */
    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            // Refresh the completed tasks
            viewModel.fetchCompletedTasks()
        }
    }
    // endregion

    // region Observers
    /**
     * Observes the completed tasks LiveData and updates the UI.
     */
    private fun observeCompletedTasks() {
        viewModel.completedTasks.observe(viewLifecycleOwner) { completedTasks ->
            completedAdapter.submitList(completedTasks)
            handleEmptyState(completedTasks.isNullOrEmpty())
        }
    }

    /**
     * Manages the visibility of the RecyclerView based on whether the list is empty.
     */
    private fun handleEmptyState(isEmpty: Boolean) {
        binding.completedRecycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.noRemindersTV.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
    // endregion

    // region TaskAdapter Setup & Callbacks
    /**
     * Creates a new TaskAdapter with all relevant listeners.
     */
    private fun createTaskAdapter(): TaskAdapter {
        return TaskAdapter(completionListener = this,
            itemClickListener = this,
            deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                override fun onDeleteClick(task: Tasks) {
                    viewModel.deleteTask(task.roomTaskId)
                    context?.shortToast("Task deleted")
                }
            })
    }

    /**
     * Called when a task's completion status is toggled.
     */
    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            val toastMessage = if (success) "Task updated" else "Failed to update: $message"
            context?.shortToast(toastMessage)
        }
    }

    /**
     * Navigates to the Task Details screen when a task is clicked.
     */
    override fun onItemClick(task: Tasks) {
        val bundle = Bundle().apply { putParcelable("task", task) }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }
    // endregion
}