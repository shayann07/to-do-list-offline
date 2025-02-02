package com.shayan.remindersios.ui.fragments

import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shayan.remindersios.R
import com.shayan.remindersios.adapters.TaskAdapter
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentScheduledBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import com.shayan.remindersios.utils.PullToRefreshUtil
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout

/**
 * Fragment to display "scheduled" tasks, grouped by the next 12 months.
 */
class ScheduledFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    // region View Binding
    private var _binding: FragmentScheduledBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel
    private lateinit var viewModel: ViewModel
    // endregion

    // region Adapters & Data
    /**
     * A list of 12 [TaskAdapter] objects, each corresponding to one of the next 12 months.
     */
    private val adapters by lazy {
        List(12) {
            TaskAdapter(completionListener = this,
                itemClickListener = this,
                deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                    override fun onDeleteClick(task: Tasks) {
                        viewModel.deleteTask(task.roomTaskId)
                        Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                        // Or context?.shortToast("Task deleted")
                    }
                })
        }
    }
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduledBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView(). Sets up UI, ViewModel, observers, and fetches data if needed.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()
        setupUI()
        observeBackStackRefresh()
        observeTasksByMonth()

        // Fetch data if tasks are not yet loaded
        if (viewModel.tasksByMonth.value.isNullOrEmpty()) {
            viewModel.fetchScheduledTasks()
        }
    }

    /**
     * Clean up when the view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region ViewModel & Observers
    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }

    /**
     * Observes the [ViewModel.tasksByMonth] LiveData and updates RecyclerViews accordingly.
     */
    private fun observeTasksByMonth() {
        viewModel.tasksByMonth.observe(viewLifecycleOwner) { tasksByMonth ->
            Log.d("ScheduledFragment", "Observed tasksByMonth: $tasksByMonth")
            updateRecyclerViews(tasksByMonth)
        }
    }

    /**
     * If coming from the details screen (or another fragment) triggers a refresh, re-fetch data.
     */
    private fun observeBackStackRefresh() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refreshTasks")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh == true) {
                    viewModel.fetchScheduledTasks()
                }
            }
    }
    // endregion

    // region UI Setup
    private fun setupUI() {
        setupBackButton()
        setupMonthHeaders()
        initRecyclerViews()
        setupPullToRefresh()
    }

    /**
     * Configures the back button to either pop the back stack or close the activity.
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            // Using NavController for consistency:
            findNavController().navigateUp()

            // Or if you want the old approach:
            // requireActivity().onBackPressed()
        }
    }

    /**
     * Dynamically sets up the 12 month headers (e.g., "January 2023", "February 2023", etc.).
     */
    private fun setupMonthHeaders() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val months = listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )

        for (i in 0 until 12) {
            val monthIndex = (currentMonth + i) % 12
            val year = currentYear + (currentMonth + i) / 12
            val text = "${months[monthIndex]} $year"

            // Find the corresponding TextView (tv1, tv2, ..., tv12)
            val textViewId =
                resources.getIdentifier("tv${i + 1}", "id", requireContext().packageName)
            binding.root.findViewById<TextView>(textViewId)?.text = text
        }
    }

    /**
     * Initializes the 12 RecyclerViews for scheduled tasks, each mapped to a [TaskAdapter] from [adapters].
     */
    private fun initRecyclerViews() {
        val recyclerViews = listOf(
            binding.rv1,
            binding.rv2,
            binding.rv3,
            binding.rv4,
            binding.rv5,
            binding.rv6,
            binding.rv7,
            binding.rv8,
            binding.rv9,
            binding.rv10,
            binding.rv11,
            binding.rv12
        )

        recyclerViews.forEachIndexed { index, recyclerView ->
            Log.d("ScheduledFragment", "Initializing RecyclerView for index: $index")
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = adapters[index]
                visibility = View.GONE // Hidden until data is assigned
            }
        }
    }

    /**
     * Adds pull-to-refresh functionality, calling [ViewModel.fetchScheduledTasks] on refresh.
     */
    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            viewModel.fetchScheduledTasks()
        }
    }
    // endregion

    // region RecyclerView Update
    /**
     * Updates each of the 12 RecyclerViews with tasks that match the month-year keys in [tasksByMonth].
     */
    private fun updateRecyclerViews(tasksByMonth: Map<String, List<Tasks>>) {
        val calendar = Calendar.getInstance()
        val months = listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )

        // Build a list of month-year strings for the next 12 months
        val monthsWithYears = (0 until 12).map {
            val monthIndex = (calendar.get(Calendar.MONTH) + it) % 12
            val year = calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + it) / 12
            "${months[monthIndex]} $year"
        }

        // Matching the same indexing for adapters and recycler views
        val recyclerViews = listOf(
            binding.rv1,
            binding.rv2,
            binding.rv3,
            binding.rv4,
            binding.rv5,
            binding.rv6,
            binding.rv7,
            binding.rv8,
            binding.rv9,
            binding.rv10,
            binding.rv11,
            binding.rv12
        )

        monthsWithYears.forEachIndexed { index, monthYear ->
            val tasksForMonth = tasksByMonth[monthYear].orEmpty()
            Log.d("ScheduledFragment", "Updating $monthYear with ${tasksForMonth.size} tasks")

            // Update the adapter’s data
            adapters[index].submitList(tasksForMonth.toMutableList())

            // Show/hide the RecyclerView based on whether tasks are present
            recyclerViews[index].visibility =
                if (tasksForMonth.isEmpty()) View.GONE else View.VISIBLE
        }
    }
    // endregion

    // region TaskAdapter Callbacks
    /**
     * Called when a task's completion status is toggled from the [TaskAdapter].
     */
    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
            // Or context?.shortToast("Task updated") if you have a toast extension
        }
    }

    /**
     * Called when a task item is clicked, navigating to the Task Details screen.
     */
    override fun onItemClick(task: Tasks) {
        val bundle = Bundle().apply {
            putParcelable("task", task)
        }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }
    // endregion
}