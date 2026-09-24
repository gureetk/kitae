/*
 * Copyright (C) 2025 LooKeR & Contributors
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

package com.looker.kenko.ui.planEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.model.Routine
import com.looker.kenko.ui.components.DaySelectorChip
import com.looker.kenko.ui.theme.KenkoIcons

@Composable
fun PlanExercise(
    header: @Composable () -> Unit,
    items: LazyListScope.() -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        stickyHeader {
            header()
        }
        items()
    }
}

@Composable
fun PlanDaysHeader(
    routines: List<Routine>,
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onAddRoutine: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedId, routines.size) {
        val index = routines.indexOfFirst { it.id == selectedId }
        if (index >= 0) listState.animateScrollToItem(index)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = stringResource(R.string.heading_plan_days),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(routines, key = { it.id }) { routine ->
                DaySelectorChip(
                    selected = routine.id == selectedId,
                    onClick = { onSelect(routine.id) },
                ) {
                    Text(text = routine.name, maxLines = 1)
                }
            }
            item(key = "add_day") {
                DaySelectorChip(
                    selected = false,
                    onClick = onAddRoutine,
                ) {
                    Icon(
                        painter = KenkoIcons.Add,
                        contentDescription = stringResource(R.string.label_new_day),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun FirstDay(
    suggestions: List<String>,
    onCreate: (String) -> Unit,
    onCustomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.label_first_day),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.outline,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { name ->
                SuggestionChip(
                    onClick = { onCreate(name) },
                    label = { Text(text = name) },
                )
            }
            AssistChip(
                onClick = onCustomClick,
                label = { Text(text = stringResource(R.string.label_new_day)) },
                leadingIcon = {
                    Icon(
                        painter = KenkoIcons.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }
    }
}
