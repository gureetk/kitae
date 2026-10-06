/*
 * Copyright (C) 2026 Kitae Contributors
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.looker.kenko.data.timer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RestTimerService : Service() {

    @Inject
    lateinit var restTimer: RestTimer

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observer: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var lastStartId = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lastStartId = startId
        when (intent?.action) {
            ACTION_SKIP -> restTimer.skip()
            ACTION_ADD_TIME -> restTimer.adjust(REST_ADJUST_SECONDS.seconds)
            ACTION_REMOVE_TIME -> restTimer.adjust(-REST_ADJUST_SECONDS.seconds)
            // Required after startForegroundService()
            else -> if (!promote(restTimer.state.value as? RestTimerState.Running)) {
                stopSelf(startId)
                return START_NOT_STICKY
            }
        }
        if (restTimer.state.value !is RestTimerState.Running) {
            stop()
            return START_NOT_STICKY
        }
        if (observer == null) {
            observer = scope.launch {
                restTimer.state.collect { state ->
                    when (state) {
                        is RestTimerState.Running -> {
                            RestTimerNotifications.updateOngoing(this@RestTimerService, state)
                            holdWakeLock(state)
                        }

                        RestTimerState.Idle -> stop()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun promote(state: RestTimerState.Running?): Boolean {
        val notification = RestTimerNotifications.ongoing(this, state)
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    RestTimerNotifications.ONGOING_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
                )
            } else {
                startForeground(RestTimerNotifications.ONGOING_ID, notification)
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "Could not start the rest timer service", e)
            false
        }
    }

    private fun holdWakeLock(state: RestTimerState.Running) {
        val lock = wakeLock ?: getSystemService(PowerManager::class.java)
            ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Kitae:RestTimer")
            ?.apply { setReferenceCounted(false) }
            ?.also { wakeLock = it }
            ?: return
        val remaining = state.remainingMillis(SystemClock.elapsedRealtime())
        // Not reference counted, this only moves the timeout
        lock.acquire(remaining + WAKE_LOCK_MARGIN_MILLIS)
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    private fun stop() {
        observer?.cancel()
        observer = null
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(lastStartId)
    }

    override fun onDestroy() {
        scope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    companion object {
        const val ACTION_SKIP = "com.looker.kenko.rest_timer.SKIP"
        const val ACTION_ADD_TIME = "com.looker.kenko.rest_timer.ADD_TIME"
        const val ACTION_REMOVE_TIME = "com.looker.kenko.rest_timer.REMOVE_TIME"

        private const val TAG = "RestTimerService"
        private const val WAKE_LOCK_MARGIN_MILLIS = 10_000L

        fun start(context: Context) {
            try {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, RestTimerService::class.java),
                )
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Could not start the rest timer service", e)
            }
        }
    }
}
