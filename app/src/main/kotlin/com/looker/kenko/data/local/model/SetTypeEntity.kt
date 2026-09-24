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

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("set_type")
data class SetTypeEntity(
    @PrimaryKey
    val type: SetType,
    val modifier: Float,
)

// Stored by name, don't rename
enum class SetType(val ratingModifier: Float) {
    Standard(STANDARD_SET_RATING_MODIFIER),
    Drop(DROP_SET_RATING_MODIFIER),
    RestPause(REST_PAUSE_SET_RATING_MODIFIER),
    Warmup(WARMUP_SET_RATING_MODIFIER),
    Failure(FAILURE_SET_RATING_MODIFIER),
}

private const val STANDARD_SET_RATING_MODIFIER: Float = 1.0F
private const val DROP_SET_RATING_MODIFIER: Float = 1.35F
private const val REST_PAUSE_SET_RATING_MODIFIER: Float = 1.2F
private const val WARMUP_SET_RATING_MODIFIER: Float = 0F

// RIR 0 instead of the default 2: 1.20 / 1.04
private const val FAILURE_SET_RATING_MODIFIER: Float = 1.15F

// Performance joins on this table, every type needs a row
fun defaultSetTypes() = SetType.entries.map { SetTypeEntity(it, it.ratingModifier) }
