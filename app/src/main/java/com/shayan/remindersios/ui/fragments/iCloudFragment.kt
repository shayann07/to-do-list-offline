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
import com.shayan.remindersios.databinding.FragmentICloudBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import com.shayan.remindersios.utils.PullToRefreshUtil
import com.shayan.remindersios.utils.shortToast  // <-- Our Toast extension
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment to display and manage iCloud tasks.
 */
class iCloudFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    // region View Binding
    private var _binding: FragmentICloudBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel & Adapter
    private lateinit var viewModel: ViewModel
    private lateinit var iCloudAdapter: TaskAdapter
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentICloudBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Initializes the ViewModel, sets up UI elements, and observes the tasks data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
        setupUI()
        observeTotalTasks()
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
        viewModel.fetchTotalTasks() // Initial fetch
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
     * Sets up the RecyclerView with a linear layout and attaches the iCloudAdapter.
     */
    private fun setupRecyclerView() {
        iCloudAdapter = createTaskAdapter()
        binding.icloudRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = iCloudAdapter
        }
    }

    /**
     * Configures pull-to-refresh functionality using Ultra Pull-To-Refresh library.
     */
    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            // Re-fetch total tasks on refresh
            viewModel.fetchTotalTasks()
        }
    }
    // endregion

    // region Observers
    /**
     * Observes total tasks from the ViewModel and updates the adapter list.
     */
    private fun observeTotalTasks() {
        viewModel.totalTasks.observe(viewLifecycleOwner) { tasks ->
            iCloudAdapter.submitList(tasks)
            binding.icloudRecycler.visibility =
                if (tasks.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.noRemindersTV.visibility =
                if (tasks.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
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
     * Called when a task's completion status is toggled.
     */
    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            val toastMessage = if (success) "Task updated" else "Failed to update: $message"
            context?.shortToast(toastMessage)
        }
    }

    /**
     * Called when a task item is clicked, navigating to the Task Details screen.
     */
    override fun onItemClick(task: Tasks) {
        val bundle = Bundle().apply { putParcelable("task", task) }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }
    // endregion
}