package com.shayan.remindersios.utils

import android.view.View
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout
import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout

/**
 * A utility for setting up Ultra Pull-To-Refresh using [PtrClassicFrameLayout].
 */
object PullToRefreshUtil {

    // region Public API
    /**
     * Configures a [ptrFrameLayout] to execute [fetchData] when the user performs a pull-to-refresh.
     * Once [fetchData] completes, the refresh animation ends after a short delay.
     *
     * @param ptrFrameLayout The [PtrClassicFrameLayout] to configure.
     * @param fetchData A lambda to fetch or refresh data. Called when onRefreshBegin is triggered.
     */
    fun setupUltraPullToRefresh(
        ptrFrameLayout: PtrClassicFrameLayout, fetchData: () -> Unit
    ) {
        ptrFrameLayout.setPtrHandler(object : PtrDefaultHandler() {

            override fun onRefreshBegin(frame: PtrFrameLayout) {
                // 1) Invoke data fetch
                fetchData()

                // 2) Stop the refresh animation after a short delay
                ptrFrameLayout.postDelayed({
                    ptrFrameLayout.refreshComplete()
                }, REFRESH_COMPLETE_DELAY)
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout, content: View, header: View
            ): Boolean {
                // Only allow pull-to-refresh if content can be scrolled to the top
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }
        })
    }
    // endregion

    // region Constants
    private const val REFRESH_COMPLETE_DELAY = 1500L
    // endregion
}