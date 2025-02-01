package com.shayan.remindersios.ui.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.shayan.remindersios.R
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.FragmentNewReminderBinding
import com.shayan.remindersios.ui.viewmodel.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class NewReminderFragment : BottomSheetDialogFragment() {

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

        val bottomSheetDialog = dialog as? BottomSheetDialog
        val behavior = bottomSheetDialog?.behavior

        // Calculate 90% of screen height
        val displayMetrics = DisplayMetrics().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireContext().display?.getRealMetrics(this)
            } else {
                val windowManager =
                    requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.defaultDisplay.getMetrics(this)
            }
        }
        val screenHeight = displayMetrics.heightPixels
        val peekHeight = (screenHeight * 0.94f).toInt()

        // Set bottom sheet behavior properties
        behavior?.let {
            it.peekHeight = peekHeight
            it.state = BottomSheetBehavior.STATE_COLLAPSED
            it.skipCollapsed = false  // Keep this false to maintain collapsed state
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setupBlurView()
        setupListeners()
        setupFormValidation()
    }

    private fun setupBlurView() {
        val rootView =
            requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = requireActivity().window.decorView.background

        binding.blurView.setupWith(rootView).setFrameClearDrawable(windowBackground)
            .setBlurRadius(15f).setBlurAutoUpdate(true)
            .setOverlayColor(requireContext().getColor(R.color.transparent_black))
    }

    private fun setupListeners() {
        binding.cancelButton.setOnClickListener { dismissWithKeyboardHidden() }
        binding.addTaskButton.setOnClickListener { handleAddTask() }
        binding.dateSwitch.setOnCheckedChangeListener { _, isChecked -> toggleDateSwitch(isChecked) }
        binding.timeSwitch.setOnCheckedChangeListener { _, isChecked -> toggleTimeSwitch(isChecked) }
        binding.flagSwitch.setOnCheckedChangeListener { _, isChecked -> isFlagged = isChecked }
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            updateSelectedDate(
                year, month, dayOfMonth
            )
        }
    }

    private fun setupFormValidation() {
        binding.titleInput.doOnTextChanged { text, _, _, _ ->
            binding.addTaskButton.isEnabled = !text.isNullOrEmpty()
        }
    }

    private fun observeViewModel() {
        viewModel.taskCreationStatus.observe(viewLifecycleOwner) { isSuccess ->
            val message =
                if (isSuccess) "Task created successfully!" else "Failed to create task. Please try again."
            showSnackbar(message)
            if (isSuccess) {
                clearForm()
                dismiss()
            }
        }
    }

    private fun toggleDateSwitch(isChecked: Boolean) {
        hideKeyboard()
        if (isChecked) {
            selectedDate = getCurrentDate()
            binding.dateDisplay.text = selectedDate
            binding.calendarContainer.visibility = View.VISIBLE
        } else {
            selectedDate = null
            binding.dateDisplay.text = ""
            binding.calendarContainer.visibility = View.GONE
        }
    }

    private fun toggleTimeSwitch(isChecked: Boolean) {
        hideKeyboard()
        if (isChecked) showTimePicker() else clearTimeSelection()
    }

    private fun updateSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        binding.dateDisplay.text = selectedDate
    }

    private fun showTimePicker() {
        showBlurOverlay()
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(), { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                binding.timeDisplay.text = selectedTime
                showSnackbar("Selected time: $selectedTime (${determineTimeCategory(hour)})")
                hideBlurOverlay()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        ).apply { setOnDismissListener { hideBlurOverlay() } }.show()
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

        viewModel.saveTask(
            Tasks(
                title = title,
                notes = notes,
                date = selectedDate,
                time = selectedTime,
                timeCategory = timeCategory,
                flag = isFlagged,
                isCompleted = false
            )
        )
    }

    private fun clearForm() {
        binding.apply {
            titleInput.text.clear()
            notesInput.text.clear()
            dateDisplay.text = ""
            timeDisplay.text = ""
            flagSwitch.isChecked = false
            dateSwitch.isChecked = false
            timeSwitch.isChecked = false
        }
        selectedDate = null
        selectedTime = null
    }

    private fun dismissWithKeyboardHidden() {
        hideKeyboard()
        dismiss()
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
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun showBlurOverlay() {
        binding.blurView.apply {
            alpha = 1f
            visibility = View.VISIBLE
        }
    }

    private fun hideBlurOverlay() {
        binding.blurView.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}