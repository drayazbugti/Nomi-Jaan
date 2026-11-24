package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.GoogleSignInButton
import com.example.model.CloudRecord
import com.example.ui.components.CyberCyan
import com.example.ui.components.CyberPurple
import com.example.ui.components.NetOfflineRed
import com.example.ui.components.NetOnlineGreen
import com.example.ui.components.NetworkStatusPill
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.CloudBackendViewModel
import com.example.viewmodel.CloudRecordsUiState
import com.google.firebase.Timestamp

@Composable
fun CloudSignedOutScreen(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.card,
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudDone,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Cloud Sync & Backup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Log in with Google to sync your presets, studio drafts, and cloud records in real time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
                NetworkStatusPill(isOnline = isOnline)
                Spacer(modifier = Modifier.height(4.dp))
                GoogleSignInButton(
                    onAuthSuccess = { errorMessage = null },
                    onAuthError = { errorMessage = it }
                )
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = NetOfflineRed
                    )
                }
            }
        }
    }
}

@Composable
fun CloudBackendScreen(
    viewModel: CloudBackendViewModel,
    userEmail: String?,
    isOnline: Boolean,
    onRetryInternet: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val accentColor = if (colors.isDark) CyberCyan else colors.accent
    val bgBrush = if (colors.isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF0E1426), Color(0xFF05070D)))
    } else {
        Brush.verticalGradient(colors = listOf(colors.backgroundGlow, colors.background))
    }

    val recordsState by viewModel.recordsState.collectAsStateWithLifecycle()
    val actionStatus by viewModel.actionStatus.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var titleInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var payloadInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.card,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Brush.linearGradient(listOf(CyberCyan, CyberPurple))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "BE",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = Color(0xFF05070D)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "BUGTI EDITZ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.5.sp,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "Online Database & Backend Portal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = onSignOutClick,
                                modifier = Modifier.testTag("cloud_sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign out",
                                    tint = colors.textSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NetworkStatusPill(isOnline = isOnline)
                            Text(
                                text = userEmail ?: "Google Authenticated",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }

            // Online Database Connection Status Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.card,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Online Database Connection (Real-Time Backend)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isOnline) {
                                    "Backend Status: Cloud Firestore Connected & Live Syncing"
                                } else {
                                    "Backend Status: Waiting for Internet Connection"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }

            // Save Data to Backend Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.card,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Save Data to Backend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )

                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Title / Project Name (e.g. BUGTI EDITZ Pack)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardSecondary,
                                unfocusedContainerColor = colors.cardSecondary,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cloud_title_input")
                        )

                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text("Category / Tag (e.g. Preset, Asset, User Data)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardSecondary,
                                unfocusedContainerColor = colors.cardSecondary,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cloud_category_input")
                        )

                        OutlinedTextField(
                            value = payloadInput,
                            onValueChange = { payloadInput = it },
                            label = { Text("Backend Data / JSON / Description") },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colors.cardSecondary,
                                unfocusedContainerColor = colors.cardSecondary,
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cloud_payload_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (!isOnline) {
                                        onRetryInternet()
                                        return@Button
                                    }
                                    viewModel.saveRecord(titleInput, categoryInput, payloadInput) {
                                        titleInput = ""
                                        categoryInput = ""
                                        payloadInput = ""
                                    }
                                },
                                enabled = !isSaving,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = if (colors.isDark) Color(0xFF05070D) else Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("cloud_save_button")
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = if (colors.isDark) Color(0xFF05070D) else Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Save to Online DB", fontWeight = FontWeight.ExtraBold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    onRetryInternet()
                                    val count = (recordsState as? CloudRecordsUiState.Success)?.records?.size ?: 0
                                    viewModel.reportSyncNotice(count)
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("cloud_sync_button")
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync", color = colors.textPrimary)
                            }
                        }

                        actionStatus?.let { status ->
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (status.contains("Failed", ignoreCase = true)) NetOfflineRed else NetOnlineGreen,
                                modifier = Modifier.testTag("cloud_action_status")
                            )
                        }
                    }
                }
            }

            // Live Backend Records Card Header
            item {
                Text(
                    text = "Live Backend Records",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            when (val state = recordsState) {
                CloudRecordsUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = accentColor)
                        }
                    }
                }

                is CloudRecordsUiState.Error -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = NetOfflineRed.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, NetOfflineRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = NetOfflineRed,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }

                is CloudRecordsUiState.Success -> {
                    if (state.records.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.card,
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No cloud records yet. Save a preset or project record above to sync in real time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(state.records, key = { it.id }) { record ->
                            CloudRecordCard(
                                record = record,
                                onDeleteClick = { viewModel.deleteRecord(record.id) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun CloudRecordCard(
    record: CloudRecord,
    onDeleteClick: () -> Unit
) {
    val colors = StudioTheme.colors
    val accentColor = if (colors.isDark) CyberCyan else colors.accent
    val displayTime = record.updatedAt ?: record.createdAt ?: Timestamp.now()
    val relativeTime = DateUtils.getRelativeTimeSpanString(
        displayTime.toDate().time,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cloud_record_${record.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "[${record.category}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.payload,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ID: ${record.id.take(10)} • $relativeTime",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
            }

            Surface(
                onClick = onDeleteClick,
                shape = RoundedCornerShape(9.dp),
                color = NetOfflineRed.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, NetOfflineRed.copy(alpha = 0.35f)),
                modifier = Modifier.testTag("cloud_delete_${record.id}")
            ) {
                Text(
                    text = "Delete",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NetOfflineRed,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }
        }
    }
}
