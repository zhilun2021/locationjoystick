package com.locationjoystick.feature.settings.impl

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.locationjoystick.core.common.constants.AppConstants
import com.locationjoystick.core.designsystem.LjIcons
import com.locationjoystick.core.designsystem.component.LjCheckboxRow
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.model.AppFeature
import com.locationjoystick.core.model.FeatureSurface
import kotlin.math.roundToInt

@Composable
internal fun SettingsMenusSubScreen(
    uiState: SettingsUiState,
    isRooted: Boolean,
    onNavigateBack: () -> Unit,
    isSpoofing: Boolean,
    onToggleSpoofing: () -> Unit,
    onAction: (SettingsAction) -> Unit,
    bottomBar: @Composable () -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    LjScaffold(
        title = "Menus",
        isSpoofing = isSpoofing,
        onToggleSpoofing = onToggleSpoofing,
        onNavigationClick = onNavigateBack,
        navigationIcon = LjIcons.ArrowBack,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        actions = { SubScreenActions(uiState.isDirty, onAction) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(remember { ScrollState(0) })
                                .padding(16.dp),
                    ) {
                        AppFeaturesSection(uiState, isRooted, onAction)
                        Spacer(Modifier.height(24.dp))
                        TapToWalkSection(uiState, onAction)
                    }
                }
            }
        }
    }
}

@Composable
private fun TapToWalkSection(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
) {
    var showOverlayWarning by rememberSaveable { mutableStateOf(false) }

    Text("Tap to Walk", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(4.dp))
    Text(
        "Walk to a location by tapping it.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(8.dp))
    LjCheckboxRow(
        checked = uiState.floatingMapQuickWalk,
        onCheckedChange = { onAction(SettingsAction.SetFloatingMapQuickWalk(it)) },
        title = "Floating map: skip confirmation",
        description = "Tapping the floating map walks immediately, without showing the action panel.",
    )
    Spacer(Modifier.height(8.dp))
    LjCheckboxRow(
        checked = uiState.tapToWalkOverlayEnabled,
        onCheckedChange = { enabled ->
            if (enabled) showOverlayWarning = true else onAction(SettingsAction.SetTapToWalkOverlayEnabled(false))
        },
        title = "Screen tap-to-walk overlay",
        description = "Adds a crosshair button to the widget. Tap it to intercept your next screen touch and walk to that position.",
    )
    if (uiState.tapToWalkOverlayEnabled) {
        Spacer(Modifier.height(8.dp))
        Text(
            "Scale (meters per pixel) — zoom out in the game for better accuracy",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = uiState.tapToWalkScaleMpx.toString(),
            onValueChange = { raw ->
                raw.toIntOrNull()?.let { v ->
                    onAction(
                        SettingsAction.SetTapToWalkScaleMpx(
                            v.coerceIn(
                                AppConstants.TapToWalkConstants.MIN_SCALE_MPX,
                                AppConstants.TapToWalkConstants.MAX_SCALE_MPX,
                            ),
                        ),
                    )
                }
            },
            label = { Text("m/px") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
    }
    if (showOverlayWarning) {
        AlertDialog(
            onDismissRequest = { showOverlayWarning = false },
            title = { Text("Enable screen tap-to-walk?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Anti-cheat risk: a screen overlay that intercepts taps may increase detection chance. Use at your own risk.")
                    Text("Accuracy: walk targets are estimated from screen position and may not match the exact tapped location.")
                    Text("Tip: zoom out in the game for better accuracy — a larger area on screen means less positioning error per pixel.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showOverlayWarning = false
                    onAction(SettingsAction.SetTapToWalkOverlayEnabled(true))
                }) { Text("Enable anyway") }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayWarning = false }) { Text("Cancel") }
            },
        )
    }
}

private data class FeatureMeta(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val isRootGated: Boolean = false,
)

private fun featureMeta(feature: AppFeature): FeatureMeta =
    when (feature) {
        AppFeature.MAP_FLOATING -> {
            FeatureMeta("Map shortcut", "Opens a compact map view without switching to the main app.", LjIcons.LocationOn)
        }

        AppFeature.JOYSTICK_TOGGLE -> {
            FeatureMeta("Show/hide joystick", "Toggles the floating joystick overlay on or off.", LjIcons.Visibility)
        }

        AppFeature.JOYSTICK_LOCK -> {
            FeatureMeta(
                "Lock joystick",
                "Keeps the joystick moving in the last held direction after you release.",
                LjIcons.Lock,
            )
        }

        AppFeature.FAVORITES -> {
            FeatureMeta("Favorites", "Teleport or walk to a saved location.", LjIcons.Favorite)
        }

        AppFeature.ROUTES -> {
            FeatureMeta("Routes", "Lists saved routes and starts replay.", LjIcons.Route)
        }

        AppFeature.ROAMING -> {
            FeatureMeta("Roaming", "Configure and start random walking within a radius.", LjIcons.Explore)
        }

        AppFeature.SEARCH -> {
            FeatureMeta("Search", "Find and jump to a place by name.", LjIcons.Search)
        }

        AppFeature.SPEED_CYCLE -> {
            FeatureMeta("Speed cycle", "Cycles through Walk, Run, and Bike speed profiles with a single tap.", LjIcons.Speed)
        }

        AppFeature.ELEVATION_CONTROLS -> {
            FeatureMeta(
                "Elevation controls",
                "Shows a floating overlay to tilt the simulated sensor angle · requires root",
                LjIcons.Layers,
                isRootGated = true,
            )
        }
    }

