package com.emm.mybest.data

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Process
import com.emm.mybest.domain.repository.AppRestarter

class AppRestarterImpl(
    private val context: Context,
    private val mainHandler: Handler,
) : AppRestarter {

    override fun restart() {
        mainHandler.postDelayed(::relaunch, CONFIRMATION_DELAY_MS)
    }

    private fun relaunch() {
        val launcher = context.packageManager.getLaunchIntentForPackage(context.packageName)?.component ?: return
        context.startActivity(Intent.makeRestartActivityTask(launcher))
        Process.killProcess(Process.myPid())
    }
}

private const val CONFIRMATION_DELAY_MS = 1_500L
