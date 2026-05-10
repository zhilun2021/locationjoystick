package com.locationjoystick.core.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LjCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ElevatedCard(
        modifier = modifier,
        content = content,
    )
}
