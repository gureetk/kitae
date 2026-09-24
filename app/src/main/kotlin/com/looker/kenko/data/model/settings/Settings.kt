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

package com.looker.kenko.data.model.settings

import kotlin.time.Instant

data class Settings(
    val isOnboardingDone: Boolean,
    val theme: Theme,
    val colorPalette: ColorPalettes,
    val lastSetTime: Instant?,
    val backupUri: String?,
    val backupInterval: BackupInterval,
    val lastBackupTime: Instant?,
    val weightUnit: WeightUnit = WeightUnit.Kilograms,
    val restTimerSeconds: Int = DEFAULT_REST_TIMER_SECONDS,
)

const val DEFAULT_REST_TIMER_SECONDS = 90

// Seconds, 0 is off
val RestTimerOptions: List<Int> = listOf(0, 30, 60, 90, 120, 180, 300)
