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
import com.shayan.remindersios.utils.PullToRefreshUtil
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment for displaying and managing Today's tasks, grouped into morning, afternoon, and tonight.
 */
class TodayFragment : Fragment(), TaskAdapter.TaskCompletionListener {

    // region View Binding
    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel & Adapters
    private lateinit var viewModel: ViewModel
    private lateinit var morningAdapter: TaskAdapter
    private lateinit var afternoonAdapter: TaskAdapter
    private lateinit var tonightAdapter: TaskAdapter
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView(). Sets up ViewModel, UI elements, observers, and data fetching.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupUI()
        observeTodayTasks()

        // Initial fetch of today's tasks
        viewModel.fetchTodayTasks()
    }

    /**
     * Release resources when the fragment's view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region ViewModel
    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }
    // endregion

    // region UI Setup
    /**
     * Sets up all UI elements including back button, RecyclerViews, and pull-to-refresh.
     */
    private fun setupUI() {
        setupBackButton()
        setupRecyclerViews()
        setupPullToRefresh()
    }

    /**
     * Configures the back button to navigate up in the NavController stack
     * (or finish the activity if desired).
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            // If you'd prefer to pop the back stack using Navigation:
            findNavController().navigateUp()

            // Or if you want the old behavior:
            // requireActivity().onBackPressed()
        }
    }

    /**
     * Initializes and attaches adapters to the morning, afternoon, and tonight RecyclerViews.
     */
    private fun setupRecyclerViews() {
        // LinearLayoutManagers
        binding.recyclerMorning.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAfternoon.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTonight.layoutManager = LinearLayoutManager(requireContext())

        // Adapters
        morningAdapter = createTaskAdapter()
        afternoonAdapter = createTaskAdapter()
        tonightAdapter = createTaskAdapter()

        binding.recyclerMorning.adapter = morningAdapter
        binding.recyclerAfternoon.adapter = afternoonAdapter
        binding.recyclerTonight.adapter = tonightAdapter
    }

    /**
     * Configures the pull-to-refresh functionality.
     */
    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            viewModel.fetchTodayTasks()
        }
    }
    // endregion

    // region Observers
    /**
     * Observes live data for tasks in the morning, afternoon, and tonight categories.
     */
    private fun observeTodayTasks() {
        // Observe morning tasks
        viewModel.morningTasksLiveData.observe(viewLifecycleOwner) { morningTasks ->
            morningAdapter.submitList(morningTasks)
            binding.recyclerMorning.visibility =
                if (morningTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.noRemindersTV.visibility =
                if (morningTasks.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        // Observe afternoon tasks
        viewModel.afternoonTasksLiveData.observe(viewLifecycleOwner) { afternoonTasks ->
            afternoonAdapter.submitList(afternoonTasks)
            binding.recyclerAfternoon.visibility =
                if (afternoonTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.noRemindersTV.visibility =
                if (afternoonTasks.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        // Observe tonight tasks
        viewModel.tonightTasksLiveData.observe(viewLifecycleOwner) { tonightTasks ->
            tonightAdapter.submitList(tonightTasks)
            binding.recyclerTonight.visibility =
                if (tonightTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.noRemindersTV.visibility =
                if (tonightTasks.isNullOrEmpty()) View.VISIBLE else View.GONE
        }
    }
    // endregion

    // region TaskAdapter - Completion Listener & Factory
    /**
     * Called when a task's completion status is toggled.
     */
    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
            // If using shortToast extension: context?.shortToast("Task updated")
        }
    }

    /**
     * Creates a new [TaskAdapter] with custom delete and click listeners.
     */
    private fun createTaskAdapter(): TaskAdapter {
        return TaskAdapter(completionListener = this,
            itemClickListener = object : TaskAdapter.OnItemClickListener {
                override fun onItemClick(task: Tasks) {
                    // Navigate to TaskDetailsFragment
                    val bundle = Bundle().apply {
                        putParcelable("task", task)
                    }
                    findNavController().navigate(R.id.taskDetailsFragment, bundle)
                }
            },
            deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                override fun onDeleteClick(task: Tasks) {
                    viewModel.deleteTask(task.roomTaskId)
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            })
    }
    // endregion
}