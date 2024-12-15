package com.shayan.remindersios.ui.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.shayan.remindersios.R
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentNewReminderBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class NewReminderFragment : Fragment() {

    private var _binding: FragmentNewReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModel by viewModels()

    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var isFlagged: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        initializeObservers()
    }

    /**
     * Initialize UI components and set up event listeners.
     */
    private fun initializeComponents() {

        binding.cancelButton.setOnClickListener { requireActivity().onBackPressed() }
        binding.addTaskButton.setOnClickListener {
            handleAddTask()
        }
        setupDateSwitch()
        setupTimeSwitch()
        setupFlagSwitch()
    }

    /**
     * Observe LiveData from the ViewModel.
     */
    private fun initializeObservers() {
        viewModel.taskCreationStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                showSnackbar("Task created successfully")
                clearForm()
                navigateToHome()
            } else {
                showSnackbar("Failed to create task. Try again.")
            }
        }
    }

    private fun setupDateSwitch() {
        binding.dateSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Set the selectedDate to the current date
                val currentDate = getCurrentDate()
                selectedDate = currentDate

                // Update the UI
                binding.dateDisplay.text = currentDate
                showSnackbar("Date set to: $currentDate")

                // Show the calendar container
                binding.calendarContainer.visibility = View.VISIBLE
            } else {
                // Clear the selected date when the switch is toggled off
                selectedDate = null
                binding.dateDisplay.text = ""
                binding.calendarContainer.visibility = View.GONE
            }
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            binding.dateDisplay.text = selectedDate
            showSnackbar("Selected date: $selectedDate")
        }
    }

    private fun setupTimeSwitch() {
        binding.timeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) showTimePicker() else clearTimeSelection()
        }
    }

    private fun determineTimeCategory(hour: Int): String {
        return when (hour) {
            in 5..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..23, in 0..4 -> "tonight"
            else -> "unknown"
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                Log.d("TimePicker", "Selected Hour in 24-hour format: $hour")
                selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.timeDisplay.text = selectedTime

                val timeCategory = determineTimeCategory(selectedHour)
                showSnackbar("Selected time: $selectedTime($timeCategory)")
            }, hour, minute, true
        ).show()
    }

    private fun setupFlagSwitch() {
        binding.flagSwitch.setOnCheckedChangeListener { _, isChecked ->
            isFlagged = isChecked
        }
    }

    private fun handleAddTask() {
        val title = binding.titleInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()

        if (title.isEmpty()) {
            showSnackbar("Title is required.")
            return
        } else {
            navigateToHome()
        }

        // Automatically set time to current time if selectedDate is today's date and time is null
        if (selectedDate == getCurrentDate() && selectedTime == null) {
            val calendar = Calendar.getInstance()
            selectedTime = String.format(
                "%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)
            )
        }

        // Determine the time category based on the selected time
        val timeCategory = if (selectedTime != null) {
            determineTimeCategory(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        } else {
            "tonight" // Default time category if no time is selected
        }

        // Proceed to save the task
        val task = Tasks(
            title = title,
            notes = notes,
            date = selectedDate,
            time = selectedTime,
            timeCategory = timeCategory,
            flag = isFlagged,
            isCompleted = false
        )
        viewModel.saveTask(task)
    }


    // Function to get the current date in the same format as selectedDate
    private fun getCurrentDate(): String {
        return SimpleDateFormat(
            "yyyy-MM-dd", Locale.getDefault()
        ).format(Calendar.getInstance().time)
    }

    private fun clearForm() {
        binding.titleInput.text.clear()
        binding.notesInput.text.clear()
        binding.dateDisplay.text = ""
        binding.timeDisplay.text = ""
        binding.flagSwitch.isChecked = false
        selectedDate = null
        selectedTime = null
    }

    private fun clearTimeSelection() {
        binding.timeDisplay.text = ""
        selectedTime = null
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.newReminderFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
