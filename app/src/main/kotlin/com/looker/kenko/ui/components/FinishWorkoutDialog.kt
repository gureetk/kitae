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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.model.FinishMode

// With nothing done, removing from the plan would empty the day
@Composable
fun FinishWorkoutDialog(
    incompleteSets: Int,
    completedSets: Int,
    onFinish: (mode: FinishMode, addNewSets: Boolean) -> Unit,
    onDismiss: () -> Unit,
    newSets: Int = 0,
    dayName: String? = null,
    title: String = stringResource(R.string.label_finish_workout),
    message: String? = null,
) {
    val resources = LocalContext.current.resources
    val day = dayName.takeIf { newSets > 0 }
    var addNewSets by rememberSaveable { mutableStateOf(false) }
    val setsMessage = when {
        incompleteSets == 0 && day != null -> resources.getQuantityString(
            R.plurals.new_sets_question,
            newSets,
            newSets,
            day,
        )

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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (text.isNotEmpty()) Text(text = text)
                if (incompleteSets > 0 && day != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = addNewSets,
                                onValueChange = { addNewSets = it },
                                role = Role.Checkbox,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = addNewSets, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = resources.getQuantityString(
                                R.plurals.also_add_new_sets,
                                newSets,
                                newSets,
                                day,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when {
                    incompleteSets == 0 && day != null -> {
                        Button(
                            onClick = { onFinish(FinishMode.KeepSkipped, true) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(R.string.label_add_to_day, day))
                        }
                        OutlinedButton(
                            onClick = { onFinish(FinishMode.KeepSkipped, false) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(R.string.label_only_this_workout))
                        }
                    }

                    incompleteSets == 0 -> Button(
                        onClick = { onFinish(FinishMode.KeepSkipped, false) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.label_finish))
                    }

                    else -> {
                        Button(
                            onClick = { onFinish(FinishMode.KeepSkipped, addNewSets) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(R.string.label_keep_skipped))
                        }
                        val removeMode = if (completedSets == 0) FinishMode.Discard else FinishMode.RemoveFromPlan
                        OutlinedButton(
                            onClick = { onFinish(removeMode, addNewSets) },
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
