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

package com.looker.kenko.ui.planEdit

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looker.kenko.R
import com.looker.kenko.data.StringHandler
import com.looker.kenko.data.local.model.SetType
import com.looker.kenko.data.model.DEFAULT_REPS
import com.looker.kenko.data.model.DEFAULT_SET_COUNT
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.PlannedSet
import com.looker.kenko.data.model.Routine
import com.looker.kenko.data.model.RoutineExercise
import com.looker.kenko.data.model.SetDraft
import com.looker.kenko.data.model.toDraft
import com.looker.kenko.data.repository.PlanRepo
import com.looker.kenko.data.repository.SessionRepo
import com.looker.kenko.data.repository.SettingsRepo
import com.looker.kenko.ui.navigation.Routes
import com.looker.kenko.utils.asStateFlow
import com.looker.kenko.utils.today
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@HiltViewModel(assistedFactory = PlanEditViewModel.Factory::class)
class PlanEditViewModel @AssistedInject constructor(
    private val repo: PlanRepo,
    private val sessionRepo: SessionRepo,
    private val settingsRepo: SettingsRepo,
    private val stringHandler: StringHandler,
    @Assisted private val routeData: Routes.PlanEdit,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(routeData: Routes.PlanEdit): PlanEditViewModel
    }

    // -1 until the plan is named
    private val planIdStream = MutableStateFlow(routeData.id)

    val planNameState: TextFieldState = TextFieldState("")

    val snackbarState = SnackbarHostState()

    private var isBackAlreadyPressedOnce = false

    @OptIn(FlowPreview::class)
    val isNameAlreadyUsed = snapshotFlow { planNameState.text.trim().toString() }
        .debounce(200.milliseconds)
        .map { repo.planNameExists(it) }
        .asStateFlow(false)

    val pageState: StateFlow<PlanEditStage> = planIdStream.map { id ->
        if (id == -1) PlanEditStage.NameEdit else PlanEditStage.PlanEdit
    }.asStateFlow(PlanEditStage.NameEdit)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val routinesStream: Flow<List<Routine>> = planIdStream.flatMapLatest { id ->
        if (id == -1) flowOf(emptyList()) else repo.routines(id)
    }

    private val pickedRoutineId = MutableStateFlow(routeData.routineId)

    private val selectedRoutineStream: Flow<Int?> =
        combine(routinesStream, pickedRoutineId) { routines, pickedId ->
            (routines.find { it.id == pickedId } ?: routines.firstOrNull())?.id
        }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val exercisesStream: Flow<List<RoutineExercise>> =
        selectedRoutineStream.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repo.routineExercises(id)
        }

    private val sheetStream = MutableStateFlow<PlanEditSheet?>(null)
    private val dialogStream = MutableStateFlow<RoutineDialog?>(null)
    private var sheetCount = 0L

    val state: StateFlow<PlanEditState> = combine(
        routinesStream,
        selectedRoutineStream,
        exercisesStream,
        sheetStream,
        dialogStream,
    ) { routines, selectedId, exercises, sheet, dialog ->
        PlanEditState(
            routines = routines,
            selectedRoutineId = selectedId,
            exercises = exercises,
            sheet = sheet,
            dialog = dialog,
        )
    }.asStateFlow(PlanEditState())

    fun saveName() {
        viewModelScope.launch {
            if (planNameState.text.isBlank()) {
                snackbarState.showSnackbar(stringHandler.getString(R.string.error_plan_name_empty))
                return@launch
            }
            if (isNameAlreadyUsed.value) {
                snackbarState.showSnackbar(stringHandler.getString(R.string.error_plan_name_exists))
                return@launch
            }
            val createId = repo.createPlan(planNameState.text.toString())
            planIdStream.emit(createId)
        }
    }

    fun selectRoutine(id: Int) {
        pickedRoutineId.value = id
    }

    fun onAddClick() {
        if (state.value.routines.isEmpty()) {
            dialogStream.value = RoutineDialog.Create
        } else {
            sheetStream.value = PlanEditSheet.AddExercise
        }
    }

    fun openCreateRoutine() {
        dialogStream.value = RoutineDialog.Create
    }

    fun openRenameRoutine() {
        val routine = state.value.selectedRoutine ?: return
        dialogStream.value = RoutineDialog.Rename(routine)
    }

    fun openDeleteRoutine() {
        val routine = state.value.selectedRoutine ?: return
        dialogStream.value = RoutineDialog.Delete(routine)
    }

    fun dismissDialog() {
        dialogStream.value = null
    }

    fun createRoutine(name: String) {
        val planId = planIdStream.value
        if (name.isBlank() || planId == -1) return
        dialogStream.value = null
        viewModelScope.launch {
            pickedRoutineId.value = repo.createRoutine(planId, name)
        }
    }

    fun renameRoutine(id: Int, name: String) {
        if (name.isBlank()) return
        dialogStream.value = null
        viewModelScope.launch {
            repo.renameRoutine(id, name)
        }
    }

    fun deleteRoutine(id: Int) {
        dialogStream.value = null
        viewModelScope.launch {
            repo.deleteRoutine(id)
        }
    }

    fun closeSheet() {
        sheetStream.value = null
    }

    fun addExercise(exercise: Exercise) {
        val exerciseId = exercise.id ?: return
        val routineId = state.value.selectedRoutineId ?: return
        viewModelScope.launch {
            repo.addExercise(routineId, exerciseId, suggestedSets(exerciseId))
        }
    }

    fun removeExercise(routineExerciseId: Int) {
        viewModelScope.launch {
            repo.removeExercise(routineExerciseId)
        }
    }

    fun openAddSet(routineExercise: RoutineExercise) {
        viewModelScope.launch {
            val initial = routineExercise.sets.lastOrNull()?.toDraft() ?: defaultSet()
            sheetStream.value = PlanEditSheet.EditSet(
                id = ++sheetCount,
                routineExerciseId = routineExercise.id,
                exercise = routineExercise.exercise,
                initial = initial,
            )
        }
    }

    fun openEditSet(routineExercise: RoutineExercise, set: PlannedSet) {
        sheetStream.value = PlanEditSheet.EditSet(
            id = ++sheetCount,
            routineExerciseId = routineExercise.id,
            exercise = routineExercise.exercise,
            initial = set.toDraft(),
            setId = set.id,
        )
    }

    fun saveSet(set: SetDraft) {
        val sheet = sheetStream.value as? PlanEditSheet.EditSet ?: return
        if (sheet.isSaved) return
        sheetStream.value = sheet.copy(isSaved = true)
        viewModelScope.launch {
            if (sheet.setId == null) {
                repo.addPlannedSet(sheet.routineExerciseId, set)
            } else {
                repo.updatePlannedSet(sheet.setId, set)
            }
        }
    }

    fun removeSet(setId: Int) {
        viewModelScope.launch {
            repo.removePlannedSet(setId)
        }
    }

    fun onBackPress(stage: PlanEditStage, onBackPress: () -> Unit) {
        viewModelScope.launch {
            if (stage == PlanEditStage.NameEdit) {
                onBackPress()
                return@launch
            }
            val planId = planIdStream.value
            if (repo.hasExercises(planId)) {
                onBackPress()
                return@launch
            }
            if (isBackAlreadyPressedOnce) {
                repo.deletePlan(planId)
                onBackPress()
                return@launch
            }
            isBackAlreadyPressedOnce = true
            snackbarState.showSnackbar(stringHandler.getString(R.string.error_plan_empty_prompt))
        }
    }

    fun done(onDone: () -> Unit) {
        viewModelScope.launch {
            if (repo.hasExercises(planIdStream.value)) {
                onDone()
            } else {
                snackbarState.showSnackbar(stringHandler.getString(R.string.error_plan_needs_exercise))
            }
        }
    }

    private suspend fun suggestedSets(exerciseId: Int): List<SetDraft> {
        val lastTime = sessionRepo.getLastSessionSets(exerciseId)
        if (lastTime.isNotEmpty()) return lastTime
        val set = defaultSet()
        return List(DEFAULT_SET_COUNT) { set }
    }

    private suspend fun defaultSet(): SetDraft {
        val unit = settingsRepo.get { weightUnit }.first()
        return SetDraft(repsOrDuration = DEFAULT_REPS, weight = unit.defaultWeightKg)
    }

    fun debugFillMockData(sessions: Int = 9) {
        viewModelScope.launch {
            val planId = planIdStream.value
            val routines = repo.routines(planId).first().filter { it.exerciseCount > 0 }
            if (routines.isEmpty()) {
                snackbarState.showSnackbar("Add exercises to a day to generate mock workouts")
                return@launch
            }
            val today = today().toEpochDays().toInt()
            var added = 0
            repeat(sessions) {
                val routine = routines.random()
                val sets = repo.routineExercises(routine.id).first().flatMap { routineExercise ->
                    val exerciseId = routineExercise.exercise.id
                        ?: return@flatMap emptyList<Pair<Int, SetDraft>>()
                    List(Random.nextInt(1, 4)) {
                        exerciseId to SetDraft(
                            repsOrDuration = Random.nextInt(5, 15),
                            weight = Random.nextInt(10, 80).toFloat(),
                            type = SetType.entries.random(),
                        )
                    }
                }
                sessionRepo.createSession(
                    date = LocalDate.fromEpochDays(today - Random.nextInt(1, sessions * 2)),
                    planId = planId,
                    routineId = routine.id,
                    sets = sets,
                    isCompleted = true,
                )
                added += sets.size
            }
            snackbarState.showSnackbar("Mock data added: $added sets")
        }
    }
}

@Stable
enum class PlanEditStage {
    NameEdit,
    PlanEdit,
}

@Immutable
data class PlanEditState(
    val routines: List<Routine> = emptyList(),
    val selectedRoutineId: Int? = null,
    val exercises: List<RoutineExercise> = emptyList(),
    val sheet: PlanEditSheet? = null,
    val dialog: RoutineDialog? = null,
) {
    val selectedRoutine: Routine?
        get() = routines.find { it.id == selectedRoutineId }
}

sealed interface PlanEditSheet {

    data object AddExercise : PlanEditSheet

    data class EditSet(
        val id: Long,
        val routineExerciseId: Int,
        val exercise: Exercise,
        val initial: SetDraft,
        val setId: Int? = null,
        val isSaved: Boolean = false,
    ) : PlanEditSheet
}

sealed interface RoutineDialog {

    data object Create : RoutineDialog

    data class Rename(val routine: Routine) : RoutineDialog

    data class Delete(val routine: Routine) : RoutineDialog
}
