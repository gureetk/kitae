package com.looker.kenko.ui.addSet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.looker.kenko.R
import com.looker.kenko.data.model.settings.WeightUnit
import com.looker.kenko.ui.addSet.FloatTransformation
import com.looker.kenko.ui.components.LocalWeightUnit
import com.looker.kenko.ui.components.OnSurfaceBorder

@Composable
fun WeightTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    unit: WeightUnit = LocalWeightUnit.current,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.small,
        ),
    ) {
        BasicTextField(
            state = state,
            inputTransformation = FloatTransformation,
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.displayLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.tertiary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        )
        Text(
            text = stringResource(R.string.label_weight),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .border(
                    border = OnSurfaceBorder,
                    shape = CircleShape,
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
        )
        Text(
            text = ".${unit.symbol.lowercase()}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}
