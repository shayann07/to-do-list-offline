package com.shayan.remindersios.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tasks")
data class Tasks(
    @PrimaryKey(autoGenerate = true) val roomTaskId: Int = 0,
    val title: String = "",
    val notes: String? = null,
    val date: String? = null,
    val dateCompleted: String? = null,
    val time: String? = null,
    val timeCategory: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val flag: Boolean = false,
    val isCompleted: Boolean = false
) : Parcelable