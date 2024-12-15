package com.shayan.remindersios.ui.fragments


import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shayan.remindersios.R
import com.shayan.remindersios.adapters.TaskAdapter
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentScheduledBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

class ScheduledFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    private var _binding: FragmentScheduledBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel

    private val recyclerViews = mutableListOf<RecyclerView>()
    private val adapters = mutableListOf<TaskAdapter>()

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
        initRecyclerViewsAndAdapters()

        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

        viewModel.fetchScheduledTasks()

        viewModel.tasksByMonth.observe(viewLifecycleOwner) { tasksByMonth ->
            updateRecyclerViews(tasksByMonth)
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
            val textView = binding.root.findViewById<TextView>(textViewId)
            textView?.text = text
        }
    }

    private fun initRecyclerViewsAndAdapters() {
        recyclerViews.addAll(
            listOf(
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
        )

        recyclerViews.forEachIndexed { _, recyclerView ->
            val adapter = createTaskAdapter()
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                this.adapter = adapter
            }
            adapters.add(adapter)
        }
    }

    private fun createTaskAdapter(): TaskAdapter {
        return TaskAdapter(completionListener = this,
            itemClickListener = object : TaskAdapter.OnItemClickListener {
                override fun onItemClick(task: Tasks) {
                    // Navigate to TaskDetailsFragment
                    val bundle = Bundle().apply {
                        putParcelable("task", task) // Pass the task object
                    }
                    findNavController().navigate(R.id.taskDetailsFragment, bundle)
                }
            },
            deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                override fun onDeleteClick(task: Tasks) {
                    // Handle task deletion
                    viewModel.deleteTask(task.roomTaskId)
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            })
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

        monthsWithYears.forEachIndexed { index, monthYear ->
            val tasksForMonth = tasksByMonth[monthYear] ?: emptyList()
            if (index < adapters.size) {
                adapters[index].submitList(tasksForMonth)
                recyclerViews[index].visibility =
                    if (tasksForMonth.isEmpty()) View.GONE else View.VISIBLE
            }
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