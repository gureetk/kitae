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

package com.looker.kenko.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.model.FinishMode

// With nothing done, removing from the plan would empty the day
@Composable
fun FinishWorkoutDialog(
    incompleteSets: Int,
    completedSets: Int,
    onFinish: (FinishMode) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.label_finish_workout),
    message: String? = null,
) {
    val resources = LocalContext.current.resources
    val setsMessage = when {
        incompleteSets == 0 -> null
        completedSets == 0 -> stringResource(R.string.label_nothing_done)
        else -> resources.getQuantityString(
            R.plurals.finish_incomplete_sets,
            incompleteSets,
            incompleteSets,
        )
    }
    val text = listOfNotNull(message, setsMessage).joinToString("\n\n")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            if (text.isNotEmpty()) Text(text = text)
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (incompleteSets == 0) {
                    Button(
                        onClick = { onFinish(FinishMode.KeepSkipped) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.label_finish))
                    }
                } else {
                    Button(
                        onClick = { onFinish(FinishMode.KeepSkipped) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.label_keep_skipped))
                    }
                    val removeMode = if (completedSets == 0) FinishMode.Discard else FinishMode.RemoveFromPlan
                    OutlinedButton(
                        onClick = { onFinish(removeMode) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text(
                            text = stringResource(
                                if (completedSets == 0) {
                                    R.string.label_discard_workout
                                } else {
                                    R.string.label_remove_from_plan
                                },
                            ),
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.label_cancel))
                }
            }
        },
    )
}
