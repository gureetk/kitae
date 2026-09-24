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

import androidx.compose.runtime.Immutable
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.Set

// In progress: routine order, history: the order sets were done
internal fun groupSessionExercises(
    sets: List<Set>,
    routineExercises: List<Exercise>,
    isEditable: Boolean,
): List<SessionExercise> {
    val groups = LinkedHashMap<Int, SessionExercise>()
    if (isEditable) {
        routineExercises.forEach { exercise ->
            val id = exercise.id ?: return@forEach
            if (id !in groups) groups[id] = SessionExercise(exercise, emptyList())
        }
    }
    sets.forEach { set ->
        if (!isEditable && !set.isCompleted) return@forEach
        val id = set.exercise.id ?: return@forEach
        val group = groups[id] ?: SessionExercise(set.exercise, emptyList())
        groups[id] = group.copy(sets = group.sets + set)
    }
    return groups.values.toList()
}

@Immutable
data class SessionExercise(
    val exercise: Exercise,
    val sets: List<Set>,
)
