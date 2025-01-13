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

class TaskAdapter(
    private val completionListener: TaskCompletionListener,
    private val itemClickListener: OnItemClickListener,
    private val deleteClickListener: OnDeleteClickListener
) : ListAdapter<Tasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding =
            ReminderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        val isLastItem = position == itemCount - 1
        holder.bind(task, completionListener, itemClickListener, deleteClickListener, isLastItem)
        Log.d("TaskAdapter", "Task bound: ${task.title}")
    }

    class TaskViewHolder(private val binding: ReminderItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            task: Tasks,
            completionListener: TaskCompletionListener,
            itemClickListener: OnItemClickListener,
            deleteClickListener: OnDeleteClickListener,
            isLastItem: Boolean
        ) {
            binding.apply {
                fetchedTaskTitle.text = task.title

                // Time and color handling
                if (!task.time.isNullOrBlank()) {
                    fetchedTaskTime.text = task.time
                    fetchedTaskTime.setTextColor(
                        ContextCompat.getColor(
                            root.context, R.color.light_blue
                        )
                    )
                } else {
                    fetchedTaskTime.text = root.context.getString(R.string.not_available)
                    fetchedTaskTime.setTextColor(
                        ContextCompat.getColor(
                            root.context, R.color.orange
                        )
                    )
                }

                // Task completion handling
                radioButton.apply {
                    setOnCheckedChangeListener(null)
                    isChecked = task.isCompleted
                    setOnCheckedChangeListener { _, isChecked ->
                        completionListener.onTaskCompletionToggled(task.roomTaskId, isChecked)
                    }
                }

                // Last item divider visibility
                recyclerViewDivider.visibility = if (isLastItem) View.GONE else View.VISIBLE

                // Click listeners
                deleteTask.setOnClickListener { deleteClickListener.onDeleteClick(task) }
                root.setOnClickListener { itemClickListener.onItemClick(task) }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Tasks>() {
        override fun areItemsTheSame(oldItem: Tasks, newItem: Tasks): Boolean =
            oldItem.roomTaskId == newItem.roomTaskId

        override fun areContentsTheSame(oldItem: Tasks, newItem: Tasks): Boolean =
            oldItem == newItem
    }

    interface TaskCompletionListener {
        fun onTaskCompletionToggled(roomTaskId: Int, isCompleted: Boolean)
    }

    interface OnItemClickListener {
        fun onItemClick(task: Tasks)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(task: Tasks)
    }
}