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

package com.looker.kenko.data.model

import androidx.compose.runtime.Immutable
import com.looker.kenko.data.local.model.SetType

@Immutable
data class Routine(
    val id: Int,
    val planId: Int,
    val name: String,
    val position: Int,
    val exerciseCount: Int = 0,
    val setCount: Int = 0,
)

// id is the routine slot's, not the exercise's
@Immutable
data class RoutineExercise(
    val id: Int,
    val exercise: Exercise,
    val sets: List<PlannedSet>,
)

@Immutable
data class PlannedSet(
    val id: Int,
    val repsOrDuration: Int,
    val weight: Float,
    val type: SetType,
    val restSeconds: Int? = null,
)

fun PlannedSet.toDraft(): SetDraft = SetDraft(
    repsOrDuration = repsOrDuration,
    weight = weight,
    type = type,
    restSeconds = restSeconds,
)

fun List<Routine>.nextAfter(lastRoutineId: Int?): Routine? {
    if (isEmpty()) return null
    val index = indexOfFirst { it.id == lastRoutineId }
    return if (index == -1) first() else get((index + 1) % size)
}
