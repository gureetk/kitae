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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.timer.REST_ADJUST_SECONDS
import com.looker.kenko.data.timer.formatClock
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.numbers

// Null means the default is used
@Composable
fun RestPicker(
    restSeconds: Int?,
    defaultSeconds: Int,
    onChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val current = restSeconds ?: defaultSeconds
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = stringResource(R.string.label_rest),
                style = MaterialTheme.typography.titleMedium,
            )
            if (restSeconds == null) {
                Text(
                    text = stringResource(R.string.label_rest_default),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        if (restSeconds != null) {
            IconButton(onClick = { onChange(null) }) {
                Icon(
                    painter = KenkoIcons.Close,
                    contentDescription = stringResource(R.string.label_rest_use_default),
                )
            }
        }
        FilledTonalIconButton(
            onClick = { onChange((current - REST_ADJUST_SECONDS).coerceAtLeast(0)) },
            enabled = current > 0,
        ) {
            Icon(
                painter = KenkoIcons.Remove,
                contentDescription = stringResource(R.string.label_remove_seconds, REST_ADJUST_SECONDS),
            )
        }
        Text(
            text = formatRest(current),
            style = MaterialTheme.typography.titleLarge.numbers(),
            color = if (restSeconds == null) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 64.dp),
        )
        FilledTonalIconButton(onClick = { onChange(current + REST_ADJUST_SECONDS) }) {
            Icon(
                painter = KenkoIcons.Add,
                contentDescription = stringResource(R.string.label_add_seconds, REST_ADJUST_SECONDS),
            )
        }
    }
}

@Composable
fun formatRest(seconds: Int): String =
    if (seconds > 0) formatClock(seconds * 1_000L) else stringResource(R.string.label_rest_timer_off)