private val FEATURE_ROW_HEIGHT = 64.dp
private val FEATURE_ROW_SPACING = 8.dp

@Composable
private fun AppFeaturesSection(
    uiState: SettingsUiState,
    isRooted: Boolean,
    onAction: (SettingsAction) -> Unit,
) {
    Text("App Features", style = MaterialTheme.typography.headlineSmall)
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        "Choose which quick-access features appear in the floating widget and on the map screen, " +
            "and drag to reorder them. Both surfaces share the same order by default.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth().padding(start = 48.dp), horizontalArrangement = Arrangement.End) {
        Text("Widget", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
        Text("Map", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(56.dp), textAlign = TextAlign.Center)
    }

    val order = uiState.featureOrder
    val rowHeightPx = with(LocalDensity.current) { (FEATURE_ROW_HEIGHT + FEATURE_ROW_SPACING).toPx() }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragDeltaY by remember { mutableStateOf(0f) }

    Column(verticalArrangement = Arrangement.spacedBy(FEATURE_ROW_SPACING)) {
        order.forEachIndexed { index, feature ->
            val isDragging = draggingIndex == index
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 1f else 0f)
                        .let { mod ->
                            if (isDragging) {
                                mod.graphicsLayerTranslationY(dragDeltaY)
                            } else {
                                mod
                            }
                        },
            ) {
                FeatureRow(
                    feature = feature,
                    isRooted = isRooted,
                    uiState = uiState,
                    onAction = onAction,
                    dragModifier =
                        Modifier.pointerInput(feature) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingIndex = index
                                    dragDeltaY = 0f
                                },
                                onDragEnd = {
                                    val targetIndex =
                                        (index + (dragDeltaY / rowHeightPx).roundToInt()).coerceIn(0, order.lastIndex)
                                    if (targetIndex != index) {
                                        val newOrder = order.toMutableList()
                                        val moved = newOrder.removeAt(index)
                                        newOrder.add(targetIndex, moved)
                                        onAction(SettingsAction.SetFeatureOrder(newOrder))
                                    }
                                    draggingIndex = null
                                    dragDeltaY = 0f
                                },
                                onDragCancel = {
                                    draggingIndex = null
                                    dragDeltaY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragDeltaY += dragAmount.y
                                },
                            )
                        },
                )
            }
        }
    }
}

private fun Modifier.graphicsLayerTranslationY(ty: Float): Modifier = this.then(Modifier.graphicsLayer { translationY = ty })

@Composable
private fun FeatureRow(
    feature: AppFeature,
    isRooted: Boolean,
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    dragModifier: Modifier,
) {
    val meta = featureMeta(feature)
    val rowEnabled = !meta.isRootGated || isRooted
    Row(
        modifier = Modifier.fillMaxWidth().height(FEATURE_ROW_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LjIcons.DragHandle,
            contentDescription = "Drag to reorder ${meta.label}",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = dragModifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = meta.icon,
            contentDescription = null,
            tint = if (rowEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(
                text = meta.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (rowEnabled) Color.Unspecified else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
            Text(
                text = meta.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (meta.isRootGated) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (FeatureSurface.WIDGET in feature.surfaces) {
            Checkbox(
                checked = feature in uiState.enabledWidgetFeatures,
                enabled = rowEnabled,
                modifier = Modifier.width(56.dp).semantics { contentDescription = "${meta.label} on widget" },
                onCheckedChange = { checked ->
                    val updated = uiState.enabledWidgetFeatures.toMutableSet()
                    if (checked) {
                        updated.add(feature)
                        if (meta.isRootGated) onAction(SettingsAction.RequestElevationAccess)
                    } else {
                        updated.remove(feature)
                    }
                    onAction(SettingsAction.SetWidgetFeatures(updated))
                },
            )
        } else {
            Checkbox(
                checked = false,
                enabled = false,
                modifier = Modifier.width(56.dp),
                onCheckedChange = {},
            )
        }
        if (FeatureSurface.MAP in feature.surfaces) {
            Checkbox(
                checked = feature in uiState.enabledMapFeatures,
                modifier = Modifier.width(56.dp).semantics { contentDescription = "${meta.label} on map" },
                onCheckedChange = { checked ->
                    val updated = uiState.enabledMapFeatures.toMutableSet()
                    if (checked) updated.add(feature) else updated.remove(feature)
                    onAction(SettingsAction.SetMapFeatures(updated))
                },
            )
        } else {
            Checkbox(
                checked = false,
                enabled = false,
                modifier = Modifier.width(56.dp),
                onCheckedChange = {},
            )
        }
    }
}
