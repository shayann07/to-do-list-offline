package com.shayan.remindersios.utils

import android.content.Context
import android.widget.Toast

/**
 * Displays a short toast message with [message].
 */
fun Context.shortToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}