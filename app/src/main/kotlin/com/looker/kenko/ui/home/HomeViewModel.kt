/*
 * Copyright (C) 2025 LooKeR & Contributors
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

package com.looker.kenko.ui.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looker.kenko.data.model.ActiveSession
import com.looker.kenko.data.model.Routine
import com.looker.kenko.data.model.nextAfter
import com.looker.kenko.data.repository.PlanRepo
import com.looker.kenko.data.repository.SessionRepo
import com.looker.kenko.data.timer.RestTimer
import com.looker.kenko.utils.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    planRepo: PlanRepo,
    private val sessionRepo: SessionRepo,
    private val restTimer: RestTimer,
) : ViewModel() {

    private val pickedRoutineId = MutableStateFlow<Int?>(null)

    private val history: Flow<WorkoutHistory> = combine(
        sessionRepo.activeSession,
        sessionRepo.lastPerformedRoutineId,
        sessionRepo.hasCompletedSets,
    ) { active, lastRoutineId, hasHistory ->
        WorkoutHistory(active, lastRoutineId, hasHistory)
    }

    val state: StateFlow<HomeUiData> = combine(
        planRepo.current,
        planRepo.currentRoutines,
        history,
        pickedRoutineId,
    ) { plan, routines, history, pickedId ->
        val active = history.activeSession
        val lastRoutineId = history.lastRoutineId
        val hasHistory = history.hasCompletedSets
        val showActive = active != null && (pickedId == null || pickedId == active.routineId)
        val selected = routines.find { it.id == pickedId }
            ?: routines.find { it.id == active?.routineId }
            ?: routines.nextAfter(lastRoutineId)
        HomeUiData(
            isPlanSelected = plan != null,
            planId = plan?.id,
            routines = routines,
            selectedRoutineId = if (showActive) active?.routineId else selected?.id,
            activeSession = active,
            showActive = showActive,
            isFirstSession = !hasHistory,
        )
    }.asStateFlow(HomeUiData(isLoading = true))

    private var isStarting = false

    fun selectRoutine(id: Int) {
        pickedRoutineId.value = id
    }

    fun startRoutine(routineId: Int, onStarted: (sessionId: Int) -> Unit) {
        start(onStarted) { sessionRepo.startSession(routineId) }
    }

    fun finishAndStart(
        activeSessionId: Int,
        routineId: Int,
        keepIncompleteSets: Boolean,
        onStarted: (sessionId: Int) -> Unit,
    ) {
        start(onStarted) {
            restTimer.skip()
            sessionRepo.finishSession(activeSessionId, keepIncompleteSets)
            sessionRepo.startSession(routineId)
        }
    }

    private inline fun start(
        crossinline onStarted: (Int) -> Unit,
        crossinline block: suspend () -> Int,
    ) {
        // Ignore double taps
        if (isStarting) return
        isStarting = true
        viewModelScope.launch {
            try {
                val sessionId = block()
                pickedRoutineId.value = null
                onStarted(sessionId)
            } finally {
                isStarting = false
            }
        }
    }
}

private data class WorkoutHistory(
    val activeSession: ActiveSession?,
    val lastRoutineId: Int?,
    val hasCompletedSets: Boolean,
)

@Immutable
data class HomeUiData(
    val isLoading: Boolean = false,
    val isPlanSelected: Boolean = true,
    val planId: Int? = null,
    val routines: List<Routine> = emptyList(),
    val selectedRoutineId: Int? = null,
    val activeSession: ActiveSession? = null,
    val showActive: Boolean = false,
    val isFirstSession: Boolean = false,
) {
    val selectedRoutine: Routine?
        get() = routines.find { it.id == selectedRoutineId }
}
