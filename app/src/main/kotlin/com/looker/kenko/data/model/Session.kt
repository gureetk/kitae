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

package com.looker.kenko.data.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class Session(
    val date: LocalDate,
    val sets: List<Set>,
    val planId: Int?,
    val routineId: Int? = null,
    val routineName: String? = null,
    val isFinished: Boolean = true,
    val id: Int? = null,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
) {
    val completedSets: List<Set>
        get() = sets.filter { it.isCompleted }

    val durationMillis: Long?
        get() = if (startedAt != null && finishedAt != null) (finishedAt - startedAt).coerceAtLeast(0L) else null

    val performExercises: List<Exercise>
        get() = completedSets.map { it.exercise }.distinct()
}

enum class FinishMode {
    KeepSkipped,

    /** Also removed from the plan. */
    RemoveFromPlan,

    /** Removed from this workout only. */
    Discard,
}

@Immutable
data class ActiveSession(
    val id: Int,
    val date: LocalDate,
    val routineId: Int?,
    val routineName: String?,
    val completedSets: Int,
    val totalSets: Int,
    val newSets: Int = 0,
)

fun Session(planId: Int, sets: List<Set>) = Session(planId = planId, date = localDate, sets = sets)
