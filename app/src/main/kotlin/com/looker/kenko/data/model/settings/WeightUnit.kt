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

package com.looker.kenko.data.model.settings

import kotlin.math.abs
import kotlin.math.roundToLong

const val KG_PER_LB: Float = 0.45359237F

// Weights are always stored in kilograms
enum class WeightUnit(val symbol: String) {
    Kilograms("KG"),
    Pounds("LBS"),
    ;

    fun fromKg(kg: Float): Float = when (this) {
        Kilograms -> kg
        Pounds -> kg / KG_PER_LB
    }

    fun toKg(value: Float): Float = when (this) {
        Kilograms -> value
        Pounds -> value * KG_PER_LB
    }

    val defaultWeightKg: Float
        get() = when (this) {
            Kilograms -> 20F
            Pounds -> toKg(45F)
        }

    val smallStep: Float
        get() = 2.5F

    val largeStep: Float
        get() = 5F
}

// Two decimals at most, hides kg/lb conversion noise
fun formatWeightValue(value: Float): String {
    val hundredths = (value.coerceAtLeast(0F) * 100).roundToLong()
    val whole = hundredths / 100
    val fraction = abs(hundredths % 100)
    return if (fraction % 10 == 0L) {
        "$whole.${fraction / 10}"
    } else {
        "$whole.${fraction.toString().padStart(2, '0')}"
    }
}

fun WeightUnit.format(kg: Float): String = "${formatWeightValue(fromKg(kg))} $symbol"

fun WeightUnit.formatInput(kg: Float): String = formatWeightValue(fromKg(kg))

fun WeightUnit.parseToKg(text: CharSequence): Float? =
    text.toString().toFloatOrNull()?.takeIf { it >= 0F }?.let(::toKg)
