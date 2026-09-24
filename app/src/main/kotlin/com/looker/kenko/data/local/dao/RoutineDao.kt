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

package com.looker.kenko.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.looker.kenko.data.local.model.RoutineEntity
import com.looker.kenko.data.local.model.RoutineExerciseEntity
import com.looker.kenko.data.local.model.RoutineExerciseRow
import com.looker.kenko.data.local.model.RoutineSetEntity
import com.looker.kenko.data.local.model.RoutineWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query(
        """
        SELECT routines.*,
        (SELECT COUNT(*)
        FROM routine_exercises
        WHERE routine_exercises.routineId = routines.id) AS exerciseCount,
        (SELECT COUNT(*)
        FROM routine_sets
        INNER JOIN routine_exercises ON routine_exercises.id = routine_sets.routineExerciseId
        WHERE routine_exercises.routineId = routines.id) AS setCount
        FROM routines
        WHERE routines.planId = :planId
        ORDER BY routines.position ASC, routines.id ASC
        """,
    )
    fun routinesFlow(planId: Int): Flow<List<RoutineWithStats>>

    @Query(
        """
        SELECT routines.*,
        (SELECT COUNT(*)
        FROM routine_exercises
        WHERE routine_exercises.routineId = routines.id) AS exerciseCount,
        (SELECT COUNT(*)
        FROM routine_sets
        INNER JOIN routine_exercises ON routine_exercises.id = routine_sets.routineExerciseId
        WHERE routine_exercises.routineId = routines.id) AS setCount
        FROM routines
        WHERE routines.planId =
        (SELECT planId
        FROM plan_history
        WHERE `end` IS NULL
        AND start IS NOT NULL)
        ORDER BY routines.position ASC, routines.id ASC
        """,
    )
    fun currentRoutinesFlow(): Flow<List<RoutineWithStats>>

    @Query(
        """
        SELECT *
        FROM routines
        """,
    )
    fun allRoutinesFlow(): Flow<List<RoutineEntity>>

    @Query(
        """
        SELECT *
        FROM routines
        WHERE id = :id
        """,
    )
    suspend fun getRoutine(id: Int): RoutineEntity?

    @Query(
        """
        SELECT COALESCE(MAX(position), -1) + 1
        FROM routines
        WHERE planId = :planId
        """,
    )
    suspend fun nextRoutinePosition(planId: Int): Int

    @Insert
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Query("UPDATE routines SET name = :name WHERE id = :id")
    suspend fun renameRoutine(id: Int, name: String)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutine(id: Int)

    @Query(
        """
        SELECT routine_exercises.id AS routineExerciseId,
        routine_exercises.position AS position,
        exercises.*
        FROM routine_exercises
        INNER JOIN exercises ON exercises.id = routine_exercises.exerciseId
        WHERE routine_exercises.routineId = :routineId
        ORDER BY routine_exercises.position ASC, routine_exercises.id ASC
        """,
    )
    fun routineExercisesFlow(routineId: Int): Flow<List<RoutineExerciseRow>>

    @Query(
        """
        SELECT routine_exercises.id AS routineExerciseId,
        routine_exercises.position AS position,
        exercises.*
        FROM routine_exercises
        INNER JOIN exercises ON exercises.id = routine_exercises.exerciseId
        WHERE routine_exercises.routineId = :routineId
        ORDER BY routine_exercises.position ASC, routine_exercises.id ASC
        """,
    )
    suspend fun getRoutineExercises(routineId: Int): List<RoutineExerciseRow>

    @Query(
        """
        SELECT routine_sets.*
        FROM routine_sets
        INNER JOIN routine_exercises ON routine_exercises.id = routine_sets.routineExerciseId
        WHERE routine_exercises.routineId = :routineId
        ORDER BY routine_exercises.position ASC, routine_exercises.id ASC,
        routine_sets.position ASC, routine_sets.id ASC
        """,
    )
    fun routineSetsFlow(routineId: Int): Flow<List<RoutineSetEntity>>

    @Query(
        """
        SELECT routine_sets.*
        FROM routine_sets
        INNER JOIN routine_exercises ON routine_exercises.id = routine_sets.routineExerciseId
        WHERE routine_exercises.routineId = :routineId
        ORDER BY routine_exercises.position ASC, routine_exercises.id ASC,
        routine_sets.position ASC, routine_sets.id ASC
        """,
    )
    suspend fun getRoutineSets(routineId: Int): List<RoutineSetEntity>

    @Query(
        """
        SELECT COALESCE(MAX(position), -1) + 1
        FROM routine_exercises
        WHERE routineId = :routineId
        """,
    )
    suspend fun nextExercisePosition(routineId: Int): Int

    @Insert
    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity): Long

    @Query("DELETE FROM routine_exercises WHERE id = :id")
    suspend fun deleteRoutineExercise(id: Int)

    @Query(
        """
        SELECT COALESCE(MAX(position), -1) + 1
        FROM routine_sets
        WHERE routineExerciseId = :routineExerciseId
        """,
    )
    suspend fun nextSetPosition(routineExerciseId: Int): Int

    @Insert
    suspend fun insertRoutineSet(set: RoutineSetEntity): Long

    @Insert
    suspend fun insertRoutineSets(sets: List<RoutineSetEntity>)

    @Query(
        """
        UPDATE routine_sets
        SET reps = :reps, weight = :weight, type = :type
        WHERE id = :id
        """,
    )
    suspend fun updateRoutineSet(id: Int, reps: Int, weight: Float, type: String)

    @Query("DELETE FROM routine_sets WHERE id = :id")
    suspend fun deleteRoutineSet(id: Int)

    // Fills in routineExerciseId and position of the sets
    @Transaction
    suspend fun insertExerciseWithSets(
        routineId: Int,
        exerciseId: Int,
        sets: List<RoutineSetEntity>,
    ): Int {
        val routineExerciseId = insertRoutineExercise(
            RoutineExerciseEntity(
                routineId = routineId,
                exerciseId = exerciseId,
                position = nextExercisePosition(routineId),
            ),
        ).toInt()
        insertRoutineSets(
            sets.mapIndexed { index, set ->
                set.copy(routineExerciseId = routineExerciseId, position = index)
            },
        )
        return routineExerciseId
    }
}
