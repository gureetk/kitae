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

const val REST_ADJUST_SECONDS = 15

sealed interface RestTimerState {

    data object Idle : RestTimerState

    /** Times are from [android.os.SystemClock.elapsedRealtime]. */
    data class Running(
        val startedAt: Long,
        val endsAt: Long,
    ) : RestTimerState {

        val totalMillis: Long
            get() = (endsAt - startedAt).coerceAtLeast(1L)

        fun remainingMillis(now: Long): Long = (endsAt - now).coerceAtLeast(0L)

        fun progress(now: Long): Float =
            (remainingMillis(now).toFloat() / totalMillis).coerceIn(0F, 1F)
    }
}

// m:ss, rounding up like countdowns do
fun formatClock(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0L) + 999L) / 1000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
