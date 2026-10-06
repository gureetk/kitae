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

package com.looker.kenko.ui.addSet

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.OutlinedToggleButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.looker.kenko.R
import com.looker.kenko.data.local.model.SetType
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.SetDraft
import com.looker.kenko.data.model.repDurationStringRes
import com.looker.kenko.data.model.settings.formatInput
import com.looker.kenko.data.model.settings.formatWeightValue
import com.looker.kenko.data.model.settings.parseToKg
import com.looker.kenko.ui.addSet.components.ITEMS
import com.looker.kenko.ui.addSet.components.ItemSize
import com.looker.kenko.ui.addSet.components.VerticalSelector
import com.looker.kenko.ui.addSet.components.WeightButtons
import com.looker.kenko.ui.addSet.components.WeightTextField
import com.looker.kenko.ui.components.LocalWeightUnit
import com.looker.kenko.ui.components.RestPicker
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import com.looker.kenko.ui.theme.colorSchemes.JapanRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSetSheet(
    exercise: Exercise,
    initial: SetDraft,
    defaultRestSeconds: Int,
    onDismiss: () -> Unit,
    onDone: (SetDraft) -> Unit,
    isEdit: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        AddSet(
            exercise = exercise,
            initial = initial,
            defaultRestSeconds = defaultRestSeconds,
            isEdit = isEdit,
            onDone = { set ->
                onDone(set)
                scope.launch { state.hide() }.invokeOnCompletion {
                    if (!state.isVisible) onDismiss()
                }
            },
        )
    }
}

@Composable
fun AddSet(
    exercise: Exercise,
    initial: SetDraft,
    defaultRestSeconds: Int,
    onDone: (SetDraft) -> Unit,
    isEdit: Boolean = false,
) {
    val unit = LocalWeightUnit.current
    // In the display unit
    val weights = rememberTextFieldState(unit.formatInput(initial.weight))
    var reps by remember { mutableIntStateOf(initial.repsOrDuration) }
    var setType by remember { mutableStateOf(initial.type) }
    var restSeconds by remember { mutableStateOf(initial.restSeconds) }
    AddSetContent(
        title = stringResource(if (isEdit) R.string.label_edit_set_for else R.string.label_add_set_for),
        exerciseName = exercise.name,
        repsLabel = stringResource(exercise.repDurationStringRes),
        weights = weights,
        reps = reps,
        selectedSetType = setType,
        onSelectSetType = { setType = it },
        onAddWeight = { step ->
            val current = weights.text.toString().toFloatOrNull() ?: 0F
            weights.setTextAndPlaceCursorAtEnd(formatWeightValue(current + step))
        },
        onRepsChanged = { reps = it },
        restSeconds = restSeconds,
        defaultRestSeconds = defaultRestSeconds,
        onRestChange = { restSeconds = it },
        onDoneClick = {
            onDone(
                SetDraft(
                    repsOrDuration = reps,
                    weight = unit.parseToKg(weights.text) ?: initial.weight,
                    type = setType,
                    restSeconds = restSeconds,
                ),
            )
        },
    )
}

