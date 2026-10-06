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

package com.looker.kenko.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.looker.kenko.data.model.PlannedSet
import com.looker.kenko.data.model.Routine
import com.looker.kenko.data.model.RoutineExercise
import com.looker.kenko.data.model.SetDraft

@Entity(
    tableName = "routines",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("planId"),
    ],
)
data class RoutineEntity(
    val planId: Int,
    val name: String,
    val position: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("routineId"),
        Index("exerciseId"),
    ],
)
data class RoutineExerciseEntity(
    val routineId: Int,
    val exerciseId: Int,
    val position: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

@Entity(
    tableName = "routine_sets",
    foreignKeys = [
        ForeignKey(
            entity = RoutineExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineExerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("routineExerciseId"),
    ],
)
data class RoutineSetEntity(
    val routineExerciseId: Int,
    @ColumnInfo("reps")
    val repsOrDuration: Int,
    val weight: Float,
    val type: SetType,
    val position: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val restSeconds: Int? = null,
)

data class RoutineWithStats(
    @Embedded
    val routine: RoutineEntity,
    val exerciseCount: Int,
    val setCount: Int,
)

data class RoutineExerciseRow(
    val routineExerciseId: Int,
    val position: Int,
    @Embedded
    val exercise: ExerciseEntity,
)

fun RoutineEntity.toExternal(
    exerciseCount: Int = 0,
    setCount: Int = 0,
): Routine = Routine(
    id = id,
    planId = planId,
    name = name,
    position = position,
    exerciseCount = exerciseCount,
    setCount = setCount,
)

fun RoutineWithStats.toExternal(): Routine = routine.toExternal(
    exerciseCount = exerciseCount,
    setCount = setCount,
)

fun RoutineSetEntity.toExternal(): PlannedSet = PlannedSet(
    id = id,
    repsOrDuration = repsOrDuration,
    weight = weight,
    type = type,
    restSeconds = restSeconds,
)

fun SetDraft.toRoutineSet(routineExerciseId: Int, position: Int): RoutineSetEntity =
    RoutineSetEntity(
        routineExerciseId = routineExerciseId,
        repsOrDuration = repsOrDuration,
        weight = weight,
        type = type,
        position = position,
        restSeconds = restSeconds,
    )

fun List<RoutineExerciseRow>.withSets(sets: List<RoutineSetEntity>): List<RoutineExercise> {
    val setsByExercise = sets.groupBy { it.routineExerciseId }
    return map { row ->
        RoutineExercise(
            id = row.routineExerciseId,
            exercise = row.exercise.toExternal(),
            sets = setsByExercise[row.routineExerciseId].orEmpty().map { it.toExternal() },
        )
    }
}
