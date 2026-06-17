package com.locationjoystick.feature.group.impl

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.locationjoystick.core.designsystem.component.LjScaffold
import com.locationjoystick.core.model.GroupRole
import com.locationjoystick.core.model.GroupState

@Composable
fun GroupSyncRoute(
    onOpenDrawer: () -> Unit,
    viewModel: GroupSyncViewModel = hiltViewModel(),
) {
    val groupState by viewModel.groupState.collectAsStateWithLifecycle()
    val qrBitmap by viewModel.qrBitmap.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage!!)
            viewModel.clearError()
        }
    }

    GroupSyncScreen(
        groupState = groupState,
        qrBitmap = qrBitmap,
        snackbarHostState = snackbarHostState,
        onOpenDrawer = onOpenDrawer,
        onCreateGroup = viewModel::createGroup,
        onSetFollowerModeEnabled = viewModel::setFollowerModeEnabled,
        onSetSharingEnabled = viewModel::setSharingEnabled,
        onLeaveGroup = viewModel::leaveGroup,
        onRegenerateQr = viewModel::regenerateQr,
    )
}

@Composable
internal fun GroupSyncScreen(
    groupState: GroupState,
    qrBitmap: Bitmap?,
    snackbarHostState: SnackbarHostState,
    onOpenDrawer: () -> Unit,
    onCreateGroup: () -> Unit,
    onSetFollowerModeEnabled: (Boolean) -> Unit,
    onSetSharingEnabled: (Boolean) -> Unit,
    onLeaveGroup: () -> Unit,
    onRegenerateQr: () -> Unit,
) {
    LjScaffold(
        title = "Group Sync",
        onNavigationClick = onOpenDrawer,
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            when (groupState.role) {
                GroupRole.NONE -> {
                    NoGroupContent(onCreateGroup = onCreateGroup)
                }

                GroupRole.LEADER -> {
                    LeaderContent(
                        groupState = groupState,
                        qrBitmap = qrBitmap,
                        onSetSharingEnabled = onSetSharingEnabled,
                        onLeaveGroup = onLeaveGroup,
                        onRegenerateQr = onRegenerateQr,
                    )
                }

                GroupRole.FOLLOWER -> {
                    FollowerContent(
                        groupState = groupState,
                        onSetFollowerModeEnabled = onSetFollowerModeEnabled,
                        onLeaveGroup = onLeaveGroup,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NoGroupContent(onCreateGroup: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Rounded.Groups,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Group Sync",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Sync your spoofed location across multiple devices on the same Wi-Fi network. No account needed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You are not in a group.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FilledTonalButton(
            onClick = onCreateGroup,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create group — I'm the leader")
        }
        Text(
            text = "To join a group, scan the QR code shown on the leader's device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LeaderContent(
    groupState: GroupState,
    qrBitmap: Bitmap?,
    onSetSharingEnabled: (Boolean) -> Unit,
    onLeaveGroup: () -> Unit,
    onRegenerateQr: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Role: Leader",
            style = MaterialTheme.typography.titleMedium,
        )

        val shortId = groupState.groupId?.takeLast(8) ?: "—"
        Text(
            text = "Group: …$shortId",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (qrBitmap != null) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Group invite QR code",
                        modifier = Modifier.size(220.dp),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text(
                            text = " Scan to join",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onRegenerateQr) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Regenerate QR")
                        }
                    }
                }
            }
        }

        SwitchRow(
            label = "Sharing",
            description = "broadcasts your location to followers",
            checked = groupState.sharingEnabled,
            onCheckedChange = onSetSharingEnabled,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLeaveGroup,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Text("Leave group")
        }
    }
}

@Composable
private fun FollowerContent(
    groupState: GroupState,
    onSetFollowerModeEnabled: (Boolean) -> Unit,
    onLeaveGroup: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Role: Follower",
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = "Leader: ${groupState.leaderHost ?: "—"}:${groupState.leaderPort ?: "—"}",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SwitchRow(
            label = "Follow leader",
            description = "mirrors leader's location",
            checked = groupState.followerModeEnabled,
            onCheckedChange = onSetFollowerModeEnabled,
        )

        Text(
            text = "Follower mode resumes when you open the app after a reboot.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLeaveGroup,
            modifier = Modifier.fillMaxWidth(),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
        ) {
            Text("Leave group")
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
