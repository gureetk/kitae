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

package com.looker.kenko.ui.exerciseDetail

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.looker.kenko.ui.theme.numbers

// Oldest value first. The highest and lowest values get a guide line and a label.
@Composable
fun ProgressChart(
    values: List<Float>,
    maxLabel: String,
    minLabel: String,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.tertiary
    val guideColor = MaterialTheme.colorScheme.outlineVariant
    val ringColor = MaterialTheme.colorScheme.surface
    val labelStyle = MaterialTheme.typography.labelMedium.numbers()
        .copy(color = MaterialTheme.colorScheme.outline)
    val measurer = rememberTextMeasurer()
    Canvas(modifier) {
        if (values.isEmpty()) return@Canvas
        val maxText = measurer.measure(maxLabel, style = labelStyle)
        val minText = measurer.measure(minLabel, style = labelStyle)
        val dot = 4.dp.toPx()
        val left = dot + 2.dp.toPx()
        val right = size.width - maxOf(maxText.size.width, minText.size.width) - 12.dp.toPx()
        val top = maxText.size.height / 2f + dot
        val bottom = size.height - minText.size.height / 2f - dot
        val min = values.min()
        val max = values.max()
        val span = max - min
        fun x(index: Int): Float =
            if (values.size == 1) (left + right) / 2f else left + (right - left) * index / values.lastIndex
        fun y(value: Float): Float =
            if (span == 0f) (top + bottom) / 2f else bottom - (bottom - top) * (value - min) / span

        val labelX = right + 8.dp.toPx()
        drawLine(guideColor, Offset(left, y(max)), Offset(right, y(max)), strokeWidth = 1.dp.toPx())
        drawText(maxText, topLeft = Offset(labelX, y(max) - maxText.size.height / 2f))
        if (span > 0f) {
            drawLine(guideColor, Offset(left, y(min)), Offset(right, y(min)), strokeWidth = 1.dp.toPx())
            drawText(minText, topLeft = Offset(labelX, y(min) - minText.size.height / 2f))
        }

        val line = Path()
        values.forEachIndexed { index, value ->
            if (index == 0) line.moveTo(x(index), y(value)) else line.lineTo(x(index), y(value))
        }
        val area = Path().apply {
            addPath(line)
            lineTo(x(values.lastIndex), bottom)
            lineTo(x(0), bottom)
            close()
        }
        drawPath(area, color = lineColor.copy(alpha = 0.1f))
        drawPath(
            line,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        values.forEachIndexed { index, value ->
            val center = Offset(x(index), y(value))
            drawCircle(ringColor, radius = dot + 2.dp.toPx(), center = center)
            drawCircle(lineColor, radius = dot, center = center)
        }
    }
}
