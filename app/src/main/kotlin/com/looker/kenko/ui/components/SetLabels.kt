/*
 * Copyright (C) 2026 Kitae Contributors
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

package com.looker.kenko.ui.components

import com.looker.kenko.data.local.model.SetType

fun setLabels(types: List<SetType>): List<String> {
    val counts = mutableMapOf<SetType, Int>()
    return types.map { type ->
        val number = (counts[type] ?: 0) + 1
        counts[type] = number
        when (type) {
            SetType.Standard -> number.toString().padStart(2, '0')
            SetType.Warmup -> "W$number"
            SetType.Failure -> "F$number"
            SetType.Drop -> "D$number"
            SetType.RestPause -> "R$number"
        }
    }
}
