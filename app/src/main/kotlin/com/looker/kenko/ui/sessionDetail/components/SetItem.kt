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

package com.looker.kenko.ui.sessionDetail.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.model.settings.format
import com.looker.kenko.ui.components.LocalWeightUnit
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import com.looker.kenko.ui.theme.numbers

@Composable
fun SetItem(
    repsOrDuration: Int,
    weight: Float,
    isIsometric: Boolean,
    modifier: Modifier = Modifier,
    isCompleted: Boolean? = null,
    onCompletedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    isSkipped: Boolean = false,
    title: @Composable () -> Unit,
) {
    val unit = LocalWeightUnit.current
    val dim = if (isSkipped) Modifier.alpha(0.45F) else Modifier
    val isDone = isCompleted == true
    val showToggle = isCompleted != null && onCompletedChange != null
    val containerColor by animateColorAsState(
        targetValue = if (isDone) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        label = "SetContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isDone) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        label = "SetContentColor",
    )
    val indexColor by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
        label = "SetIndexColor",
    )
    Row(
        modifier = Modifier
            .heightIn(64.dp)
            .widthIn(240.dp, 420.dp)
            .background(MaterialTheme.colorScheme.surface)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides indexColor,
            LocalTextStyle provides MaterialTheme.typography.displayMedium.numbers(),
        ) {
            Box(modifier = dim.padding(horizontal = 16.dp)) {
                title()
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier
                    .weight(1F)
                    .then(dim)
                    .clip(MaterialTheme.shapes.large)
                    .background(containerColor)
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                    .padding(vertical = 16.dp, horizontal = if (showToggle) 16.dp else 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PerformedItem(
                    title = stringResource(if (isIsometric) R.string.label_duration else R.string.label_reps),
                    performance = "$repsOrDuration",
                )
                PerformedItem(
                    title = stringResource(R.string.label_weight),
                    performance = unit.format(weight),
                )
            }
        }
        if (isSkipped) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.label_skipped),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        if (showToggle) {
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconToggleButton(
                checked = isDone,
                onCheckedChange = { onCompletedChange?.invoke(it) },
            ) {
                Icon(
                    painter = KenkoIcons.Done,
                    contentDescription = stringResource(R.string.label_complete_set),
                )
            }
        }
    }
}

@Composable
private fun PerformedItem(
    title: String,
    performance: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.outline,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = titleColor,
        )
        Text(
            text = performance,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview
@Composable
private fun SetItemPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Surface {
            Column {
                SetItem(
                    repsOrDuration = 12,
                    weight = 40F,
                    isIsometric = false,
                    isCompleted = true,
                    onCompletedChange = {},
                ) {
                    Text(text = "01")
                }
                SetItem(
                    repsOrDuration = 8,
                    weight = 60F,
                    isIsometric = false,
                    isCompleted = false,
                    onCompletedChange = {},
                ) {
                    Text(text = "02")
                }
                SetItem(repsOrDuration = 45, weight = 0F, isIsometric = true) {
                    Text(text = "03")
                }
            }
        }
    }
}
