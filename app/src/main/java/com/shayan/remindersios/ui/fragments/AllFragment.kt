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
import com.shayan.remindersios.databinding.FragmentAllBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import com.shayan.remindersios.utils.PullToRefreshUtil
import com.shayan.remindersios.utils.shortToast
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * A Fragment displaying all incomplete tasks with pull-to-refresh functionality.
 */
class AllFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    // region View Binding
    private var _binding: FragmentAllBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel and Adapter
    private lateinit var viewModel: ViewModel
    private lateinit var allAdapter: TaskAdapter
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView(). Here we initialize the ViewModel, setup the UI,
     * and start observing LiveData.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Initialize ViewModel
        initViewModel()

        // 2) Setup UI (RecyclerView, Back Button, Pull-to-Refresh)
        setupUI()

        // 3) Observe the incomplete tasks LiveData
        observeIncompleteTasks()
    }

    /**
     * Release resources when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region ViewModel Setup
    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchIncompleteTasks()
    }
    // endregion

    // region UI Setup
    private fun setupUI() {
        setupBackButton()
        setupRecyclerView()
        setupPullToRefresh()
    }

    /**
     * Back button handler. Uses NavController's navigateUp for better back-stack handling.
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * Configures the RecyclerView and initializes the TaskAdapter.
     */
    private fun setupRecyclerView() {
        binding.allRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            allAdapter = createTaskAdapter()
            adapter = allAdapter
        }
    }

    /**
     * Setup Pull-to-Refresh using Ultra Pull-To-Refresh library.
     */
    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            // Reload the incomplete tasks
            viewModel.fetchIncompleteTasks()
        }
    }
    // endregion

    // region Observers
    /**
     * Observes changes in the incomplete tasks LiveData and updates the UI.
     */
    private fun observeIncompleteTasks() {
        viewModel.incompleteTasks.observe(viewLifecycleOwner) { incompleteTasks ->
            allAdapter.submitList(incompleteTasks)
            binding.allRecyclerView.visibility =
                if (incompleteTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.noRemindersTV.visibility =
                if (incompleteTasks.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }
    // endregion

    // region TaskAdapter Callbacks
    /**
     * Called when the user toggles a task's completion status.
     */
    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            if (success) {
                context?.shortToast("Task updated")
            } else {
                context?.shortToast("Failed to update: $message")
            }
        }
    }

    /**
     * Called when a task item is clicked to view details.
     */
    override fun onItemClick(task: Tasks) {
        val bundle = Bundle().apply { putParcelable("task", task) }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }
    // endregion

    // region Adapter Creation
    /**
     * Creates and returns a new instance of TaskAdapter.
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
    // endregion
}