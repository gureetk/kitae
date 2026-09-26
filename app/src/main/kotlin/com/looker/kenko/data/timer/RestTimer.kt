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

import android.content.Context
import android.os.SystemClock
import com.looker.kenko.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class RestTimer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:ApplicationScope private val scope: CoroutineScope,
) {

    private val _state = MutableStateFlow<RestTimerState>(RestTimerState.Idle)
    val state: StateFlow<RestTimerState> = _state.asStateFlow()

    private var completion: Job? = null

    fun start(duration: Duration) {
        if (!duration.isPositive()) return
        val now = SystemClock.elapsedRealtime()
        update(RestTimerState.Running(startedAt = now, endsAt = now + duration.inWholeMilliseconds))
    }

    fun adjust(delta: Duration) {
        val running = _state.value as? RestTimerState.Running ?: return
        val endsAt = running.endsAt + delta.inWholeMilliseconds
        if (endsAt <= SystemClock.elapsedRealtime()) {
            update(RestTimerState.Idle)
        } else {
            update(running.copy(endsAt = endsAt))
        }
    }

    fun skip() {
        if (_state.value is RestTimerState.Running) update(RestTimerState.Idle)
    }

    private fun update(newState: RestTimerState) {
        val wasRunning = _state.value is RestTimerState.Running
        _state.value = newState
        completion?.cancel()
        completion = null
        if (newState !is RestTimerState.Running) return
        if (!wasRunning) RestTimerService.start(context)
        completion = scope.launch {
            delay(newState.endsAt - SystemClock.elapsedRealtime())
            if (_state.value != newState) return@launch
            // Alert before going idle, which stops the service
            RestTimerNotifications.notifyFinished(context)
            _state.compareAndSet(newState, RestTimerState.Idle)
        }
    }
}
