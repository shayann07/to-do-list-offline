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
import com.shayan.remindersios.databinding.FragmentFlaggedBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import com.shayan.remindersios.utils.PullToRefreshUtil
import com.shayan.remindersios.utils.shortToast
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment to display and manage flagged tasks.
 */
class FlaggedFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    // region View Binding
    private var _binding: FragmentFlaggedBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel & Adapter
    private lateinit var viewModel: ViewModel
    private lateinit var flaggedAdapter: TaskAdapter
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlaggedBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Initializes UI components, ViewModel, and observes data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Initialize the ViewModel
        initViewModel()

        // 2) Setup UI (RecyclerView, Back Button, Pull-To-Refresh, etc.)
        setupUI()

        // 3) Observe flagged tasks
        observeFlaggedTasks()
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
        viewModel.fetchFlaggedTasks()
    }
    // endregion

    // region UI Setup
    private fun setupUI() {
        setupBackButton()
        setupRecyclerView()
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
        binding.flaggedRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            flaggedAdapter = createTaskAdapter()
            adapter = flaggedAdapter
        }
    }

    /**
     * Configures pull-to-refresh functionality using Ultra Pull-To-Refresh library.
     */
    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            // Re-fetch flagged tasks
            viewModel.fetchFlaggedTasks()
        }
    }
    // endregion

    // region Observers
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
     * Controls visibility of the RecyclerView based on data being empty or not.
     */
    private fun handleEmptyState(isEmpty: Boolean) {
        binding.flaggedRecycler.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.noRemindersTV.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
    // endregion

    // region Adapter & Callbacks
    /**
     * Creates and returns a TaskAdapter with the required listeners.
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
     * Handles toggling the completion status of a task.
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