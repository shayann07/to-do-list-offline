package com.shayan.remindersios.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.shayan.remindersios.R
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentTaskDetailsBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel

/**
 * A Fragment to display detailed information about a single [Tasks] object.
 */
class TaskDetailsFragment : Fragment() {

    // region View Binding
    private var _binding: FragmentTaskDetailsBinding? = null
    private val binding get() = _binding!!
    // endregion

    // region ViewModel
    private lateinit var viewModel: ViewModel
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView(). Initializes ViewModel, sets up UI, and loads data.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        setupBackButton()
        displayTaskDetails()
    }

    /**
     * Release resources when the Fragment’s view is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region ViewModel Setup
    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
    }
    // endregion

    // region UI Setup
    /**
     * Sets up the back button to trigger a refresh on the previous screen and then navigate up.
     */
    private fun setupBackButton() {
        binding.backToHomeBtn.setOnClickListener {
            // If you want to trigger a refresh in the previous fragment
            findNavController().previousBackStackEntry?.savedStateHandle?.set("refreshTasks", true)

            findNavController().navigateUp()
        }
    }

    /**
     * Retrieves the [Tasks] object from arguments, if available, and displays its details.
     */
    private fun displayTaskDetails() {
        val task = arguments?.getParcelable<Tasks>("task")
        if (task != null) {
            bindTaskDetails(task)
        } else {
            showTaskNotAvailableMessage()
        }
    }

    /**
     * Binds a [Tasks] object's data to the UI fields in [FragmentTaskDetailsBinding].
     */
    private fun bindTaskDetails(task: Tasks) = with(binding) {
        tvTitle.text = task.title.naIfBlank()
        tvNotes.text = task.notes.naIfNull()
        tvDate.text = task.date.naIfNull()
        tvTime.text = task.time.naIfNull()
        tvFlag.text = if (task.flag) getString(R.string.yes) else getString(R.string.no)
        tvCompleted.text = if (task.isCompleted) getString(R.string.yes) else getString(R.string.no)
    }

    /**
     * Shows 'Not Available' for title and clears other fields if the task is missing.
     */
    private fun showTaskNotAvailableMessage() = with(binding) {
        tvTitle.text = getString(R.string.not_available)
        tvNotes.text = ""
        tvDate.text = ""
        tvTime.text = ""
        tvFlag.text = ""
        tvCompleted.text = ""
    }
    // endregion
}

// region Optional Extensions
/**
 * Returns the string or "Not available" if the value is null.
 */
private fun String?.naIfNull(): String {
    return this ?: "N/A"
}

/**
 * Returns the string or "Not available" if the value is blank (or null).
 */
private fun String?.naIfBlank(): String {
    return if (this.isNullOrBlank()) "N/A" else this
}
// endregion