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
import androidx.room.PrimaryKey
import com.looker.kenko.data.model.Labels.Difficulty
import com.looker.kenko.data.model.Labels.Equipment
import com.looker.kenko.data.model.Labels.Focus
import com.looker.kenko.data.model.Labels.Time
import com.looker.kenko.data.model.Plan
import com.looker.kenko.data.model.PlanStat

@Entity(tableName = "plans")
data class PlanEntity(
    val name: String,
    @ColumnInfo(defaultValue = "NULL")
    val description: String?,
    @ColumnInfo(defaultValue = "NULL")
    val difficulty: Difficulty?,
    @ColumnInfo(defaultValue = "NULL")
    val focus: Focus?,
    @ColumnInfo(defaultValue = "NULL")
    val equipment: Equipment?,
    @ColumnInfo(defaultValue = "NULL")
    val time: Time?,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)

data class PlanWithStats(
    @Embedded
    val plan: PlanEntity,
    val exerciseCount: Int,
    val routineCount: Int,
)

fun PlanEntity.toExternal(isActive: Boolean, stat: PlanStat) = Plan(
    id = id,
    name = name,
    description = description,
    difficulty = difficulty,
    focus = focus,
    equipment = equipment,
    time = time,
    stat = stat,
    isActive = isActive,
)

fun PlanWithStats.toExternal(isActive: Boolean): Plan = plan.toExternal(
    isActive = isActive,
    stat = PlanStat(exercises = exerciseCount, days = routineCount),
)

fun Plan.toEntity(): PlanEntity = PlanEntity(
    id = id ?: 0,
    name = name,
    description = description,
    difficulty = difficulty,
    focus = focus,
    equipment = equipment,
    time = time,
)
