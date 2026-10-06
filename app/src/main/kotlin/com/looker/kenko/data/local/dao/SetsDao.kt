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
import com.looker.kenko.data.local.model.ExerciseHistoryRow
import com.looker.kenko.data.local.model.SetEntity
import com.looker.kenko.data.local.model.SetType
import com.looker.kenko.data.model.warmupsFirstSlots
import kotlinx.coroutines.flow.Flow

@Dao
interface SetsDao {

    @Query(
        """
        SELECT *
        FROM sets
        WHERE sessionId = :sessionId
        ORDER BY `order`
        """,
    )
    fun setsBySessionId(sessionId: Int): Flow<List<SetEntity>>

    @Query(
        """
        SELECT sets.*
        FROM sets
        INNER JOIN sessions ON sets.sessionId = sessions.id
        WHERE sets.exerciseId = :exerciseId
        AND sets.isCompleted = 1
        ORDER BY sessions.date DESC, sessions.id DESC, sets.`order` DESC, sets.id DESC
        LIMIT 1
        """,
    )
    suspend fun getLastSetByExerciseId(exerciseId: Int): SetEntity?

    @Query(
        """
        SELECT *
        FROM sets
        WHERE exerciseId = :exerciseId
        AND isCompleted = 1
        AND sessionId =
        (SELECT sets.sessionId
        FROM sets
        INNER JOIN sessions ON sessions.id = sets.sessionId
        WHERE sets.exerciseId = :exerciseId
        AND sets.isCompleted = 1
        ORDER BY sessions.date DESC, sessions.id DESC
        LIMIT 1)
        ORDER BY `order` ASC, id ASC
        """,
    )
    suspend fun getLastSessionSetsByExerciseId(exerciseId: Int): List<SetEntity>

    @Query(
        """
        SELECT *
        FROM sets
        WHERE sessionId = :sessionId
        ORDER BY `order`
        """,
    )
    suspend fun getSetsBySessionId(sessionId: Int): List<SetEntity>

    @Query(
        """
        SELECT COALESCE(MAX(`order`), -1) + 1
        FROM sets
        WHERE sessionId = :sessionId
        """,
    )
    suspend fun nextOrder(sessionId: Int): Int

    @Query(
        """
        SELECT *
        FROM sets
        WHERE (:exerciseId IS NULL OR exerciseId = :exerciseId)
        AND isCompleted = 1
        AND sessionId IN (
            SELECT id
            FROM sessions
            WHERE (:planId IS NULL OR planId = :planId)
        )
        ORDER BY `order`
        """,
    )
    fun setsByExerciseIdPerPlan(exerciseId: Int? = null, planId: Int? = null): Flow<List<SetEntity>>

    @Query(
        """
        SELECT *
        FROM sets
        WHERE (:exerciseId IS NULL OR exerciseId = :exerciseId)
        AND isCompleted = 1
        AND sessionId IN (
            SELECT id
            FROM sessions
            WHERE (:planId IS NULL OR planId = :planId)
        )
        ORDER BY `order`
        """,
    )
    suspend fun getSetsByExerciseIdPerPlan(
        exerciseId: Int? = null,
        planId: Int? = null,
    ): List<SetEntity>

    @Query(
        """
        SELECT COUNT (*)
        FROM sets
        WHERE isCompleted = 1
        """,
    )
    fun totalSetCount(): Flow<Int>

    @Query(
        """
        SELECT sets.sessionId AS sessionId,
        sessions.date AS date,
        routines.name AS routineName,
        sets.reps AS reps,
        sets.weight AS weight,
        sets.type AS type
        FROM sets
        INNER JOIN sessions ON sessions.id = sets.sessionId
        LEFT JOIN routines ON routines.id = sessions.routineId
        WHERE sets.exerciseId = :exerciseId
        AND sets.isCompleted = 1
        ORDER BY sessions.date DESC, sessions.id DESC, sets.`order` ASC, sets.id ASC
        """,
    )
    fun exerciseHistory(exerciseId: Int): Flow<List<ExerciseHistoryRow>>

    @Insert
    suspend fun insert(set: SetEntity)

    @Query("UPDATE sets SET isCompleted = :isCompleted WHERE id = :setId")
    suspend fun setCompleted(setId: Int, isCompleted: Boolean)

    @Query(
        """
        UPDATE sets
        SET reps = :reps, weight = :weight, type = :type, restSeconds = :restSeconds
        WHERE id = :setId
        """,
    )
    suspend fun updateValues(setId: Int, reps: Int, weight: Float, type: String, restSeconds: Int?)

    @Query(
        """
        DELETE
        FROM sets
        WHERE id = :setId
        """,
    )
    suspend fun delete(setId: Int)

    @Query("SELECT * FROM sets WHERE id = :setId")
    suspend fun getSet(setId: Int): SetEntity?

    @Query(
        """
        SELECT *
        FROM sets
        WHERE sessionId = :sessionId
        AND exerciseId = :exerciseId
        ORDER BY `order` ASC, id ASC
        """,
    )
    suspend fun getExerciseSets(sessionId: Int, exerciseId: Int): List<SetEntity>

    @Query("UPDATE sets SET `order` = :order WHERE id = :setId")
    suspend fun updateOrder(setId: Int, order: Int)

    suspend fun groupWarmups(sessionId: Int, exerciseId: Int) {
        warmupsFirstSlots(getExerciseSets(sessionId, exerciseId), { it.order }, { it.type == SetType.Warmup })
            .forEach { (set, order) -> updateOrder(set.id, order) }
    }

    @Transaction
    suspend fun insertGrouped(set: SetEntity) {
        insert(set)
        groupWarmups(set.sessionId, set.exerciseId)
    }

    @Transaction
    suspend fun updateGrouped(setId: Int, reps: Int, weight: Float, type: String, restSeconds: Int?) {
        updateValues(setId, reps, weight, type, restSeconds)
        val set = getSet(setId) ?: return
        groupWarmups(set.sessionId, set.exerciseId)
    }
}
