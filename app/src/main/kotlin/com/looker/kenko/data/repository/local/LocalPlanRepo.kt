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

package com.looker.kenko.data.repository.local

import com.looker.kenko.data.local.dao.PlanDao
import com.looker.kenko.data.local.dao.PlanHistoryDao
import com.looker.kenko.data.local.dao.RoutineDao
import com.looker.kenko.data.local.model.PlanEntity
import com.looker.kenko.data.local.model.PlanHistoryEntity
import com.looker.kenko.data.local.model.RoutineEntity
import com.looker.kenko.data.local.model.toEntity
import com.looker.kenko.data.local.model.toExternal
import com.looker.kenko.data.local.model.toRoutineSet
import com.looker.kenko.data.local.model.withSets
import com.looker.kenko.data.model.Labels
import com.looker.kenko.data.model.Plan
import com.looker.kenko.data.model.Routine
import com.looker.kenko.data.model.RoutineExercise
import com.looker.kenko.data.model.SetDraft
import com.looker.kenko.data.repository.PlanRepo
import com.looker.kenko.utils.toLocalEpochDays
import com.looker.kenko.utils.today
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class LocalPlanRepo @Inject constructor(
    private val dao: PlanDao,
    private val routineDao: RoutineDao,
    private val historyDao: PlanHistoryDao,
) : PlanRepo {

    override val plans: Flow<List<Plan>> =
        combine(dao.plansWithStatsFlow(), historyDao.currentIdFlow()) { plans, current ->
            plans.map { it.toExternal(isActive = it.plan.id == current) }
        }

    override val current: Flow<Plan?> =
        dao.currentPlanWithStatsFlow().map { it?.toExternal(isActive = true) }

    override val currentRoutines: Flow<List<Routine>> =
        routineDao.currentRoutinesFlow().map { routines -> routines.map { it.toExternal() } }

    override fun routines(planId: Int): Flow<List<Routine>> =
        routineDao.routinesFlow(planId).map { routines -> routines.map { it.toExternal() } }

    override fun routineExercises(routineId: Int): Flow<List<RoutineExercise>> =
        combine(
            routineDao.routineExercisesFlow(routineId),
            routineDao.routineSetsFlow(routineId),
        ) { exercises, sets -> exercises.withSets(sets) }

    override suspend fun plan(id: Int): Plan? {
        val currentId = historyDao.getCurrentId()
        return dao.getPlanWithStats(id)?.toExternal(isActive = currentId == id)
    }

    override suspend fun planNameExists(name: String): Boolean =
        dao.exists(name)

    override suspend fun hasExercises(planId: Int): Boolean =
        dao.hasExercises(planId)

    override suspend fun createPlan(
        name: String,
        description: String?,
        difficulty: Labels.Difficulty?,
        focus: Labels.Focus?,
        equipment: Labels.Equipment?,
        time: Labels.Time?,
    ): Int = dao.upsertPlan(
        PlanEntity(
            name = name,
            description = description,
            difficulty = difficulty,
            focus = focus,
            equipment = equipment,
            time = time,
        ),
    ).toInt()

    override suspend fun updatePlan(plan: Plan) {
        dao.upsertPlan(plan.toEntity())
    }

    override suspend fun setCurrent(id: Int) {
        val date = today().toLocalEpochDays()
        val current = historyDao.getCurrent()
        if (current != null) {
            historyDao.upsert(current.copy(end = date))
        }
        historyDao.upsert(PlanHistoryEntity(planId = id, start = date))
    }

    override suspend fun deletePlan(id: Int) {
        dao.deletePlan(id)
    }

    override suspend fun deleteEmptyPlans() {
        dao.deleteEmptyPlans()
    }

    override suspend fun createRoutine(planId: Int, name: String): Int =
        routineDao.insertRoutine(
            RoutineEntity(
                planId = planId,
                name = name.trim(),
                position = routineDao.nextRoutinePosition(planId),
            ),
        ).toInt()

    override suspend fun renameRoutine(id: Int, name: String) {
        routineDao.renameRoutine(id, name.trim())
    }

    override suspend fun deleteRoutine(id: Int) {
        routineDao.deleteRoutine(id)
    }

    override suspend fun addExercise(routineId: Int, exerciseId: Int, sets: List<SetDraft>) {
        routineDao.insertExerciseWithSets(
            routineId = routineId,
            exerciseId = exerciseId,
            sets = sets.map { it.toRoutineSet(routineExerciseId = 0, position = 0) },
        )
    }

    override suspend fun removeExercise(routineExerciseId: Int) {
        routineDao.deleteRoutineExercise(routineExerciseId)
    }

    override suspend fun addPlannedSet(routineExerciseId: Int, set: SetDraft) {
        routineDao.insertRoutineSet(
            set.toRoutineSet(
                routineExerciseId = routineExerciseId,
                position = routineDao.nextSetPosition(routineExerciseId),
            ),
        )
    }

    override suspend fun updatePlannedSet(id: Int, set: SetDraft) {
        routineDao.updateRoutineSet(
            id = id,
            reps = set.repsOrDuration,
            weight = set.weight,
            type = set.type.name,
        )
    }

    override suspend fun removePlannedSet(id: Int) {
        routineDao.deleteRoutineSet(id)
    }
}
