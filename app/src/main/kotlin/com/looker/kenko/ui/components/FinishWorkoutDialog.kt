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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.model.FinishMode

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
    var chosenMode by rememberSaveable {
        mutableStateOf(if (incompleteSets == 0) FinishMode.KeepSkipped else null)
    }
    val mode = chosenMode
    val text = when {
        mode == null -> listOfNotNull(
            message,
            if (completedSets == 0) {
                stringResource(R.string.label_nothing_done)
            } else {
                resources.getQuantityString(R.plurals.finish_incomplete_sets, incompleteSets, incompleteSets)
            },
        )

        day != null -> listOfNotNull(
            message.takeIf { incompleteSets == 0 },
            resources.getQuantityString(R.plurals.new_sets_question, newSets, newSets, day),
        )

        else -> listOfNotNull(message)
    }.joinToString("\n\n")

    fun choose(choice: FinishMode) {
        if (day != null && choice != FinishMode.Discard) chosenMode = choice else onFinish(choice, false)
    }

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
                when {
                    mode == null -> {
                        Button(
                            onClick = { choose(FinishMode.KeepSkipped) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(R.string.label_keep_skipped))
                        }
                        // With nothing done, removing from the plan would empty the day
                        val removeMode = if (completedSets == 0) FinishMode.Discard else FinishMode.RemoveFromPlan
                        OutlinedButton(
                            onClick = { choose(removeMode) },
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

                    day != null -> {
                        Button(
                            onClick = { onFinish(mode, true) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(R.string.label_add_to_day, day))
                        }
                        OutlinedButton(
                            onClick = { onFinish(mode, false) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(text = stringResource(R.string.label_only_this_workout))
                        }
                    }

                    else -> Button(
                        onClick = { onFinish(mode, false) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = stringResource(R.string.label_finish))
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
