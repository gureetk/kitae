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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.looker.kenko.R
import com.looker.kenko.data.model.Exercise
import com.looker.kenko.data.model.ExerciseSession
import com.looker.kenko.data.model.SetDraft
import com.looker.kenko.data.model.settings.format
import com.looker.kenko.ui.components.BackButton
import com.looker.kenko.ui.components.EmptyPage
import com.looker.kenko.ui.components.LocalWeightUnit
import com.looker.kenko.ui.components.formatRest
import com.looker.kenko.ui.extensions.plus
import com.looker.kenko.ui.theme.KenkoIcons
import com.looker.kenko.ui.theme.numbers
import com.looker.kenko.utils.DateFormat
import com.looker.kenko.utils.formatDate

private const val MAX_POINTS = 12

@Composable
fun ExerciseDetail(
    viewModel: ExerciseDetailViewModel,
    onBackPress: () -> Unit,
    onEditClick: (id: Int) -> Unit,
    onSessionClick: (sessionId: Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ExerciseDetail(
        state = state,
        onBackPress = onBackPress,
        onEditClick = onEditClick,
        onSessionClick = onSessionClick,
        onReferenceClick = viewModel::openReference,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDetail(
    state: ExerciseDetailState,
    onBackPress: () -> Unit,
    onEditClick: (id: Int) -> Unit,
    onSessionClick: (sessionId: Int) -> Unit,
    onReferenceClick: (String) -> Unit,
) {
    val exercise = (state as? ExerciseDetailState.Success)?.exercise
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onClick = onBackPress) },
                title = {
                    Text(
                        text = exercise?.name.orEmpty(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    val reference = exercise?.reference
                    if (!reference.isNullOrBlank()) {
                        IconButton(onClick = { onReferenceClick(reference) }) {
                            Icon(
                                painter = KenkoIcons.Lightbulb,
                                contentDescription = stringResource(R.string.label_reference),
                            )
                        }
                    }
                    val id = exercise?.id
                    if (id != null) {
                        IconButton(onClick = { onEditClick(id) }) {
                            Icon(
                                painter = KenkoIcons.Rename,
                                contentDescription = stringResource(R.string.label_edit_exercise),
                            )
                        }
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        when (state) {
            ExerciseDetailState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            ExerciseDetailState.NotFound -> EmptyPage(
                text = stringResource(R.string.label_exercise_not_found),
                modifier = Modifier.padding(padding),
                hero = {},
            )

            is ExerciseDetailState.Success -> ExerciseDetailContent(
                state = state,
                contentPadding = padding,
                onSessionClick = onSessionClick,
            )
        }
    }
}

@Composable
private fun ExerciseDetailContent(
    state: ExerciseDetailState.Success,
    contentPadding: PaddingValues,
    onSessionClick: (sessionId: Int) -> Unit,
) {
    val exercise = state.exercise
    LazyColumn(
        contentPadding = contentPadding + PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        item {
            Text(
                text = stringResource(exercise.target.stringRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        item { SectionTitle(stringResource(R.string.label_how_to)) }
        item {
            val instructions = exercise.instructions
            if (instructions.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.label_no_instructions),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                )
            } else {
                Steps(instructions)
            }
        }
        item { SectionTitle(stringResource(R.string.label_rest)) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatRest(exercise.restSeconds ?: state.defaultRestSeconds),
                    style = MaterialTheme.typography.titleLarge.numbers(),
                )
                if (exercise.restSeconds == null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.label_rest_default),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
        }
        item { SectionTitle(stringResource(R.string.label_progress)) }
        if (state.history.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.label_no_progress),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        } else {
            if (state.history.size > 1) {
                item { Progress(exercise = exercise, history = state.history) }
            }
            items(state.history, key = { it.sessionId }) { session ->
                SessionRow(
                    exercise = exercise,
                    session = session,
                    onClick = { onSessionClick(session.sessionId) },
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
    )
}

// One numbered step per line
@Composable
private fun Steps(text: String) {
    val steps = remember(text) {
        text.lines()
            .map { it.trim().replace(Regex("^\\d+[.)]\\s*"), "") }
            .filter { it.isNotEmpty() }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        steps.forEachIndexed { index, step ->
            Row {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.titleMedium.numbers(),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.width(28.dp),
                )
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun Progress(
    exercise: Exercise,
    history: List<ExerciseSession>,
) {
    val recent = remember(history) { history.take(MAX_POINTS).reversed() }
    val byWeight = remember(recent) { recent.any { (it.heaviest?.weight ?: 0F) > 0F } }
    val values = remember(recent, byWeight) {
        recent.map { if (byWeight) it.heaviest?.weight ?: 0F else it.mostReps.toFloat() }
    }
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    when {
                        byWeight -> R.string.label_heaviest_set
                        exercise.isIsometric -> R.string.label_longest_hold
                        else -> R.string.label_most_reps
                    },
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1F),
            )
            Text(
                text = progressValue(values.last(), byWeight, exercise.isIsometric),
                style = MaterialTheme.typography.titleMedium.numbers(),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        ProgressChart(
            values = values,
            maxLabel = progressValue(values.max(), byWeight, exercise.isIsometric),
            minLabel = progressValue(values.min(), byWeight, exercise.isIsometric),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Text(
                text = formatDate(recent.first().date, DateFormat.SessionLabel),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.weight(1F),
            )
            Text(
                text = formatDate(recent.last().date, DateFormat.SessionLabel),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun progressValue(value: Float, byWeight: Boolean, isIsometric: Boolean): String = when {
    byWeight -> LocalWeightUnit.current.format(value)
    isIsometric -> stringResource(R.string.label_value_seconds, value.toInt())
    else -> pluralStringResource(R.plurals.count_reps, value.toInt(), value.toInt())
}

// 60 kg × 8, or just the reps when there is no weight
@Composable
private fun setText(set: SetDraft, isIsometric: Boolean): String {
    val amount = if (isIsometric) {
        stringResource(R.string.label_value_seconds, set.repsOrDuration)
    } else {
        pluralStringResource(R.plurals.count_reps, set.repsOrDuration, set.repsOrDuration)
    }
    if (set.weight <= 0F) return amount
    val weight = LocalWeightUnit.current.format(set.weight)
    return if (isIsometric) "$weight × $amount" else "$weight × ${set.repsOrDuration}"
}

@Composable
private fun SessionRow(
    exercise: Exercise,
    session: ExerciseSession,
    onClick: () -> Unit,
) {
    val title = remember(session.date, session.routineName) {
        listOfNotNull(formatDate(session.date, DateFormat.SessionLabel), session.routineName)
            .joinToString(" · ")
    }
    val sets = session.sets.map { setText(it, exercise.isIsometric) }.joinToString(", ")
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = sets,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
