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

package com.looker.kenko.data.repository

import com.looker.kenko.data.model.ActiveSession
import com.looker.kenko.data.model.Session
import com.looker.kenko.data.model.Set
import com.looker.kenko.data.model.SetDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SessionRepo {

    val stream: Flow<List<Session>>

    val setsCount: Flow<Int>

    val sessionsCount: Flow<Int>

    val activeSession: Flow<ActiveSession?>

    val lastPerformedRoutineId: Flow<Int?>

    val hasCompletedSets: Flow<Boolean>

    fun session(id: Int): Flow<Session?>

    suspend fun startSession(routineId: Int): Int

    suspend fun createSession(
        date: LocalDate,
        planId: Int?,
        routineId: Int?,
        sets: List<Pair<Int, SetDraft>>,
        isCompleted: Boolean,
    ): Int

    suspend fun finishSession(id: Int)

    suspend fun addSet(sessionId: Int, exerciseId: Int, set: SetDraft, isCompleted: Boolean = false)

    suspend fun updateSet(setId: Int, set: SetDraft)

    suspend fun setCompleted(setId: Int, isCompleted: Boolean)

    suspend fun removeSet(setId: Int)

    suspend fun previousSessionId(sessionId: Int, routineId: Int?, date: LocalDate): Int?

    suspend fun getLastSetByExerciseId(exerciseId: Int): Set?

    suspend fun getLastSessionSets(exerciseId: Int): List<SetDraft>
}
