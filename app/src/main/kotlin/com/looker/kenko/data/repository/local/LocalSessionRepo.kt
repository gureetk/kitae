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

package com.looker.kenko.data.repository.local

import com.looker.kenko.data.local.dao.ExerciseDao
import com.looker.kenko.data.local.dao.RoutineDao
import com.looker.kenko.data.local.dao.SessionDao
import com.looker.kenko.data.local.dao.SetsDao
import com.looker.kenko.data.local.model.SessionDataEntity
import com.looker.kenko.data.local.model.SessionEntity
import com.looker.kenko.data.local.model.toActiveSession
import com.looker.kenko.data.local.model.toExternal
import com.looker.kenko.data.local.model.toSetEntity
import com.looker.kenko.data.model.ActiveSession
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.FinishMode
import com.looker.kenko.data.model.Session
import com.looker.kenko.data.model.Set
import com.looker.kenko.data.model.SetDraft
import com.looker.kenko.data.model.toDraft
import com.looker.kenko.data.repository.SessionRepo
import com.looker.kenko.utils.EpochDays
import com.looker.kenko.utils.today
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class LocalSessionRepo @Inject constructor(
    private val dao: SessionDao,
    private val setsDao: SetsDao,
    private val routineDao: RoutineDao,
    private val exerciseDao: ExerciseDao,
) : SessionRepo {

    private val exercises: Flow<Map<Int, Exercise>> =
        exerciseDao.stream().map { exercises ->
            exercises.associate { it.id to it.toExternal() }
        }

    private val routineNames: Flow<Map<Int, String>> =
        routineDao.allRoutinesFlow().map { routines ->
            routines.associate { it.id to it.name }
        }

    override val stream: Flow<List<Session>> =
        combine(dao.stream(), exercises, routineNames) { sessions, exercises, names ->
            sessions.map { it.toSession(exercises, names) }
        }

    override val setsCount: Flow<Int> = setsDao.totalSetCount()

    override val sessionsCount: Flow<Int> = dao.totalSessions()

    override val daysTrained: Flow<Int> = dao.daysTrained()

    // Also workouts that went past midnight
    override val activeSession: Flow<ActiveSession?>
        get() = dao.activeSession(minDate = today().epochDay - 1).map { it?.toActiveSession() }

    override val lastPerformedRoutineId: Flow<Int?> = dao.lastPerformedRoutineId()

    override val hasCompletedSets: Flow<Boolean> = dao.hasCompletedSets()

    override fun session(id: Int): Flow<Session?> =
        combine(dao.session(id), exercises, routineNames) { session, exercises, names ->
            session?.toSession(exercises, names)
        }

    override suspend fun startSession(routineId: Int): Int {
        val date = today()
        dao.getSessionId(date.epochDay, routineId)?.let { return it }
        val routine = requireNotNull(routineDao.getRoutine(routineId)) { "Routine $routineId not found" }
        val exerciseIds = routineDao.getRoutineExercises(routineId)
            .associate { it.routineExerciseId to it.exercise.id }
        val sets = routineDao.getRoutineSets(routineId).mapNotNull { planned ->
            val exerciseId = exerciseIds[planned.routineExerciseId] ?: return@mapNotNull null
            SetDraft(
                repsOrDuration = planned.repsOrDuration,
                weight = planned.weight,
                type = planned.type,
                restSeconds = planned.restSeconds,
            ).toSetEntity(
                sessionId = 0,
                exerciseId = exerciseId,
                order = 0,
                isCompleted = false,
                routineSetId = planned.id,
            )
        }
        return dao.insertWithSets(
            session = SessionDataEntity(
                date = EpochDays(date.epochDay),
                planId = routine.planId,
                routineId = routineId,
                isFinished = false,
                startedAt = System.currentTimeMillis(),
            ),
            sets = sets,
        )
    }

    override suspend fun createSession(
        date: LocalDate,
        planId: Int?,
        routineId: Int?,
        sets: List<Pair<Int, SetDraft>>,
        isCompleted: Boolean,
    ): Int = dao.insertWithSets(
        session = SessionDataEntity(
            date = EpochDays(date.epochDay),
            planId = planId,
            routineId = routineId,
            isFinished = isCompleted,
        ),
        sets = sets.map { (exerciseId, set) ->
            set.toSetEntity(
                sessionId = 0,
                exerciseId = exerciseId,
                order = 0,
                isCompleted = isCompleted,
            )
        },
    )

    override suspend fun finishSession(id: Int, mode: FinishMode, addNewSets: Boolean) {
        routineDao.applyWorkout(sessionId = id, addNewSets = addNewSets)
        dao.finish(
            sessionId = id,
            keepIncompleteSets = mode == FinishMode.KeepSkipped,
            removeFromPlan = mode == FinishMode.RemoveFromPlan,
            finishedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun addSet(sessionId: Int, exerciseId: Int, set: SetDraft, isCompleted: Boolean) {
        setsDao.insertGrouped(
            set.toSetEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                order = setsDao.nextOrder(sessionId),
                isCompleted = isCompleted,
            ),
        )
    }

    override suspend fun updateSet(setId: Int, set: SetDraft) {
        setsDao.updateGrouped(
            setId = setId,
            reps = set.repsOrDuration,
            weight = set.weight,
            type = set.type.name,
            restSeconds = set.restSeconds,
        )
    }

    override suspend fun setCompleted(setId: Int, isCompleted: Boolean) {
        setsDao.setCompleted(setId, isCompleted)
    }

    override suspend fun removeSet(setId: Int) {
        setsDao.delete(setId)
    }

    override suspend fun previousSessionId(sessionId: Int, routineId: Int?, date: LocalDate): Int? =
        dao.previousSessionId(sessionId = sessionId, routineId = routineId, date = date.epochDay)

    override suspend fun getLastSetByExerciseId(exerciseId: Int): Set? {
        val exercise = exerciseDao.get(exerciseId) ?: return null
        return setsDao.getLastSetByExerciseId(exerciseId)?.toExternal(exercise.toExternal())
    }

    override suspend fun getLastSessionSets(exerciseId: Int): List<SetDraft> {
        val exercise = exerciseDao.get(exerciseId)?.toExternal() ?: return emptyList()
        return setsDao.getLastSessionSetsByExerciseId(exerciseId).map {
            it.toExternal(exercise).toDraft()
        }
    }

    private fun SessionEntity.toSession(
        exercises: Map<Int, Exercise>,
        routineNames: Map<Int, String>,
    ): Session = toExternal(
        sets = sets
            .sortedWith(compareBy({ it.order }, { it.id }))
            .mapNotNull { set -> exercises[set.exerciseId]?.let { set.toExternal(it) } },
        routineName = data.routineId?.let { routineNames[it] },
    )

    private val LocalDate.epochDay: Int
        get() = toEpochDays().toInt()
}
