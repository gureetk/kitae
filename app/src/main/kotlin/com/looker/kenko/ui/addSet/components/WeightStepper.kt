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

package com.looker.kenko.ui.addSet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.looker.kenko.data.model.settings.WeightUnit
import com.looker.kenko.data.model.settings.formatWeightValue
import com.looker.kenko.ui.components.LocalWeightUnit
import com.looker.kenko.ui.components.OnSurfaceVariantBorder
import com.looker.kenko.ui.theme.numbers
import com.looker.kenko.ui.theme.top
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

private val PillWidth = 32.dp
private const val INCREMENT_DELAY = 200L
private const val INCREMENT_DELAY_STEP = 20L
private const val MIN_INCREMENT_DELAY = 100L

enum class WeightStep {
    LargeDecrement,
    Decrement,
    Rest,
    Increment,
    LargeIncrement,
    ;

    fun amount(unit: WeightUnit): Float = when (this) {
        LargeDecrement -> -unit.largeStep
        Decrement -> -unit.smallStep
        Rest -> 0F
        Increment -> unit.smallStep
        LargeIncrement -> unit.largeStep
    }

    fun label(unit: WeightUnit): String {
        val amount = amount(unit)
        return when {
            amount > 0F -> "+${formatWeightValue(amount)}"
            amount < 0F -> "-${formatWeightValue(-amount)}"
            else -> "•"
        }
    }
}

@Composable
fun WeightStepper(
    onStep: (Float) -> Unit,
    modifier: Modifier = Modifier,
    unit: WeightUnit = LocalWeightUnit.current,
) {
    val state = remember { AnchoredDraggableState(initialValue = WeightStep.Rest) }
    val interactionSource = remember { MutableInteractionSource() }

    RepeatStepEffect(state, unit, onStep)
    SettleToRestEffect(state, interactionSource)

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .border(
                border = OnSurfaceVariantBorder,
                shape = MaterialTheme.shapes.small,
            )
            .weightStepAnchors(state)
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal,
                interactionSource = interactionSource,
            ),
    ) {
        WeightStepLabels(unit)
        DraggablePill(offset = { state.requireOffset().roundToInt() })
    }
}

@Composable
private fun RepeatStepEffect(
    state: AnchoredDraggableState<WeightStep>,
    unit: WeightUnit,
    onStep: (Float) -> Unit,
) {
    val currentOnStep by rememberUpdatedState(onStep)
    val currentUnit by rememberUpdatedState(unit)
    LaunchedEffect(Unit) {
        snapshotFlow { state.currentValue }.collectLatest { step ->
            var incrementTimer = INCREMENT_DELAY
            while (step != WeightStep.Rest) {
                delay(incrementTimer.milliseconds)
                if (incrementTimer > MIN_INCREMENT_DELAY) incrementTimer -= INCREMENT_DELAY_STEP
                currentOnStep(step.amount(currentUnit))
            }
        }
    }
}

@Composable
private fun SettleToRestEffect(
    state: AnchoredDraggableState<WeightStep>,
    interactionSource: MutableInteractionSource,
) {
    val isDragged by interactionSource.collectIsDraggedAsState()
    LaunchedEffect(isDragged) {
        if (!isDragged && state.anchors.size > 0) {
            state.animateTo(WeightStep.Rest)
        }
    }
}

@Composable
private fun Modifier.weightStepAnchors(state: AnchoredDraggableState<WeightStep>): Modifier {
    val density = LocalDensity.current
    return onSizeChanged { size ->
        val pillWidthPx = with(density) { PillWidth.toPx() }
        val cellWidth = size.width.toFloat() / WeightStep.entries.size
        state.updateAnchors(
            DraggableAnchors {
                WeightStep.entries.forEachIndexed { index, step ->
                    step at (index + 0.5F) * cellWidth - pillWidthPx / 2
                }
            },
        )
    }
}

@Composable
private fun WeightStepLabels(
    unit: WeightUnit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        WeightStep.entries.forEach { step ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight(),
            ) {
                Text(
                    text = step.label(unit),
                    style = MaterialTheme.typography.labelMedium.numbers(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DraggablePill(
    offset: () -> Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .offset { IntOffset(offset(), 0) }
            .padding(vertical = 16.dp)
            .width(PillWidth)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape,
            ),
    )
}
