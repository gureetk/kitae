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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.looker.kenko.data.model.ActiveSession
import com.looker.kenko.data.model.Session
import com.looker.kenko.data.model.Set
import com.looker.kenko.utils.EpochDays
import kotlinx.datetime.LocalDate

data class SessionEntity(
    @Embedded
    val data: SessionDataEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId",
    )
    val sets: List<SetEntity>,
)

@Entity(
    "sessions",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class SessionDataEntity(
    val date: EpochDays,
    @ColumnInfo(index = true)
    val planId: Int?,
    @ColumnInfo(index = true)
    val routineId: Int? = null,
    @ColumnInfo(defaultValue = "1")
    val isFinished: Boolean = true,
    // Epoch milliseconds, null for workouts from before they were recorded
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class SessionSummaryRow(
    @Embedded
    val data: SessionDataEntity,
    val routineName: String?,
    val totalSets: Int,
    val completedSets: Int,
    val newSets: Int,
)

fun SessionSummaryRow.toActiveSession(): ActiveSession = ActiveSession(
    id = data.id,
    date = LocalDate.fromEpochDays(data.date.value),
    routineId = data.routineId,
    routineName = routineName,
    completedSets = completedSets,
    totalSets = totalSets,
    newSets = newSets,
)

fun Session.data(): SessionDataEntity = SessionDataEntity(
    date = EpochDays(date.toEpochDays().toInt()),
    planId = planId,
    routineId = routineId,
    isFinished = isFinished,
    startedAt = startedAt,
    finishedAt = finishedAt,
    id = id ?: 0,
)

fun Session.sets(): List<SetEntity> = sets.map { it.toEntity(id!!, sets.indexOf(it)) }

fun SessionEntity.toExternal(
    sets: List<Set>,
    routineName: String? = null,
): Session = Session(
    planId = data.planId,
    routineId = data.routineId,
    routineName = routineName,
    date = LocalDate.fromEpochDays(data.date.value),
    sets = sets,
    isFinished = data.isFinished,
    id = data.id,
    startedAt = data.startedAt,
    finishedAt = data.finishedAt,
)
