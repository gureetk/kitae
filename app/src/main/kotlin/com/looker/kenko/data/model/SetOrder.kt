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

package com.looker.kenko.data.model

// Sets that have to move, with their new slot. [sets] must be sorted by slot.
fun <T> warmupsFirstSlots(sets: List<T>, slot: (T) -> Int, isWarmup: (T) -> Boolean): List<Pair<T, Int>> {
    val slots = sets.map(slot)
    val wanted = sets.filter(isWarmup) + sets.filterNot(isWarmup)
    return wanted.mapIndexedNotNull { index, set ->
        (set to slots[index]).takeIf { slot(set) != slots[index] }
    }
}
