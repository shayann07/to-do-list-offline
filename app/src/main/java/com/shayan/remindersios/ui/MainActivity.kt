package com.shayan.remindersios.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.shayan.remindersios.R

/**
 * Main entry point of the application, hosting the navigation graph
 * and requesting notification permissions on Android 13+.
 */
class MainActivity : AppCompatActivity() {

    // region Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // If you have a shortToast extension:
            // applicationContext?.shortToast("Notification permission denied")
            // Otherwise:
            android.widget.Toast.makeText(
                this, "Notification permission denied", android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    // endregion

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
        checkAndRequestNotificationPermission()

        // Force dark mode - remove or adjust to your preference
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    // endregion

    // region Setup
    /**
     * Finds the NavHostFragment and retrieves its NavController.
     * You can add any navController setup here (e.g. app bar config).
     */
    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Optional: Setup ActionBar or other nav configs
    }
    // endregion

    // region Notification Permission
    /**
     * Checks and requests the POST_NOTIFICATIONS permission on Android 13+ (TIRAMISU).
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    // Already granted
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale and then request
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                else -> {
                    // Directly request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
    // endregion
}