package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.shayan.remindersios.R
import com.shayan.remindersios.adapters.TaskAdapter
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentHomeBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import com.shayan.remindersios.utils.shortToast // <-- Our Toast extension from ToastExtensions.kt
import java.text.NumberFormat
import java.util.Locale
import kotlin.reflect.KMutableProperty0

/**
 * The Home fragment that displays summaries of tasks and handles various navigation
 * to other task-related screens (Today, Scheduled, All, Flagged, Completed, etc.).
 */
class HomeFragment : Fragment() {

    // region View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel & Adapters
    private lateinit var viewModel: ViewModel
    private lateinit var taskAdapter: TaskAdapter
    // endregion

    // region Arrow States
    private var isArrowDownICloud = true
    private var isArrowDownOutlook = true
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called after onCreateView. Initializes core components, sets up UI, and handles back press.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupUI()
        handleBackPress()

        // Fetch total tasks once UI is ready
        viewModel.fetchTotalTasks()
    }

    /**
     * Refresh certain task counts every time the fragment resumes.
     */
    override fun onResume() {
        super.onResume()
        binding.recyclerView.visibility = View.GONE
        refreshTaskCounts()
    }


    /**
     * Clears resources upon destroying the view.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region ViewModel
    /**
     * Initializes the ViewModel for this fragment.
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }
    // endregion

    // region UI Setup
    /**
     * Sets up all UI components and observers.
     */
    private fun setupUI() {
        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupClickListeners()
    }

    /**
     * Sets up the RecyclerView with a linear layout and a custom TaskAdapter.
     */
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            completionListener = createTaskCompletionListener(),
            deleteClickListener = createDeleteClickListener(),
            itemClickListener = createItemClickListener()
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
            visibility = View.GONE
        }
    }

    /**
     * Configures the search view to fetch tasks by title.
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    clearSearchResults()
                } else {
                    viewModel.fetchTasksByTitle(newText)
                    showSearchResults()
                }
                return true
            }
        })

        // When user clicks the container, show results right away
        binding.searchViewContainer.setOnClickListener {
            showSearchResults()
            binding.searchView.requestFocus()
        }
    }

    /**
     * Clears the search results UI and restores the home UI elements.
     */
    private fun clearSearchResults() {
        taskAdapter.submitList(emptyList())
        with(binding) {
            recyclerView.visibility = View.GONE
            buttonContainer.visibility = View.VISIBLE
            gridLayout.visibility = View.VISIBLE
            homeComponent.visibility = View.VISIBLE
            microphoneIcon.visibility = View.VISIBLE
        }
    }

    /**
     * Shows the search results RecyclerView and hides the other home UI elements.
     */
    private fun showSearchResults() {
        with(binding) {
            recyclerView.visibility = View.VISIBLE
            buttonContainer.visibility = View.GONE
            gridLayout.visibility = View.GONE
            homeComponent.visibility = View.GONE
            microphoneIcon.visibility = View.GONE
        }
    }
    // endregion

    // region Observers
    /**
     * Sets up LiveData observers to react to data changes from the ViewModel.
     */
    private fun setupObservers() {
        viewModel.searchQueryResult.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNotEmpty()) {
                binding.recyclerView.visibility = View.VISIBLE
                taskAdapter.submitList(tasks)
            } else {
                binding.recyclerView.visibility = View.GONE
                context?.shortToast("No tasks found")
            }
        }

        observeTaskCounts()
    }

    /**
     * Observes the various LiveData counts (today, flagged, incomplete, etc.)
     * and updates UI elements accordingly.
     */
    private fun observeTaskCounts() {
        with(viewModel) {
            todayTaskCount.observe(viewLifecycleOwner) {
                updateTaskCount(binding.todayCount, it)
            }
            scheduledTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(binding.scheduledCount, it)
            }
            flaggedTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(binding.flaggedCount, it)
            }
            incompleteTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(binding.allCount, it)
            }
            totalTaskCount.observe(viewLifecycleOwner) {
                updateTaskCount(binding.iCloudCount, it)
            }
        }
    }

    /**
     * Updates a [TextView] with a properly formatted [count].
     */
    private fun updateTaskCount(textView: TextView, count: Int) {
        textView.text = NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
    }
    // endregion

    // region Click Listeners
    /**
     * Configures various click listeners (navigations, toggles, menus, etc.).
     */
    private fun setupClickListeners() = with(binding) {
        todayScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_todayFragment) }
        scheduledScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_scheduledFragment) }
        allScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_allFragment) }
        flaggedScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_flaggedFragment) }
        completedScreen.setOnClickListener { navigateTo(R.id.homeFragment_to_completedFragment) }
        iCloudContainer.setOnClickListener { navigateTo(R.id.homeFragment_to_iCloudFragment) }
        outlookContainer.setOnClickListener { navigateTo(R.id.homeFragment_to_outlookFragment) }

        menuImageView.setOnClickListener { showPopupMenu() }

        textviewICloud.setOnClickListener {
            toggleVisibility(iCloudContainer, ::isArrowDownICloud)
        }
        textviewOutlook.setOnClickListener {
            toggleVisibility(outlookContainer, ::isArrowDownOutlook)
        }

        newReminderButton.setOnClickListener {
            navigateTo(R.id.homeFragment_to_newReminderFragment)
        }
    }

    /**
     * Helper method to navigate using the NavController action ID.
     */
    private fun navigateTo(actionId: Int) {
        findNavController().navigate(actionId)
    }

    /**
     * Displays a popup menu for clearing all tasks (and other potential options).
     */
    private fun showPopupMenu() {
        PopupMenu(requireContext(), binding.menuImageView).apply {
            menuInflater.inflate(R.menu.menu_dropdown_toolbar, menu)
            setOnMenuItemClickListener { handleMenuItemClick(it) }
            show()
        }
    }

    /**
     * Responds to popup menu item selections.
     */
    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_all_tasks -> {
                viewModel.clearAllTasks()
                Snackbar.make(
                    binding.root, "Successfully Nuked All Reminders", Snackbar.LENGTH_SHORT
                ).show()
                true
            }

            else -> false
        }
    }

    /**
     * Toggles visibility of a container and flips the arrow state boolean.
     */
    private fun toggleVisibility(container: LinearLayout, arrowState: KMutableProperty0<Boolean>) {
        container.visibility = if (arrowState.get()) View.GONE else View.VISIBLE
        arrowState.set(!arrowState.get())
    }
    // endregion

    // region Back Press
    /**
     * Overrides the back press to finish the activity (i.e., close the app) if the user is on the home screen.
     */
    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }
    // endregion

    // region TaskAdapter Callbacks
    /**
     * Refreshes various task counts to ensure up-to-date UI whenever onResume is called.
     */
    private fun refreshTaskCounts() {
        with(viewModel) {
            fetchTodayTasks()
            fetchScheduledTasks()
            fetchIncompleteTasks()
            fetchCompletedTasks()
            fetchFlaggedTasks()
        }
    }

    /**
     * Creates the TaskCompletionListener for toggling task completion.
     */
    private fun createTaskCompletionListener() = object : TaskAdapter.TaskCompletionListener {
        override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
            viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
                val toastMessage = if (success) "Task updated" else "Failed to update: $message"
                context?.shortToast(toastMessage)
            }
        }
    }

    /**
     * Creates the OnDeleteClickListener for removing a task.
     */
    private fun createDeleteClickListener() = object : TaskAdapter.OnDeleteClickListener {
        override fun onDeleteClick(task: Tasks) {
            viewModel.deleteTask(task.roomTaskId)
            context?.shortToast("Task deleted")
        }
    }

    /**
     * Creates the OnItemClickListener for navigating to task details.
     */
    private fun createItemClickListener() = object : TaskAdapter.OnItemClickListener {
        override fun onItemClick(task: Tasks) {
            val bundle = Bundle().apply { putParcelable("task", task) }
            findNavController().navigate(R.id.taskDetailsFragment, bundle)
        }
    }
    // endregion
}