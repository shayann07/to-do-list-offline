package com.shayan.remindersios.ui.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
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
        initializeBlurView()
        initializeComponents()
        initializeObservers()
    }

    private fun initializeBlurView() {
        // Get the root view of the activity
        val rootView =
            requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)

        // Optional: Use the window background to handle transparency
        val windowBackground = requireActivity().window.decorView.background

        // Configure the BlurView
        binding.blurView.setupWith(rootView) // Set the root view to blur
            .setFrameClearDrawable(windowBackground) // Optional: Ensure transparency is handled
            .setBlurRadius(15f) // Set the blur radius (adjust as needed)
            .setBlurAutoUpdate(true) // Automatically update blur when layout changes
            .setOverlayColor(requireContext().getColor(R.color.transparent_black)) // Optional: Add an overlay color
    }

    private fun initializeComponents() {
        setupButtonListeners()
        setupDateSwitch()
        setupTimeSwitch()
        setupFlagSwitch()
        setupFormValidation()
    }

    private fun initializeObservers() {
        viewModel.taskCreationStatus.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                showSnackbar("Task created successfully!")
                clearForm()
                navigateToHome()
            } else {
                showSnackbar("Failed to create task. Please try again.")
            }
        }
    }

    private fun setupButtonListeners() {
        binding.cancelButton.setOnClickListener {
            hideKeyboard()
            findNavController().popBackStack()
        }

        binding.addTaskButton.setOnClickListener {
            hideKeyboard()
            handleAddTask()
        }
    }

    private fun setupDateSwitch() {
        binding.dateSwitch.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboard()
            if (isChecked) {
                val currentDate = getCurrentDate()
                selectedDate = currentDate
                binding.dateDisplay.text = currentDate
                binding.calendarContainer.visibility = View.VISIBLE
            } else {
                selectedDate = null
                binding.dateDisplay.text = ""
                binding.calendarContainer.visibility = View.GONE
            }
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            binding.dateDisplay.text = selectedDate
        }
    }

    private fun setupTimeSwitch() {
        binding.timeSwitch.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboard()
            if (isChecked) showTimePicker() else clearTimeSelection()
        }
    }

    private fun setupFlagSwitch() {
        binding.flagSwitch.setOnCheckedChangeListener { _, isChecked ->
            hideKeyboard()
            isFlagged = isChecked
        }
    }

    private fun setupFormValidation() {
        binding.titleInput.doOnTextChanged { text, _, _, _ ->
            binding.addTaskButton.isEnabled = !text.isNullOrEmpty()
        }
    }

    private fun showBlurOverlay() {
        binding.blurView.apply {
            alpha = 1f // Ensure the blur effect is fully visible
            visibility = View.VISIBLE // Directly make it visible
        }
    }

    private fun hideBlurOverlay() {
        binding.blurView.apply {
            visibility = View.GONE // Directly hide the blur
        }
    }

    private fun showTimePicker() {
        showBlurOverlay() // Show the blur overlay
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.timeDisplay.text = selectedTime

                val timeCategory = determineTimeCategory(selectedHour)
                showSnackbar("Selected time: $selectedTime ($timeCategory)")
                hideBlurOverlay() // Hide the blur overlay when time is selected
            }, hour, minute, true
        ).apply {
            setOnDismissListener {
                hideBlurOverlay() // Hide the blur overlay when dialog is dismissed
            }
        }.show()
    }

    private fun handleAddTask() {
        val title = binding.titleInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()

        if (title.isEmpty()) {
            showSnackbar("Title is required.")
            return
        }

        if (selectedDate == getCurrentDate() && selectedTime == null) {
            selectedTime = getCurrentTime()
        }

        val timeCategory = selectedTime?.let {
            determineTimeCategory(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        } ?: "tonight"

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

    private fun clearForm() {
        binding.titleInput.text.clear()
        binding.notesInput.text.clear()
        binding.dateDisplay.text = ""
        binding.timeDisplay.text = ""
        binding.flagSwitch.isChecked = false
        binding.dateSwitch.isChecked = false
        binding.timeSwitch.isChecked = false
        selectedDate = null
        selectedTime = null
    }

    private fun clearTimeSelection() {
        binding.timeDisplay.text = ""
        selectedTime = null
    }

    private fun getCurrentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

    private fun getCurrentTime(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)

    private fun determineTimeCategory(hour: Int): String = when (hour) {
        in 5..11 -> "morning"
        in 12..17 -> "afternoon"
        else -> "tonight"
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.newReminderFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}