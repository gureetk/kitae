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

package com.looker.kenko.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import com.looker.kenko.ui.addEditExercise.AddEditExercise
import com.looker.kenko.ui.addEditExercise.AddEditExerciseViewModel
import com.looker.kenko.ui.exerciseDetail.ExerciseDetail
import com.looker.kenko.ui.exerciseDetail.ExerciseDetailViewModel
import com.looker.kenko.ui.exercises.Exercises
import com.looker.kenko.ui.getStarted.GetStartedOld
import com.looker.kenko.ui.home.Home
import com.looker.kenko.ui.planEdit.PlanEdit
import com.looker.kenko.ui.planEdit.PlanEditViewModel
import com.looker.kenko.ui.plans.Plan
import com.looker.kenko.ui.profile.Profile
import com.looker.kenko.ui.sessionDetail.SessionDetailViewModel
import com.looker.kenko.ui.sessionDetail.SessionDetails
import com.looker.kenko.ui.sessions.Sessions
import com.looker.kenko.ui.settings.Settings

@Composable
fun KenkoNavHost(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryDecorators = listOf(
            rememberRoundedCornerNavEntryDecorator(),
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        onBack = { backStack.removeAt(backStack.lastIndex) },
        predictivePopTransitionSpec = { swipeEdge ->
            val slideDirection = if (swipeEdge == NavigationEvent.EDGE_RIGHT) {
                SlideDirection.Start
            } else {
                SlideDirection.End
            }
            fadeIn() togetherWith
                scaleOut(targetScale = 0.9f) +
                slideOutOfContainer(towards = slideDirection, targetOffset = { it / 10 }) +
                fadeOut()
        },
        entryProvider = { key ->
            NavEntry(key) {
                when (key) {
                    is Routes.GetStarted -> GetStartedOld(
                        isOnboardingDone = key.isOnboardingDone,
                        onNext = {
                            backStack.removeAll { it is Routes.GetStarted }
                            backStack.add(Routes.Home)
                        },
                    )

                    is Routes.Home -> Home(
                        onProfileClick = { backStack.add(Routes.Profile) },
                        onSelectPlanClick = { backStack.add(Routes.Plan) },
                        onAddExerciseClick = { backStack.add(Routes.AddEditExercise()) },
                        onExploreSessionsClick = { backStack.add(Routes.Session) },
                        onExploreExercisesClick = { backStack.add(Routes.Exercises) },
                        onOpenSession = { id -> backStack.add(Routes.SessionDetail(id)) },
                        onEditPlan = { planId, routineId ->
                            backStack.add(Routes.PlanEdit(planId, routineId))
                        },
                        viewModel = hiltViewModel(),
                    )

                    is Routes.Session -> Sessions(
                        onSessionClick = { id -> backStack.add(Routes.SessionDetail(id)) },
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        viewModel = hiltViewModel(),
                    )

                    is Routes.Plan -> Plan(
                        onPlanClick = { id -> backStack.add(Routes.PlanEdit(id)) },
                        onPlanSelected = {
                            val home = backStack.indexOfLast { it is Routes.Home }
                            if (home == -1) {
                                backStack.add(Routes.Home)
                                while (backStack.size > 1) backStack.removeAt(0)
                            } else {
                                while (backStack.lastIndex > home) backStack.removeAt(backStack.lastIndex)
                            }
                        },
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        viewModel = hiltViewModel(),
                    )

                    is Routes.Settings -> Settings(
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        viewModel = hiltViewModel(),
                    )

                    is Routes.Profile -> Profile(
                        onAddExerciseClick = { backStack.add(Routes.AddEditExercise()) },
                        onExercisesClick = { backStack.add(Routes.Exercises) },
                        onPlanClick = { backStack.add(Routes.Plan) },
                        onPlanEdit = { backStack.add(Routes.PlanEdit(it)) },
                        onSettingsClick = { backStack.add(Routes.Settings) },
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        viewModel = hiltViewModel(),
                    )

                    is Routes.Exercises -> Exercises(
                        onExerciseClick = { id ->
                            if (id != null) backStack.add(Routes.ExerciseDetail(id))
                        },
                        onCreateClick = { target ->
                            backStack.add(Routes.AddEditExercise(target = target?.name))
                        },
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        viewModel = hiltViewModel(),
                    )

                    is Routes.PlanEdit -> PlanEdit(
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        onAddNewExerciseClick = { name, target ->
                            backStack.add(
                                Routes.AddEditExercise(
                                    name = name,
                                    target = target?.name,
                                ),
                            )
                        },
                        viewModel = hiltViewModel<PlanEditViewModel, PlanEditViewModel.Factory> {
                            it.create(key)
                        },
                    )

                    is Routes.SessionDetail -> SessionDetails(
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        onHistoryClick = { id -> backStack.add(Routes.SessionDetail(id)) },
                        onEditPlanClick = { planId, routineId ->
                            backStack.add(Routes.PlanEdit(planId, routineId))
                        },
                        onExerciseClick = { id -> backStack.add(Routes.ExerciseDetail(id)) },
                        viewModel = hiltViewModel<SessionDetailViewModel, SessionDetailViewModel.Factory> {
                            it.create(key)
                        },
                    )

                    is Routes.ExerciseDetail -> ExerciseDetail(
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        onEditClick = { id -> backStack.add(Routes.AddEditExercise(id = id)) },
                        onSessionClick = { id -> backStack.add(Routes.SessionDetail(id)) },
                        viewModel = hiltViewModel<ExerciseDetailViewModel, ExerciseDetailViewModel.Factory> {
                            it.create(key)
                        },
                    )

                    is Routes.AddEditExercise -> AddEditExercise(
                        onDone = { backStack.removeAt(backStack.lastIndex) },
                        onBackPress = { backStack.removeAt(backStack.lastIndex) },
                        viewModel = hiltViewModel<AddEditExerciseViewModel, AddEditExerciseViewModel.Factory> {
                            it.create(key)
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun rememberRoundedCornerNavEntryDecorator(): NavEntryDecorator<NavKey> =
    remember {
        NavEntryDecorator { entry ->
            val scope = LocalNavAnimatedContentScope.current
            val cornerRadius = scope.transition.animateDp(
                transitionSpec = { spring(dampingRatio = 1.0f, stiffness = 1600f) },
                label = "cornerRadius",
            ) { state ->
                if (state == EnterExitState.PostExit) 28.dp else 0.dp
            }
            Box(
                modifier = Modifier.graphicsLayer {
                    val radiusPx = cornerRadius.value.toPx()
                    clip = radiusPx > 0f
                    shape = RoundedCornerShape(cornerRadius.value)
                },
            ) {
                entry.Content()
            }
        }
    }
