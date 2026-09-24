/*
 * Copyright (C) 2026 LooKeR & Contributors
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

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.looker.kenko.R

object RestTimerNotifications {

    const val ONGOING_ID = 4_201
    private const val FINISHED_ID = 4_202

    private const val ONGOING_CHANNEL = "rest_timer"
    private const val FINISHED_CHANNEL = "rest_timer_done"

    private const val FINISHED_TIMEOUT_MILLIS = 60_000L
    private val VIBRATION_PATTERN = longArrayOf(0, 300, 150, 300, 150, 500)

    fun ongoing(context: Context, state: RestTimerState.Running?): Notification {
        createChannels(context)
        val builder = NotificationCompat.Builder(context, ONGOING_CHANNEL)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(context.getString(R.string.label_resting))
            .setContentIntent(openAppIntent(context))
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (state != null) {
            val remaining = state.remainingMillis(SystemClock.elapsedRealtime())
            builder
                .setWhen(System.currentTimeMillis() + remaining)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .addAction(
                    R.drawable.ic_add,
                    context.getString(R.string.label_add_seconds, REST_ADJUST_SECONDS),
                    serviceIntent(context, RestTimerService.ACTION_ADD_TIME),
                )
                .addAction(
                    R.drawable.ic_close,
                    context.getString(R.string.label_skip),
                    serviceIntent(context, RestTimerService.ACTION_SKIP),
                )
        }
        return builder.build()
    }

    fun updateOngoing(context: Context, state: RestTimerState.Running) {
        post(context, ONGOING_ID, ongoing(context, state))
    }

    // Vibrates when notifications are off
    fun notifyFinished(context: Context) {
        createChannels(context)
        val notification = NotificationCompat.Builder(context, FINISHED_CHANNEL)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(context.getString(R.string.label_rest_over))
            .setContentText(context.getString(R.string.label_rest_over_desc))
            .setContentIntent(openAppIntent(context))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setTimeoutAfter(FINISHED_TIMEOUT_MILLIS)
            .build()
        val alerts = isChannelEnabled(context, FINISHED_CHANNEL) &&
            post(context, FINISHED_ID, notification)
        if (!alerts) vibrate(context)
    }

    private fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                ONGOING_CHANNEL,
                context.getString(R.string.label_rest_timer),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                setShowBadge(false)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                FINISHED_CHANNEL,
                context.getString(R.string.label_rest_timer_alerts),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setShowBadge(false)
            },
        )
    }

    private fun isChannelEnabled(context: Context, channelId: String): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        val channel = manager.getNotificationChannel(channelId) ?: return false
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }

    private fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun post(context: Context, id: Int, notification: Notification): Boolean {
        if (!canPost(context)) return false
        return try {
            NotificationManagerCompat.from(context).notify(id, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator == null || !vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, -1))
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun serviceIntent(context: Context, action: String): PendingIntent =
        PendingIntent.getService(
            context,
            action.hashCode(),
            Intent(context, RestTimerService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
