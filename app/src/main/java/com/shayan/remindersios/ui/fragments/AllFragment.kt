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
import com.shayan.remindersios.databinding.FragmentAllBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

class AllFragment : Fragment(), TaskAdapter.TaskCompletionListener,
    TaskAdapter.OnItemClickListener {

    // View binding for this fragment
    private var _binding: FragmentAllBinding? = null
    private val binding get() = _binding!!

    // ViewModel and RecyclerView Adapter
    private lateinit var viewModel: ViewModel
    private lateinit var allAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup the "Back to Home" button
        setupBackButton()

        // Initialize RecyclerView and Adapter
        setupRecyclerView()

        // Initialize ViewModel and set observers
        initializeViewModel()
        observeIncompleteTasks()
    }

    /**
     * Sets up the back button to navigate to the previous screen.
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    /**
     * Configures the RecyclerView and initializes the TaskAdapter.
     */
    private fun setupRecyclerView() {
        binding.allRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        allAdapter = createTaskAdapter()
        binding.allRecyclerView.adapter = allAdapter
    }

    /**
     * Initializes the ViewModel and fetches the required data.
     */
    private fun initializeViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        viewModel.fetchIncompleteTasks()
    }

    /**
     * Observes changes in incomplete tasks and updates the UI accordingly.
     */
    private fun observeIncompleteTasks() {
        viewModel.incompleteTasks.observe(viewLifecycleOwner) { incompleteTasks ->
            allAdapter.submitList(incompleteTasks)
            binding.allRecyclerView.visibility =
                if (incompleteTasks.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    /**
     * Handles toggling task completion status and updates the database.
     */
    override fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean) {
        viewModel.toggleTaskCompletion(roomTaskId, isCompleted) { success, message ->
            Toast.makeText(
                requireContext(),
                if (success) "Task updated" else "Failed to update: $message",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Creates and returns a new instance of TaskAdapter.
     */
    private fun createTaskAdapter(): TaskAdapter {
        return TaskAdapter(completionListener = this,
            itemClickListener = this,
            deleteClickListener = object : TaskAdapter.OnDeleteClickListener {
                override fun onDeleteClick(task: Tasks) {
                    viewModel.deleteTask(task.roomTaskId)
                    Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /**
     * Handles navigation to the Task Details screen.
     */
    override fun onItemClick(task: Tasks) {
        val bundle = Bundle().apply {
            putParcelable("task", task)
        }
        findNavController().navigate(R.id.taskDetailsFragment, bundle)
    }

    /**
     * Releases resources when the fragment's view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