@Composable
private fun AddSetContent(
    title: String,
    exerciseName: String,
    repsLabel: String,
    weights: TextFieldState,
    reps: Int,
    selectedSetType: SetType,
    onSelectSetType: (SetType) -> Unit,
    onAddWeight: (Float) -> Unit,
    onRepsChanged: (Int) -> Unit,
    restSeconds: Int?,
    defaultRestSeconds: Int,
    onRestChange: (Int?) -> Unit,
    onDoneClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        AddSetHeader(
            modifier = Modifier.fillMaxWidth(),
            title = title,
            exerciseName = exerciseName,
            onClick = onDoneClick,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SetTypeSelector(
            modifier = Modifier.align(CenterHorizontally),
            selected = selectedSetType,
            onSelect = onSelectSetType,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                modifier = Modifier
                    .weight(1F)
                    .height(ItemSize * ITEMS),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WeightTextField(
                    state = weights,
                    modifier = Modifier
                        .weight(3F)
                        .fillMaxWidth(),
                )
                WeightButtons(
                    onStep = { step ->
                        onAddWeight(step)
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    },
                    modifier = Modifier
                        .weight(2F)
                        .fillMaxWidth(),
                )
            }
            VerticalSelector(
                label = repsLabel,
                value = reps,
                onChanged = onRepsChanged,
                // Done can be tapped before the dial settles
                onChange = {
                    onRepsChanged(it)
                    haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        RestPicker(
            restSeconds = restSeconds,
            defaultSeconds = defaultRestSeconds,
            onChange = onRestChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AddSetHeader(
    title: String,
    exerciseName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        FilledTonalIconButton(onClick = onClick) {
            Icon(
                painter = KenkoIcons.Done,
                contentDescription = "",
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetTypeSelector(
    selected: SetType,
    onSelect: (SetType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (SetTypeOptions.indexOf(selected) - 1).coerceAtLeast(0),
    )
    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween,
            Alignment.CenterHorizontally,
        ),
    ) {
        itemsIndexed(SetTypeOptions, key = { _, type -> type.name }) { index, type ->
            val interactionSource = remember { MutableInteractionSource() }
            val checked = selected == type
            OutlinedToggleButton(
                checked = checked,
                onCheckedChange = { onSelect(type) },
                interactionSource = interactionSource,
                modifier = Modifier.semantics { role = Role.RadioButton },
                border = if (checked) ButtonDefaults.outlinedButtonBorder(true) else null,
                colors = OutlinedToggleButtonDefaults.colors(
                    checkedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    checkedContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    SetTypeOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
            ) {
                SetTypeIndicator(
                    selected = checked,
                    type = type,
                    interactionSource = interactionSource,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(text = setTypeLabel(type))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetTypeIndicator(
    selected: Boolean,
    type: SetType,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    val morphAnimatable = remember { Animatable(0F) }
    val morph = remember { Morph(MaterialShapes.Circle, setTypeShape(type)) }
    val path = remember { Path() }

    LaunchedEffect(isPressed || selected) {
        launch {
            if (isPressed || selected) {
                morphAnimatable.animateTo(1F)
            } else {
                morphAnimatable.animateTo(0F)
            }
        }
    }

    val color = setTypeColor(type)
    Canvas(modifier) {
        drawPath(
            color = color,
            path = processPath(
                path = morph.toPath(progress = morphAnimatable.value, path = path),
                size = size,
                scaleFactor = 1F,
            ),
        )
    }
}

private fun processPath(
    path: Path,
    size: Size,
    scaleFactor: Float,
    scaleMatrix: Matrix = Matrix(),
): Path {
    scaleMatrix.reset()

    scaleMatrix.apply { scale(x = size.width * scaleFactor, y = size.height * scaleFactor) }

    // Scale to the desired size.
    path.transform(scaleMatrix)

    // Translate the path to align its center with the available size center.
    path.translate(size.center - path.getBounds().center)
    return path
}

private val SetTypeOptions = listOf(
    SetType.Warmup,
    SetType.Standard,
    SetType.Failure,
    SetType.Drop,
    SetType.RestPause,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun setTypeShape(type: SetType): RoundedPolygon = when (type) {
    SetType.Standard -> MaterialShapes.Ghostish
    SetType.Drop -> MaterialShapes.Arrow
    SetType.RestPause -> MaterialShapes.Bun
    SetType.Warmup -> MaterialShapes.Sunny
    SetType.Failure -> MaterialShapes.Burst
}

@Composable
fun setTypeColor(type: SetType): Color = when (type) {
    SetType.Standard -> MaterialTheme.colorScheme.primary
    SetType.Drop -> MaterialTheme.colorScheme.tertiary
    SetType.RestPause -> JapanRed
    SetType.Warmup -> MaterialTheme.colorScheme.secondary
    SetType.Failure -> MaterialTheme.colorScheme.error
}

fun setTypeLabel(type: SetType): String = when (type) {
    SetType.Standard -> "Standard"
    SetType.Drop -> "Drop"
    SetType.RestPause -> "Rest-Pause"
    SetType.Warmup -> "Warm-up"
    SetType.Failure -> "Failure"
}

@Preview
@Composable
private fun AddSetPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Surface {
            AddSetContent(
                title = stringResource(R.string.label_add_set_for),
                exerciseName = "Bench Press",
                repsLabel = stringResource(R.string.label_reps),
                weights = rememberTextFieldState("40.0"),
                reps = 12,
                selectedSetType = SetType.Standard,
                onSelectSetType = {},
                onAddWeight = {},
                onRepsChanged = {},
                restSeconds = null,
                defaultRestSeconds = 90,
                onRestChange = {},
                onDoneClick = {},
            )
        }
    }
}
