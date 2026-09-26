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

package com.looker.kenko.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.looker.kenko.data.local.model.SessionDataEntity
import com.looker.kenko.data.local.model.SessionEntity
import com.looker.kenko.data.local.model.SessionSummaryRow
import com.looker.kenko.data.local.model.SetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(session: SessionDataEntity): Long

    @Insert
    suspend fun insertSets(sets: List<SetEntity>)

    // Fills in sessionId and order of the sets
    @Transaction
    suspend fun insertWithSets(session: SessionDataEntity, sets: List<SetEntity>): Int {
        val sessionId = insert(session).toInt()
        insertSets(
            sets.mapIndexed { index, set ->
                set.copy(sessionId = sessionId, order = index)
            },
        )
        return sessionId
    }

    @Query(
        """
        SELECT COUNT(*)
        FROM sessions
        WHERE EXISTS
        (SELECT 1
        FROM sets
        WHERE sets.sessionId = sessions.id
        AND sets.isCompleted = 1)
        """,
    )
    fun totalSessions(): Flow<Int>

    @Query(
        """
        SELECT COUNT(DISTINCT date)
        FROM sessions
        WHERE EXISTS
        (SELECT 1
        FROM sets
        WHERE sets.sessionId = sessions.id
        AND sets.isCompleted = 1)
        """,
    )
    fun daysTrained(): Flow<Int>

    @Transaction
    @Query(
        """
        SELECT *
        FROM sessions
        ORDER BY date DESC, id DESC
        """,
    )
    fun stream(): Flow<List<SessionEntity>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM sessions
        WHERE id = :id
        """,
    )
    fun session(id: Int): Flow<SessionEntity?>

    @Query(
        """
        SELECT sessions.*,
        routines.name AS routineName,
        (SELECT COUNT(*)
        FROM sets
        WHERE sets.sessionId = sessions.id) AS totalSets,
        (SELECT COUNT(*)
        FROM sets
        WHERE sets.sessionId = sessions.id
        AND sets.isCompleted = 1) AS completedSets,
        (SELECT COUNT(*)
        FROM sets
        WHERE sets.sessionId = sessions.id
        AND sets.isCompleted = 1
        AND sets.routineSetId IS NULL) AS newSets
        FROM sessions
        LEFT JOIN routines ON routines.id = sessions.routineId
        WHERE sessions.date >= :minDate
        AND sessions.isFinished = 0
        ORDER BY sessions.date DESC, sessions.id DESC
        LIMIT 1
        """,
    )
    fun activeSession(minDate: Int): Flow<SessionSummaryRow?>

    @Query(
        """
        SELECT routineId
        FROM sessions
        WHERE routineId IS NOT NULL
        AND EXISTS
        (SELECT 1
        FROM sets
        WHERE sets.sessionId = sessions.id
        AND sets.isCompleted = 1)
        ORDER BY date DESC, id DESC
        LIMIT 1
        """,
    )
    fun lastPerformedRoutineId(): Flow<Int?>

    @Query(
        """
        SELECT EXISTS
        (SELECT 1
        FROM sets
        WHERE isCompleted = 1)
        """,
    )
    fun hasCompletedSets(): Flow<Boolean>

    @Query(
        """
        SELECT id
        FROM sessions
        WHERE date = :date
        AND routineId = :routineId
        AND isFinished = 0
        ORDER BY id DESC
        LIMIT 1
        """,
    )
    suspend fun getSessionId(date: Int, routineId: Int): Int?

    // Same routine, or the same weekday for sessions without one
    @Query(
        """
        SELECT id
        FROM sessions
        WHERE id != :sessionId
        AND (
            (:routineId IS NOT NULL
            AND routineId = :routineId
            AND (date < :date OR (date = :date AND id < :sessionId)))
            OR
            (:routineId IS NULL
            AND routineId IS NULL
            AND date < :date
            AND (:date - date) % 7 = 0)
        )
        AND EXISTS
        (SELECT 1
        FROM sets
        WHERE sets.sessionId = sessions.id
        AND sets.isCompleted = 1)
        ORDER BY date DESC, id DESC
        LIMIT 1
        """,
    )
    suspend fun previousSessionId(sessionId: Int, routineId: Int?, date: Int): Int?

    @Query("DELETE FROM sets WHERE sessionId = :sessionId AND isCompleted = 0")
    suspend fun deleteIncompleteSets(sessionId: Int)

    @Query(
        """
        DELETE FROM sessions
        WHERE id = :sessionId
        AND NOT EXISTS
        (SELECT 1
        FROM sets
        WHERE sets.sessionId = :sessionId)
        """,
    )
    suspend fun deleteIfEmpty(sessionId: Int)

    @Query("UPDATE sessions SET isFinished = 1 WHERE id = :sessionId")
    suspend fun markFinished(sessionId: Int)

    @Query(
        """
        SELECT DISTINCT routine_sets.routineExerciseId
        FROM routine_sets
        WHERE routine_sets.id IN
        (SELECT routineSetId
        FROM sets
        WHERE sessionId = :sessionId
        AND isCompleted = 0)
        """,
    )
    suspend fun routineExercisesOfIncompleteSets(sessionId: Int): List<Int>

    @Query(
        """
        DELETE FROM routine_sets
        WHERE id IN
        (SELECT routineSetId
        FROM sets
        WHERE sessionId = :sessionId
        AND isCompleted = 0
        AND routineSetId IS NOT NULL)
        """,
    )
    suspend fun deleteIncompletePlannedSets(sessionId: Int)

    @Query(
        """
        DELETE FROM routine_exercises
        WHERE id IN (:routineExerciseIds)
        AND NOT EXISTS
        (SELECT 1
        FROM routine_sets
        WHERE routine_sets.routineExerciseId = routine_exercises.id)
        """,
    )
    suspend fun deleteEmptyRoutineExercises(routineExerciseIds: List<Int>)

    @Transaction
    suspend fun finish(sessionId: Int, keepIncompleteSets: Boolean, removeFromPlan: Boolean) {
        if (!keepIncompleteSets) {
            if (removeFromPlan) {
                val routineExerciseIds = routineExercisesOfIncompleteSets(sessionId)
                deleteIncompletePlannedSets(sessionId)
                deleteEmptyRoutineExercises(routineExerciseIds)
            }
            deleteIncompleteSets(sessionId)
        }
        markFinished(sessionId)
        deleteIfEmpty(sessionId)
    }
}
