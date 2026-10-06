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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.looker.kenko.R
import com.looker.kenko.ui.theme.numbers
import kotlinx.coroutines.delay

@Composable
fun WorkoutClock(
    startedAt: Long,
    modifier: Modifier = Modifier,
) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(startedAt) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000L - (now - startedAt).mod(1_000L))
        }
    }
    Text(
        text = formatElapsed(now - startedAt),
        style = MaterialTheme.typography.titleMedium.numbers(),
        modifier = modifier,
    )
}

// 5:09, or 1:05:09 past an hour
fun formatElapsed(millis: Long): String {
    val totalSeconds = millis.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = totalSeconds % 3_600L / 60L
    val seconds = (totalSeconds % 60L).toString().padStart(2, '0')
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:$seconds"
    } else {
        "$minutes:$seconds"
    }
}

@Composable
fun workoutLength(millis: Long): String {
    val minutes = ((millis + 30_000L) / 60_000L).coerceAtLeast(1L)
    return if (minutes < 60) {
        stringResource(R.string.label_minutes, minutes)
    } else {
        stringResource(R.string.label_hours_minutes, minutes / 60, minutes % 60)
    }
}
