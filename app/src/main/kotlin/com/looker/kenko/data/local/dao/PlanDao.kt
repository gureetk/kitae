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
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import com.looker.kenko.data.local.model.PlanEntity
import com.looker.kenko.data.local.model.PlanWithStats
import com.looker.kenko.data.model.Labels
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query(
        """
        SELECT *
        FROM plans
        """,
    )
    fun plansFlow(): Flow<List<PlanEntity>>

    @Query(
        """
        SELECT plans.*,
        (SELECT COUNT(*)
        FROM routine_exercises
        INNER JOIN routines ON routines.id = routine_exercises.routineId
        WHERE routines.planId = plans.id) AS exerciseCount,
        (SELECT COUNT(*)
        FROM routines
        WHERE routines.planId = plans.id) AS routineCount
        FROM plans
        """,
    )
    fun plansWithStatsFlow(): Flow<List<PlanWithStats>>

    @Query(
        """
        SELECT plans.*,
        (SELECT COUNT(*)
        FROM routine_exercises
        INNER JOIN routines ON routines.id = routine_exercises.routineId
        WHERE routines.planId = plans.id) AS exerciseCount,
        (SELECT COUNT(*)
        FROM routines
        WHERE routines.planId = plans.id) AS routineCount
        FROM plans
        WHERE plans.id =
        (SELECT planId
        FROM plan_history
        WHERE `end` IS NULL
        AND start IS NOT NULL)
        """,
    )
    fun currentPlanWithStatsFlow(): Flow<PlanWithStats?>

    @Query(
        """
        SELECT plans.*,
        (SELECT COUNT(*)
        FROM routine_exercises
        INNER JOIN routines ON routines.id = routine_exercises.routineId
        WHERE routines.planId = plans.id) AS exerciseCount,
        (SELECT COUNT(*)
        FROM routines
        WHERE routines.planId = plans.id) AS routineCount
        FROM plans
        WHERE plans.id = :planId
        """,
    )
    suspend fun getPlanWithStats(planId: Int): PlanWithStats?

    @Query(
        """
        SELECT *
        FROM plans
        WHERE id = :planId
        """,
    )
    suspend fun getPlanById(planId: Int): PlanEntity?

    @Query(
        """
        SELECT EXISTS
        (SELECT *
        FROM plans
        WHERE name = :planName)
        """,
    )
    suspend fun exists(planName: String): Boolean

    @Query(
        """
        SELECT EXISTS
        (SELECT *
        FROM routine_exercises
        INNER JOIN routines ON routines.id = routine_exercises.routineId
        WHERE routines.planId = :planId)
        """,
    )
    suspend fun hasExercises(planId: Int): Boolean

    suspend fun searchPlans(
        query: String? = null,
        difficulty: Labels.Difficulty? = null,
        focus: Labels.Focus? = null,
        equipment: Labels.Equipment? = null,
        time: Labels.Time? = null,
    ): List<PlanEntity> {
        val args = mutableListOf<Any?>()
        val sql = buildString(256) {
            append("SELECT * FROM plans WHERE 1=1 ")
            if (!query.isNullOrBlank()) {
                append("AND name LIKE %?% OR description LIKE %?% ")
                args.add(query)
                args.add(query)
            }
            if (difficulty != null) {
                append("AND difficulty = ? ")
                args.add(difficulty)
            }
            if (focus != null) {
                append("AND focus = ? ")
                args.add(focus)
            }
            if (equipment != null) {
                append("AND equipment = ? ")
                args.add(equipment)
            }
            if (time != null) {
                append("AND time = ? ")
                args.add(time)
            }
        }
        return _rawSearchPlans(
            SimpleSQLiteQuery(
                query = sql,
                bindArgs = args.toTypedArray(),
            ),
        )
    }

    @RawQuery
    suspend fun _rawSearchPlans(query: SimpleSQLiteQuery): List<PlanEntity>

    @Upsert
    suspend fun upsertPlan(plan: PlanEntity): Long

    @Query("DELETE FROM plans WHERE id = :planId")
    suspend fun deletePlan(planId: Int)

    @Query(
        """
        DELETE FROM plans
        WHERE id NOT IN
        (SELECT DISTINCT routines.planId
        FROM routines
        INNER JOIN routine_exercises ON routine_exercises.routineId = routines.id)
        """,
    )
    suspend fun deleteEmptyPlans()
}
