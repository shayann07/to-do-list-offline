package com.shayan.remindersios.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shayan.remindersios.R
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.databinding.ReminderItemsBinding

/**
 * A RecyclerView adapter for displaying a list of [Tasks] objects.
 * Supports task completion toggling, item click, and delete actions via listener interfaces.
 */
class TaskAdapter(
    private val completionListener: TaskCompletionListener,
    private val itemClickListener: OnItemClickListener,
    private val deleteClickListener: OnDeleteClickListener
) : ListAdapter<Tasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    // region Adapter Methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            ReminderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        val isLastItem = (position == itemCount - 1)
        holder.bind(task, completionListener, itemClickListener, deleteClickListener, isLastItem)

        // Debug log for item binding
        Log.d("TaskAdapter", "Task bound: ${task.title}")
    }
    // endregion

    // region ViewHolder
    /**
     * A ViewHolder that binds a single [Tasks] item to the UI elements in [ReminderItemsBinding].
     */
    class TaskViewHolder(private val binding: ReminderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds [task] data to the ViewHolder's UI fields, sets up click and completion listeners.
         *
         * @param isLastItem Whether this item is the last in the list (used to hide divider).
         */
        fun bind(
            task: Tasks,
            completionListener: TaskCompletionListener,
            itemClickListener: OnItemClickListener,
            deleteClickListener: OnDeleteClickListener,
            isLastItem: Boolean
        ) {
            binding.apply {
                // Title
                fetchedTaskTitle.text = task.title

                // Time display & color
                if (!task.time.isNullOrBlank()) {
                    fetchedTaskTime.text = task.time
                    fetchedTaskTime.setTextColor(
                        ContextCompat.getColor(root.context, R.color.light_blue)
                    )
                } else {
                    fetchedTaskTime.text = root.context.getString(R.string.not_available)
                    fetchedTaskTime.setTextColor(
                        ContextCompat.getColor(root.context, R.color.orange)
                    )
                }

                // Completion handling
                radioButton.apply {
                    setOnCheckedChangeListener(null) // Avoid unwanted triggers
                    isChecked = task.isCompleted
                    setOnCheckedChangeListener { _, isChecked ->
                        completionListener.onTaskCompletionToggled(task.roomTaskId, isChecked)
                    }
                }

                // Divider visibility
                recyclerViewDivider.visibility = if (isLastItem) View.GONE else View.VISIBLE

                // Click listeners
                deleteTask.setOnClickListener {
                    deleteClickListener.onDeleteClick(task)
                }
                root.setOnClickListener {
                    itemClickListener.onItemClick(task)
                }
            }
        }
    }
    // endregion

    // region DiffCallback
    /**
     * A [DiffUtil.ItemCallback] to efficiently update only changed items in the list.
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<Tasks>() {
        override fun areItemsTheSame(oldItem: Tasks, newItem: Tasks): Boolean =
            oldItem.roomTaskId == newItem.roomTaskId

        override fun areContentsTheSame(oldItem: Tasks, newItem: Tasks): Boolean =
            oldItem == newItem
    }
    // endregion

    // region Listener Interfaces
    /**
     * Callback for toggling a task's completion status.
     */
    interface TaskCompletionListener {
        fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean)
    }

    /**
     * Callback for item-click events (e.g., viewing detailed task info).
     */
    interface OnItemClickListener {
        fun onItemClick(task: Tasks)
    }

    /**
     * Callback for when the delete icon is clicked on a task item.
     */
    interface OnDeleteClickListener {
        fun onDeleteClick(task: Tasks)
    }
    // endregion
}