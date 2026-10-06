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

package com.looker.kenko.ui.exerciseDetail

import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.ViewModel
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.ExerciseSession
import com.looker.kenko.data.model.settings.DEFAULT_REST_TIMER_SECONDS
import com.looker.kenko.data.repository.ExerciseRepo
import com.looker.kenko.data.repository.SettingsRepo
import com.looker.kenko.ui.navigation.Routes
import com.looker.kenko.utils.asStateFlow
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

@HiltViewModel(assistedFactory = ExerciseDetailViewModel.Factory::class)
class ExerciseDetailViewModel @AssistedInject constructor(
    repo: ExerciseRepo,
    settingsRepo: SettingsRepo,
    private val uriHandler: UriHandler,
    @Assisted routeData: Routes.ExerciseDetail,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(routeData: Routes.ExerciseDetail): ExerciseDetailViewModel
    }

    val state: StateFlow<ExerciseDetailState> = combine(
        repo.observe(routeData.id),
        repo.history(routeData.id),
        settingsRepo.get { restTimerSeconds },
    ) { exercise, history, defaultRest ->
        if (exercise == null) {
            ExerciseDetailState.NotFound
        } else {
            ExerciseDetailState.Success(
                exercise = exercise,
                history = history,
                defaultRestSeconds = defaultRest,
            )
        }
    }.asStateFlow(ExerciseDetailState.Loading)

    fun openReference(reference: String) {
        try {
            uriHandler.openUri(reference)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}

sealed interface ExerciseDetailState {

    data object Loading : ExerciseDetailState

    // Deleted while open
    data object NotFound : ExerciseDetailState

    @Immutable
    data class Success(
        val exercise: Exercise,
        val history: List<ExerciseSession>,
        val defaultRestSeconds: Int = DEFAULT_REST_TIMER_SECONDS,
    ) : ExerciseDetailState
}
