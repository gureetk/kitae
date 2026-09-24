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

package com.looker.kenko.data.repository

import com.looker.kenko.data.model.Labels.Difficulty
import com.looker.kenko.data.model.Labels.Equipment
import com.looker.kenko.data.model.Labels.Focus
import com.looker.kenko.data.model.Labels.Time
import com.looker.kenko.data.model.Plan
import com.looker.kenko.data.model.Routine
import com.looker.kenko.data.model.RoutineExercise
import com.looker.kenko.data.model.SetDraft
import kotlinx.coroutines.flow.Flow

interface PlanRepo {

    val plans: Flow<List<Plan>>

    val current: Flow<Plan?>

    val currentRoutines: Flow<List<Routine>>

    fun routines(planId: Int): Flow<List<Routine>>

    fun routineExercises(routineId: Int): Flow<List<RoutineExercise>>

    suspend fun plan(id: Int): Plan?

    suspend fun planNameExists(name: String): Boolean

    suspend fun hasExercises(planId: Int): Boolean

    suspend fun createPlan(
        name: String,
        description: String? = null,
        difficulty: Difficulty? = null,
        focus: Focus? = null,
        equipment: Equipment? = null,
        time: Time? = null,
    ): Int

    suspend fun updatePlan(plan: Plan)

    suspend fun setCurrent(id: Int)

    suspend fun deletePlan(id: Int)

    suspend fun deleteEmptyPlans()

    suspend fun createRoutine(planId: Int, name: String): Int

    suspend fun renameRoutine(id: Int, name: String)

    suspend fun deleteRoutine(id: Int)

    suspend fun addExercise(routineId: Int, exerciseId: Int, sets: List<SetDraft>)

    suspend fun removeExercise(routineExerciseId: Int)

    suspend fun addPlannedSet(routineExerciseId: Int, set: SetDraft)

    suspend fun updatePlannedSet(id: Int, set: SetDraft)

    suspend fun removePlannedSet(id: Int)
}
