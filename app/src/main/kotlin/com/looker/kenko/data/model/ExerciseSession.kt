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
import kotlinx.datetime.LocalDate

// The sets of one exercise done in one workout
@Immutable
data class ExerciseSession(
    val sessionId: Int,
    val date: LocalDate,
    val routineName: String?,
    val sets: List<SetDraft>,
) {
    // Warm-ups only count when nothing else was done
    private val workSets: List<SetDraft>
        get() = sets.filter { it.type != SetType.Warmup }.ifEmpty { sets }

    val heaviest: SetDraft?
        get() = workSets.maxWithOrNull(compareBy<SetDraft>({ it.weight }, { it.repsOrDuration }))

    val mostReps: Int
        get() = workSets.maxOfOrNull { it.repsOrDuration } ?: 0
}
