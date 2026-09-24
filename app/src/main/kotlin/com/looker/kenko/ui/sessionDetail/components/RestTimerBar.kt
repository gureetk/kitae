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

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.timer.REST_ADJUST_SECONDS
import com.looker.kenko.data.timer.RestTimerState
import com.looker.kenko.data.timer.formatClock
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import com.looker.kenko.ui.theme.numbers

private val BarHeight = 56.dp

@Composable
fun RestTimerBar(
    state: RestTimerState,
    restSeconds: Int,
    showFinish: Boolean,
    onStart: () -> Unit,
    onAdjust: (seconds: Int) -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedContent(
            targetState = state,
            contentKey = { it is RestTimerState.Running },
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1F),
            label = "RestTimer",
        ) { timer ->
            Row {
                when {
                    timer is RestTimerState.Running -> RestCountdown(
                        state = timer,
                        onAdjust = onAdjust,
                        onSkip = onSkip,
                        modifier = Modifier.weight(1F),
                    )

                    restSeconds > 0 -> RestStartButton(seconds = restSeconds, onClick = onStart)
                }
            }
        }
        if (showFinish) {
            Button(
                onClick = onFinish,
                modifier = Modifier.height(BarHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) {
                Text(text = stringResource(R.string.label_finish))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = KenkoIcons.Done,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun RestStartButton(
    seconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(BarHeight),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Icon(
            painter = KenkoIcons.Timer,
            contentDescription = stringResource(R.string.label_start_rest),
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = formatClock(seconds * 1000L),
            style = MaterialTheme.typography.labelLarge.numbers(),
        )
    }
}

@Composable
private fun RestCountdown(
    state: RestTimerState.Running,
    onAdjust: (seconds: Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(state) {
        while (true) {
            withFrameMillis { now = SystemClock.elapsedRealtime() }
        }
    }
    // Recomposes once a second
    val remaining by remember(state) {
        derivedStateOf { formatClock(state.remainingMillis(now)) }
    }
    val fillColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.24F)
    Surface(
        modifier = modifier.height(BarHeight),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        color = fillColor,
                        size = size.copy(width = size.width * state.progress(now)),
                    )
                }
                .padding(start = 20.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = remaining,
                style = MaterialTheme.typography.titleLarge.numbers(),
            )
            Spacer(modifier = Modifier.weight(1F))
            val textColors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current)
            TextButton(
                onClick = { onAdjust(-REST_ADJUST_SECONDS) },
                colors = textColors,
            ) {
                Text(text = stringResource(R.string.label_remove_seconds, REST_ADJUST_SECONDS))
            }
            TextButton(
                onClick = { onAdjust(REST_ADJUST_SECONDS) },
                colors = textColors,
            ) {
                Text(text = stringResource(R.string.label_add_seconds, REST_ADJUST_SECONDS))
            }
            IconButton(onClick = onSkip) {
                Icon(
                    painter = KenkoIcons.Close,
                    contentDescription = stringResource(R.string.label_skip),
                )
            }
        }
    }
}

@Preview
@Composable
private fun RestTimerBarPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Surface {
            val now = SystemClock.elapsedRealtime()
            RestTimerBar(
                state = RestTimerState.Running(startedAt = now - 30_000, endsAt = now + 60_000),
                restSeconds = 90,
                showFinish = true,
                onStart = {},
                onAdjust = {},
                onSkip = {},
                onFinish = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
