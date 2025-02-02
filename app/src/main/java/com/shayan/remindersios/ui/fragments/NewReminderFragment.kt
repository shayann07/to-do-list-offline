package com.shayan.remindersios.ui.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
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
import eightbitlab.com.blurview.BlurView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class NewReminderFragment : BottomSheetDialogFragment() {

    // region Companion Object
    companion object {
        private const val BLUR_RADIUS = 15f
        private const val BLUR_ANIMATION_DURATION = 350L
        private const val FLING_ANIMATION_DURATION = 250L

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
    // endregion

    // region View Binding & ViewModel
    private var _binding: FragmentNewReminderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ViewModel by viewModels()
    // endregion

    // region Blur & Animation
    private var backgroundBlurContainer: FrameLayout? = null
    private var isDragging = false
    private var lastSlideOffset = 0f
    private val downwardInterpolator = AccelerateInterpolator()    // Fade out on downward drag
    // endregion

    // region State Variables
    private var selectedDate: String? = null
    private var selectedTime: String? = null
    private var isFlagged: Boolean = false
    // endregion

    // region Lifecycle Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheetDialog = dialog as? BottomSheetDialog
        val behavior = bottomSheetDialog?.behavior

        // Calculate 80% of screen height (for peek height)
        val screenHeight = requireContext().getScreenHeight()
        val peekHeight = (screenHeight * 0.8f).toInt()

        // Disable dimming
        bottomSheetDialog?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0f)
        }

        // Setup bottom sheet behavior
        behavior?.apply {
            this.peekHeight = peekHeight
            skipCollapsed = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // Initialize UI elements and observers
        setupBlurView()
        setupUI()
        observeViewModel()
        setupBackgroundBlurView() // Programmatic blur background
        setupBottomSheetCallbacks(behavior)
    }

    override fun onDismiss(dialog: DialogInterface) {
        // Only fade out if we're not in the middle of a drag
        if (!isDragging) {
            backgroundBlurContainer?.animate()?.alpha(0f)?.setDuration(FLING_ANIMATION_DURATION)
                ?.setInterpolator(AccelerateInterpolator())?.withEndAction {
                    removeBlurView()
                }?.start()
        } else {
            // If it's currently dragging and gets dismissed, just remove immediately
            removeBlurView()
        }
        super.onDismiss(dialog)
    }

    override fun onDestroyView() {
        removeBlurView()
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region UI Setup
    /**
     * Sets up UI listeners and any additional configuration needed for the fragment.
     */
    private fun setupUI() {
        // Cancel Button
        binding.cancelButton.setOnClickListener {
            it.hideKeyboard()
            dismiss()
        }

        // Add Task Button
        binding.addTaskButton.setOnClickListener {
            handleAddTask()
        }

        // Date Switch
        binding.dateSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleDateSwitch(isChecked)
        }

        // Time Switch
        binding.timeSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleTimeSwitch(isChecked)
        }

        // Flag Switch
        binding.flagSwitch.setOnCheckedChangeListener { _, isChecked ->
            isFlagged = isChecked
        }

        // Calendar
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            updateSelectedDate(year, month, dayOfMonth)
        }

        // Validate Title Input
        binding.titleInput.doOnTextChanged { text, _, _, _ ->
            binding.addTaskButton.isEnabled = !text.isNullOrBlank()
        }
    }

    /**
     * Initializes the BlurView within the layout (the smaller in-layout blur, not the background).
     */
    private fun setupBlurView() {
        val rootView =
            requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)
        val windowBackground = requireActivity().window.decorView.background

        binding.blurView.setupWith(rootView).setFrameClearDrawable(windowBackground)
            .setBlurRadius(BLUR_RADIUS).setBlurAutoUpdate(true)
            .setOverlayColor(requireContext().getColor(R.color.transparent_black))
    }
    // endregion

    // region Observers
    private fun observeViewModel() {
        viewModel.taskCreationStatus.observe(viewLifecycleOwner) { isSuccess ->
            val message = if (isSuccess) "Task created successfully!"
            else "Failed to create task. Please try again."

            binding.root.showSnackbar(message)
            if (isSuccess) {
                clearForm()
                dismiss()
            }
        }
    }
    // endregion

    // region Switch Toggling
    private fun toggleDateSwitch(isChecked: Boolean) {
        requireView().hideKeyboard()
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
        requireView().hideKeyboard()
        if (isChecked) showTimePicker()
        else clearTimeSelection()
    }
    // endregion

    // region Date & Time Handling
    private fun updateSelectedDate(year: Int, month: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        selectedDate = dateFormat.format(calendar.time)
        binding.dateDisplay.text = selectedDate
    }

    private fun showTimePicker() {
        showInLayoutBlurOverlay()
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(), { _, hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                binding.timeDisplay.text = selectedTime
                binding.root.showSnackbar(
                    "Selected time: $selectedTime (${
                        determineTimeCategory(
                            hour
                        )
                    })"
                )
                hideInLayoutBlurOverlay()
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        ).apply {
            setOnDismissListener { hideInLayoutBlurOverlay() }
        }.show()
    }

    private fun clearTimeSelection() {
        selectedTime = null
        binding.timeDisplay.text = ""
    }
    // endregion

    // region Task Creation
    private fun handleAddTask() {
        val title = binding.titleInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()

        if (title.isEmpty()) {
            binding.root.showSnackbar("Title is required.")
            return
        }

        // If date is selected as "today" but no time specified, set time to current
        if (selectedDate == getCurrentDate() && selectedTime == null) {
            selectedTime = getCurrentTime()
        }

        val hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeCategory = selectedTime?.let { determineTimeCategory(hourNow) } ?: "tonight"

        // Save task
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
    // endregion

    // region Utility
    private fun getCurrentDate(): String {
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun getCurrentTime(): String {
        return timeFormat.format(Calendar.getInstance().time)
    }

    private fun determineTimeCategory(hour: Int): String = when (hour) {
        in 5..11 -> "morning"
        in 12..17 -> "afternoon"
        else -> "tonight"
    }
    // endregion

    // region Programmatic Blur Background
    /**
     * Create and add a programmatically-generated BlurView as the background overlay beneath
     * the bottom sheet.
     */

    private fun setupBackgroundBlurView() {
        val decorView = requireActivity().window.decorView as ViewGroup
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)

        if (backgroundBlurContainer == null) {
            // Create a FrameLayout container that holds blur + gradient
            val blurContainer = FrameLayout(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Add the BlurView first
                addView(BlurView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setupWith(rootView).setBlurRadius(BLUR_RADIUS).setBlurAutoUpdate(true)
                        .setOverlayColor(android.graphics.Color.TRANSPARENT)
                })

                // Add a gradient overlay on top of the BlurView
                addView(View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    background = ContextCompat.getDrawable(
                        requireContext(), R.drawable.bg_blur_gradient
                    )
                })
            }

            // Add container to decorView
            decorView.addView(blurContainer, 1)

            // Assign the container to your variable
            backgroundBlurContainer = blurContainer
        }

        // Animate fade-in on the container
        backgroundBlurContainer?.apply {
            alpha = 0f
            animate().alpha(1f).setDuration(BLUR_ANIMATION_DURATION)
                .setInterpolator(DecelerateInterpolator()).start()
        }
    }


    /**
     * Remove the background blur view from the decor view.
     */
    private fun removeBlurView() {
        backgroundBlurContainer?.let { container ->
            (requireActivity().window.decorView as? ViewGroup)?.removeView(container)
            backgroundBlurContainer = null
        }
    }
    // endregion

    // region BottomSheet Callbacks
    private fun setupBottomSheetCallbacks(behavior: BottomSheetBehavior<*>?) {
        behavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING -> isDragging = true
                    BottomSheetBehavior.STATE_SETTLING -> isDragging = false
                    BottomSheetBehavior.STATE_HIDDEN -> removeBlurView()
                    else -> Unit
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val newAlpha = when {
                    // Expanding upward - maintain full blur
                    slideOffset >= 0 -> 1f

                    // Dragging downward below collapsed state - fade out
                    else -> 1f - downwardInterpolator.getInterpolation(abs(slideOffset))
                }

                backgroundBlurContainer?.alpha = newAlpha.coerceIn(0f, 1f)
                lastSlideOffset = slideOffset
            }
        })
    }
    // endregion

    // region In-Layout Blur Overlay
    /**
     * Show the smaller (in-layout) blur overlay.
     */
    private fun showInLayoutBlurOverlay() {
        binding.blurView.apply {
            alpha = 1f
            visibility = View.VISIBLE
        }
    }

    /**
     * Hide the smaller (in-layout) blur overlay.
     */
    private fun hideInLayoutBlurOverlay() {
        binding.blurView.visibility = View.GONE
    }
    // endregion
}

// region Extension Functions
/**
 * Hide the soft keyboard from a [View].
 */
private fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Show a [Snackbar] with a [message].
 */
fun View.showSnackbar(message: String) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
}

/**
 * Retrieve the device screen height in pixels.
 */
@Suppress("DEPRECATION")
fun Context.getScreenHeight(): Int {
    val displayMetrics = DisplayMetrics()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display?.getRealMetrics(displayMetrics)
    } else {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
    }
    return displayMetrics.heightPixels
}
// endregion