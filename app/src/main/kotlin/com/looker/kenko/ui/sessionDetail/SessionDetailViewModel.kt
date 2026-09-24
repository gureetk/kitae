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

package com.looker.kenko.ui.sessionDetail

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looker.kenko.R
import com.looker.kenko.data.model.DEFAULT_REPS
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.RoutineExercise
import com.looker.kenko.data.model.Session
import com.looker.kenko.data.model.Set
import com.looker.kenko.data.model.SetDraft
import com.looker.kenko.data.model.settings.DEFAULT_REST_TIMER_SECONDS
import com.looker.kenko.data.model.toDraft
import com.looker.kenko.data.repository.PlanRepo
import com.looker.kenko.data.repository.SessionRepo
import com.looker.kenko.data.repository.SettingsRepo
import com.looker.kenko.data.timer.RestTimer
import com.looker.kenko.data.timer.RestTimerState
import com.looker.kenko.ui.navigation.Routes
import com.looker.kenko.utils.asStateFlow
import com.looker.kenko.utils.today
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@HiltViewModel(assistedFactory = SessionDetailViewModel.Factory::class)
class SessionDetailViewModel @AssistedInject constructor(
    private val repo: SessionRepo,
    private val planRepo: PlanRepo,
    private val settingsRepo: SettingsRepo,
    private val timer: RestTimer,
    private val uriHandler: UriHandler,
    @Assisted private val routeData: Routes.SessionDetail,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(routeData: Routes.SessionDetail): SessionDetailViewModel
    }

    private val sessionId: Int = routeData.sessionId

    private val sessionStream: Flow<Session?> = repo.session(sessionId)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val routineStream: Flow<List<RoutineExercise>> = sessionStream
        .map { it?.routineId }
        .distinctUntilChanged()
        .flatMapLatest { routineId ->
            if (routineId == null) flowOf(emptyList()) else planRepo.routineExercises(routineId)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val previousSessionStream: Flow<Int?> = sessionStream
        .map { session -> session?.let { it.routineId to it.date } }
        .distinctUntilChanged()
        .mapLatest { key ->
            key?.let { repo.previousSessionId(sessionId, it.first, it.second) }
        }

    private val isFinishing = MutableStateFlow(false)

    private var routine: List<RoutineExercise> = emptyList()
    private var lastSuccess: SessionDetailState.Success? = null

    val state: StateFlow<SessionDetailState> = combine(
        sessionStream,
        routineStream,
        previousSessionStream,
        isFinishing,
    ) { session, routine, previousSessionId, finishing ->
        this.routine = routine
        // It may be deleted while leaving
        if (finishing) return@combine lastSuccess ?: SessionDetailState.Loading
        if (session == null) return@combine SessionDetailState.Error.InvalidSession
        SessionDetailState.Success(session.toUiData(routine, previousSessionId))
            .also { lastSuccess = it }
    }.asStateFlow(SessionDetailState.Loading)

    val restTimer: StateFlow<RestTimerState> = timer.state

    val restSeconds: StateFlow<Int> = settingsRepo.get { restTimerSeconds }
        .asStateFlow(DEFAULT_REST_TIMER_SECONDS)

    private val _sheet = MutableStateFlow<SetSheet?>(null)
    val sheet: StateFlow<SetSheet?> = _sheet.asStateFlow()
    private var sheetCount = 0L

    fun toggleSet(set: Set) {
        val setId = set.id ?: return
        val completing = !set.isCompleted
        val data = (state.value as? SessionDetailState.Success)?.data
        val isLastSet = data != null && data.incompleteSets <= 1
        viewModelScope.launch {
            repo.setCompleted(setId, completing)
            if (completing && !isLastSet) {
                val seconds = settingsRepo.get { restTimerSeconds }.first()
                if (seconds > 0) timer.start(seconds.seconds)
            }
        }
    }

    fun removeSet(setId: Int?) {
        if (setId == null) return
        viewModelScope.launch {
            repo.removeSet(setId)
        }
    }

    fun openAddSet(exercise: Exercise) {
        val exerciseId = exercise.id ?: return
        viewModelScope.launch {
            val data = (state.value as? SessionDetailState.Success)?.data
            val initial = data?.exercises
                ?.firstOrNull { it.exercise.id == exerciseId }
                ?.sets?.lastOrNull()?.toDraft()
                ?: routine.firstOrNull { it.exercise.id == exerciseId }
                    ?.sets?.lastOrNull()?.toDraft()
                ?: repo.getLastSetByExerciseId(exerciseId)?.toDraft()
                ?: defaultSet()
            _sheet.value = SetSheet(id = ++sheetCount, exercise = exercise, initial = initial)
        }
    }

    fun openEditSet(set: Set) {
        val setId = set.id ?: return
        _sheet.value = SetSheet(
            id = ++sheetCount,
            exercise = set.exercise,
            initial = set.toDraft(),
            setId = setId,
        )
    }

    fun saveSheet(set: SetDraft) {
        val sheet = _sheet.value ?: return
        if (sheet.isSaved) return
        _sheet.value = sheet.copy(isSaved = true)
        viewModelScope.launch {
            if (sheet.setId != null) {
                repo.updateSet(sheet.setId, set)
            } else {
                val exerciseId = sheet.exercise.id ?: return@launch
                repo.addSet(sessionId = sessionId, exerciseId = exerciseId, set = set)
            }
        }
    }

    fun dismissSheet() {
        _sheet.value = null
    }

    fun finish(keepIncompleteSets: Boolean, onFinished: () -> Unit) {
        if (isFinishing.value) return
        isFinishing.value = true
        viewModelScope.launch {
            timer.skip()
            repo.finishSession(sessionId, keepIncompleteSets)
            onFinished()
        }
    }

    fun startRest() {
        val seconds = restSeconds.value
        if (seconds > 0) timer.start(seconds.seconds)
    }

    fun adjustRest(seconds: Int) {
        timer.adjust(seconds.seconds)
    }

    fun skipRest() {
        timer.skip()
    }

    fun openReference(reference: String) {
        viewModelScope.launch {
            try {
                uriHandler.openUri(reference)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun defaultSet(): SetDraft {
        val unit = settingsRepo.get { weightUnit }.first()
        return SetDraft(repsOrDuration = DEFAULT_REPS, weight = unit.defaultWeightKg)
    }

    private fun Session.toUiData(
        routine: List<RoutineExercise>,
        previousSessionId: Int?,
    ): SessionUiData {
        val today = today()
        val isEditable = !isFinished && (
            date == today ||
                (hasIncompleteSets && date.toEpochDays() >= today.toEpochDays() - 1)
            )
        return SessionUiData(
            sessionId = sessionId,
            date = date,
            title = routineName,
            exercises = groupSessionExercises(
                sets = sets,
                routineExercises = routine.map { it.exercise },
                isEditable = isEditable,
            ),
            isEditable = isEditable,
            planId = planId,
            routineId = routineId,
            previousSessionId = previousSessionId,
            completedSets = completedSets.size,
            totalSets = sets.size,
        )
    }
}

@Immutable
data class SetSheet(
    val id: Long,
    val exercise: Exercise,
    val initial: SetDraft,
    val setId: Int? = null,
    val isSaved: Boolean = false,
)

@Stable
data class SessionUiData(
    val sessionId: Int,
    val date: LocalDate,
    val title: String?,
    val exercises: List<SessionExercise>,
    val isEditable: Boolean,
    val planId: Int? = null,
    val routineId: Int? = null,
    val previousSessionId: Int? = null,
    val completedSets: Int = 0,
    val totalSets: Int = 0,
) {
    val incompleteSets: Int
        get() = totalSets - completedSets
}

sealed interface SessionDetailState {

    data object Loading : SessionDetailState

    data class Success(val data: SessionUiData) : SessionDetailState

    sealed class Error(
        @param:StringRes val title: Int,
        @param:StringRes val errorMessage: Int,
    ) : SessionDetailState {
        data object InvalidSession : Error(
            title = R.string.label_missed_day,
            errorMessage = R.string.error_cant_find_session,
        )
    }
}
