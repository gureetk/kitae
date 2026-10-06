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

package com.looker.kenko.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Routes : NavKey {
    @Serializable
    data class GetStarted(val isOnboardingDone: Boolean) : Routes

    @Serializable
    data object Home : Routes

    @Serializable
    data object Plan : Routes

    @Serializable
    data object Session : Routes

    @Serializable
    data object Settings : Routes

    @Serializable
    data object Profile : Routes

    @Serializable
    data object Exercises : Routes

    @Serializable
    data class PlanEdit(val id: Int, val routineId: Int? = null) : Routes

    @Serializable
    data class SessionDetail(val sessionId: Int) : Routes

    @Serializable
    data class ExerciseDetail(val id: Int) : Routes

    @Serializable
    data class AddEditExercise(
        val id: Int? = null,
        val name: String? = null,
        val target: String? = null,
    ) : Routes
}
