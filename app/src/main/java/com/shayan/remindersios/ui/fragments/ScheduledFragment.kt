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

class ScheduledFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    private var _binding: FragmentScheduledBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel

    private val adapters = List(12) {
        TaskAdapter(this, this, object : TaskAdapter.OnDeleteClickListener {
            override fun onDeleteClick(task: Tasks) {
                viewModel.deleteTask(task.roomTaskId)
                Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduledBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupMonthHeaders()
        initRecyclerViews()
        setupPullToRefresh()


        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        viewModel.tasksByMonth.observe(viewLifecycleOwner) { tasksByMonth ->
            Log.d("ScheduledFragment", "Observed tasksByMonth: $tasksByMonth")
            updateRecyclerViews(tasksByMonth)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refreshTasks")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh == true) {
                    viewModel.fetchScheduledTasks()
                }
            }

        if (viewModel.tasksByMonth.value.isNullOrEmpty()) {
            viewModel.fetchScheduledTasks()
        }
    }

    private fun setupPullToRefresh() {
        val ptrFrameLayout = binding.root.findViewById<PtrClassicFrameLayout>(R.id.ultra_ptr)
        PullToRefreshUtil.setupUltraPullToRefresh(ptrFrameLayout) {
            // Fetch data here
            viewModel.fetchScheduledTasks()
        }
    }

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

            val textViewId =
                resources.getIdentifier("tv${i + 1}", "id", requireContext().packageName)
            binding.root.findViewById<TextView>(textViewId)?.text = text
        }
    }

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
                layoutManager = LinearLayoutManager(context)
                adapter = adapters[index]
                visibility = View.GONE // Hide initially
            }
        }
    }

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

        val monthsWithYears = (0 until 12).map {
            val monthIndex = (calendar.get(Calendar.MONTH) + it) % 12
            val year = calendar.get(Calendar.YEAR) + (calendar.get(Calendar.MONTH) + it) / 12
            "${months[monthIndex]} $year"
        }

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
            val tasksForMonth = tasksByMonth[monthYear] ?: emptyList()
            Log.d(
                "ScheduledFragment",
                "Updating RecyclerView for $monthYear with ${tasksForMonth.size} tasks"
            )
            adapters[index].submitList(tasksForMonth.toMutableList())

            // Show or hide RecyclerView based on task list
            recyclerViews[index].visibility =
                if (tasksForMonth.isEmpty()) View.GONE else View.VISIBLE
            Log.d(
                "ScheduledFragment",
                "RecyclerView for $monthYear visibility: ${recyclerViews[index].visibility}"
            )
        }
    }

    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onItemClick(task: Tasks) {
        val bundle = Bundle().apply {
            putParcelable("task", task)
        }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
