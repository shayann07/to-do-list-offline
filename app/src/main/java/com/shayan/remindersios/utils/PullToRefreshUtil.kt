package com.shayan.remindersios.utils

import android.view.View
import `in`.srain.cube.views.ptr.PtrClassicFrameLayout
import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout

object PullToRefreshUtil {

    fun setupUltraPullToRefresh(
        ptrFrameLayout: PtrClassicFrameLayout, fetchData: () -> Unit
    ) {
        ptrFrameLayout.setPtrHandler(object : PtrDefaultHandler() {
            override fun onRefreshBegin(frame: PtrFrameLayout) {
                // Fetch data
                fetchData()

                // Stop refresh animation after data load
                ptrFrameLayout.postDelayed({
                    ptrFrameLayout.refreshComplete()
                }, 1500)
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout, content: View, header: View
            ): Boolean {
                // Allow pull-to-refresh only when RecyclerView is at the top
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }
        })
    }
}