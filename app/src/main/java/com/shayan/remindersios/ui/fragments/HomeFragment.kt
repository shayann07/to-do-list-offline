package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
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
import java.text.NumberFormat
import java.util.Locale
import kotlin.reflect.KMutableProperty0

class HomeFragment : Fragment() {

    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private lateinit var viewModel: ViewModel

    // Adapter
    private lateinit var taskAdapter: TaskAdapter

    // Arrow state for container toggles
    private var isArrowDownICloud = true
    private var isArrowDownOutlook = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        handleBackPress()

        viewModel.fetchTotalTasks()
    }

    private fun initializeComponents() {
        initializeViewModel()
        setupRecyclerView()
        setupSearchView()
        setupObservers()
        setupClickListeners()
    }

    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

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

        binding.searchViewContainer.setOnClickListener {
            showSearchResults()
            binding.searchView.requestFocus()
        }
    }

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

    private fun showSearchResults() {
        with(binding) {
            recyclerView.visibility = View.VISIBLE
            buttonContainer.visibility = View.GONE
            gridLayout.visibility = View.GONE
            homeComponent.visibility = View.GONE
            microphoneIcon.visibility = View.GONE
        }
    }

    private fun setupObservers() {
        viewModel.searchQueryResult.observe(viewLifecycleOwner) { tasks ->
            if (tasks.isNotEmpty()) {
                binding.recyclerView.visibility = View.VISIBLE
                taskAdapter.submitList(tasks)
            } else {
                binding.recyclerView.visibility = View.GONE
                Toast.makeText(requireContext(), "No tasks found", Toast.LENGTH_SHORT).show()
            }
        }

        observeTaskCounts()
    }

    private fun observeTaskCounts() {
        with(viewModel) {
            todayTaskCount.observe(viewLifecycleOwner) { updateTaskCount(binding.todayCount, it) }
            scheduledTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(
                    binding.scheduledCount, it
                )
            }
            flaggedTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(
                    binding.flaggedCount, it
                )
            }
            incompleteTasksCount.observe(viewLifecycleOwner) {
                updateTaskCount(
                    binding.allCount, it
                )
            }
            totalTaskCount.observe(viewLifecycleOwner) { updateTaskCount(binding.iCloudCount, it) }
        }
    }

    private fun updateTaskCount(textView: TextView, count: Int) {
        textView.text = NumberFormat.getNumberInstance(Locale.getDefault()).format(count)
    }

    private fun setupClickListeners() {
        with(binding) {
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

            newReminderButton.setOnClickListener { navigateTo(R.id.homeFragment_to_newReminderFragment) }
        }
    }

    private fun navigateTo(actionId: Int) {
        findNavController().navigate(actionId)
    }

    private fun showPopupMenu() {
        PopupMenu(requireContext(), binding.menuImageView).apply {
            menuInflater.inflate(R.menu.menu_dropdown_toolbar, menu)
            setOnMenuItemClickListener { handleMenuItemClick(it) }
            show()
        }
    }

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

    private fun toggleVisibility(container: LinearLayout, arrowState: KMutableProperty0<Boolean>) {
        container.visibility = if (arrowState.get()) View.GONE else View.VISIBLE
        arrowState.set(!arrowState.get())
    }

    private fun createTaskCompletionListener() = object : TaskAdapter.TaskCompletionListener {
        override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
            viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
                Toast.makeText(
                    requireContext(),
                    if (success) "Task updated" else "Failed to update: $message",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createDeleteClickListener() = object : TaskAdapter.OnDeleteClickListener {
        override fun onDeleteClick(task: Tasks) {
            viewModel.deleteTask(task.roomTaskId)
            Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createItemClickListener() = object : TaskAdapter.OnItemClickListener {
        override fun onItemClick(task: Tasks) {
            val bundle = Bundle().apply { putParcelable("task", task) }
            findNavController().navigate(R.id.taskDetailsFragment, bundle)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshTaskCounts()
    }

    private fun refreshTaskCounts() {
        with(viewModel) {
            fetchTodayTasks()
            fetchScheduledTasks()
            fetchIncompleteTasks()
            fetchCompletedTasks()
            fetchFlaggedTasks()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}