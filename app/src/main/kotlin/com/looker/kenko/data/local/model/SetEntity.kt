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

package com.looker.kenko.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.RepsInReserve
import com.looker.kenko.data.model.Set
import com.looker.kenko.data.model.SetDraft

@Entity(
    "sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SessionDataEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = RoutineSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineSetId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("sessionId", "exerciseId"),
        Index("routineSetId"),
    ],
)
data class SetEntity(
    @ColumnInfo("reps")
    val repsOrDuration: Int,
    val weight: Float,
    val type: SetType,
    val order: Int,
    val sessionId: Int,
    val exerciseId: Int,
    val rir: Int = 2,
    @ColumnInfo(defaultValue = "1")
    val isCompleted: Boolean = true,
    // The planned set it was copied from
    val routineSetId: Int? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val restSeconds: Int? = null,
)

fun SetEntity.toExternal(exercise: Exercise): Set = Set(
    repsOrDuration = repsOrDuration,
    weight = weight,
    type = type,
    exercise = exercise,
    rir = RepsInReserve(rir),
    isCompleted = isCompleted,
    isFromPlan = routineSetId != null,
    id = id,
    restSeconds = restSeconds,
)

fun Set.toEntity(sessionId: Int, order: Int): SetEntity = SetEntity(
    id = id ?: 0,
    repsOrDuration = repsOrDuration,
    weight = weight,
    type = type,
    order = order,
    sessionId = sessionId,
    exerciseId = requireNotNull(exercise.id),
    rir = rir.value,
    isCompleted = isCompleted,
    restSeconds = restSeconds,
)

fun SetDraft.toSetEntity(
    sessionId: Int,
    exerciseId: Int,
    order: Int,
    isCompleted: Boolean,
    routineSetId: Int? = null,
): SetEntity = SetEntity(
    repsOrDuration = repsOrDuration,
    weight = weight,
    type = type,
    order = order,
    sessionId = sessionId,
    exerciseId = exerciseId,
    isCompleted = isCompleted,
    routineSetId = routineSetId,
    restSeconds = restSeconds,
)

data class ExerciseHistoryRow(
    val sessionId: Int,
    // Epoch days
    val date: Int,
    val routineName: String?,
    @ColumnInfo("reps")
    val repsOrDuration: Int,
    val weight: Float,
    val type: SetType,
)
