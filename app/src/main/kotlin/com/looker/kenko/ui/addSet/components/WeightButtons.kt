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

package com.looker.kenko.ui.addSet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.looker.kenko.data.model.settings.WeightUnit
import com.looker.kenko.data.model.settings.formatWeightValue
import com.looker.kenko.ui.components.LocalWeightUnit
import com.looker.kenko.ui.theme.KenkoTheme
import com.looker.kenko.ui.theme.KenkoThemeConfig
import com.looker.kenko.ui.theme.KenkoThemePreviewParameter
import com.looker.kenko.ui.theme.numbers
import kotlin.math.abs

@Composable
fun WeightButtons(
    onStep: (Float) -> Unit,
    modifier: Modifier = Modifier,
    unit: WeightUnit = LocalWeightUnit.current,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(unit.smallStep, unit.largeStep).forEach { step ->
            Row(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StepButton(
                    step = -step,
                    onStep = onStep,
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                )
                StepButton(
                    step = step,
                    onStep = onStep,
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun StepButton(
    step: Float,
    onStep: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = { onStep(step) },
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Text(
            text = (if (step > 0) "+" else "-") + formatWeightValue(abs(step)),
            style = MaterialTheme.typography.labelLarge.numbers(),
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun WeightButtonsPreview(
    @PreviewParameter(KenkoThemePreviewParameter::class) config: KenkoThemeConfig,
) {
    KenkoTheme(colorSchemes = config.colorSchemes, theme = config.theme) {
        Surface {
            WeightButtons(
                onStep = {},
                modifier = Modifier
                    .width(180.dp)
                    .height(112.dp),
            )
        }
    }
}
