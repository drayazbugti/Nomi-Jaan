package com.example.ui.screens

import android.net.Uri
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.AppThemeMode
import com.example.model.ExportResolution
import com.example.model.SavedCreation
import com.example.model.StudioPreferences
import com.example.ui.theme.StudioTheme
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeScreen(
    isLoggedIn: Boolean,
    userDisplayName: String?,
    userEmail: String?,
    userPhotoUrl: String?,
    savedCreations: List<SavedCreation>,
    preferences: StudioPreferences,
    onGoogleLogInClick: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onSaveProfileName: (String) -> Unit,
    onPickProfilePhoto: (Uri) -> Unit,
    onClearProfilePhoto: () -> Unit,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onToggleAutoWhiteOnClose: (Boolean) -> Unit,
    onToggleAutoSaveDrafts: (Boolean) -> Unit,
    onToggleResolution: (ExportResolution) -> Unit,
    onToggleCloudAutoBackup: (Boolean) -> Unit,
    onClaimBonusCredits: () -> Unit,
    onOpenProSuite: () -> Unit,
    onOpenCloudSpace: () -> Unit,
    onOpenQualityEnhancer: () -> Unit,
    onCreateNewDraft: () -> Unit,
    onOpenCreationInEditor: (SavedCreation) -> Unit,
    onDeleteCreation: (String) -> Unit,
    onDeleteSelectedCreations: (Set<String>) -> Unit,
    onDuplicateCreation: (String) -> Unit,
    onRenameCreation: (String, String) -> Unit,
    onUploadDraftsToCloud: (List<SavedCreation>, (String) -> Unit) -> Unit,
    onStartNewProject: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    var showSettingsModal by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var isLoggingIn by remember { mutableStateOf(false) }
    var actionTargetDraft by remember { mutableStateOf<SavedCreation?>(null) }
    var renameTargetDraft by remember { mutableStateOf<SavedCreation?>(null) }

    var isSelectMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickProfilePhoto(uri)
            toastMessage = "Profile photo updated"
        }
    }

    val effectiveEmail = userEmail?.takeIf { it.isNotBlank() }
        ?: preferences.loggedInEmail.takeIf { isLoggedIn && it.isNotBlank() }
    val resolvedName = preferences.profileName.takeIf { it.isNotBlank() }
        ?: userDisplayName?.takeIf { it.isNotBlank() }
        ?: effectiveEmail?.substringBefore("@")
        ?: "Studio Creator"
    val savedAvatarFile = remember(preferences.profilePhotoPath) {
        preferences.profilePhotoPath
            .takeIf { it.isNotBlank() }
            ?.let(::File)
            ?.takeIf { it.exists() && it.length() > 0L }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2400L)
            toastMessage = null
        }
    }

    // Keep selectedIds valid when list changes
    LaunchedEffect(savedCreations) {
        val validIds = savedCreations.map { it.id }.toSet()
        selectedIds = selectedIds.intersect(validIds)
        if (savedCreations.isEmpty()) {
            isSelectMode = false
        }
    }

    val allSelected = savedCreations.isNotEmpty() && selectedIds.size == savedCreations.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(colors.backgroundGlow, colors.background),
                    center = Offset(850f, 120f),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. TOP HEADER BAR (Credits pill + New Draft + Settings Hexagon)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = {
                            onClaimBonusCredits()
                            toastMessage = "+10 Studio Credits added!"
                        },
                        shape = RoundedCornerShape(999.dp),
                        color = colors.card,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.testTag("me_credit_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(15.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFFD166), Color(0xFFF4A261))
                                        )
                                    )
                            )
                            Text(
                                text = preferences.credits.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onCreateNewDraft()
                                toastMessage = "New project draft added"
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("me_new_draft_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = "New Project Draft",
                                tint = colors.textPrimary
                            )
                        }

                        IconButton(
                            onClick = { showSettingsModal = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("me_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // 2. PROFILE ROW (Editable Photo, Name, and Google Log In)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clickable {
                                profilePhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("me_profile_avatar_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(colors.cardSecondary)
                                .border(1.5.dp, colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                savedAvatarFile != null -> {
                                    AsyncImage(
                                        model = savedAvatarFile,
                                        contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                isLoggedIn && !userPhotoUrl.isNullOrBlank() -> {
                                    AsyncImage(
                                        model = userPhotoUrl,
                                        contentDescription = "Google profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                else -> {
                                    val smileTint = colors.textMuted
                                    Canvas(modifier = Modifier.size(28.dp)) {
                                        val stroke = Stroke(width = 4.2f, cap = StrokeCap.Round)
                                        drawArc(
                                            color = smileTint,
                                            startAngle = 20f,
                                            sweepAngle = 140f,
                                            useCenter = false,
                                            topLeft = Offset(size.width * 0.22f, size.height * 0.35f),
                                            size = androidx.compose.ui.geometry.Size(size.width * 0.56f, size.height * 0.4f),
                                            style = stroke
                                        )
                                        drawCircle(
                                            color = smileTint,
                                            radius = 2.5f,
                                            center = Offset(size.width * 0.34f, size.height * 0.34f)
                                        )
                                        drawLine(
                                            color = smileTint,
                                            start = Offset(size.width * 0.58f, size.height * 0.30f),
                                            end = Offset(size.width * 0.67f, size.height * 0.38f),
                                            strokeWidth = 3.8f,
                                            cap = StrokeCap.Round
                                        )
                                        drawLine(
                                            color = smileTint,
                                            start = Offset(size.width * 0.67f, size.height * 0.38f),
                                            end = Offset(size.width * 0.76f, size.height * 0.30f),
                                            strokeWidth = 3.8f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                                .border(1.5.dp, colors.background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Change profile photo",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = resolvedName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .clickable { showEditProfileDialog = true }
                                    .testTag("me_profile_name_text")
                            )
                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("me_edit_profile_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit name and photo",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        val themeLabel = if (preferences.themeMode == AppThemeMode.WHITE) "White Theme" else "Black Theme"
                        val authSubtitle = if (isLoggedIn) {
                            effectiveEmail ?: "Google Connected"
                        } else {
                            "Guest • $themeLabel"
                        }
                        Text(
                            text = authSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("me_theme_badge_text")
                        )
                    }

                    if (!isLoggedIn) {
                        Button(
                            onClick = {
                                if (!isLoggingIn) {
                                    isLoggingIn = true
                                    onGoogleLogInClick(
                                        {
                                            isLoggingIn = false
                                            toastMessage = "Signed in with Google"
                                        },
                                        { err ->
                                            isLoggingIn = false
                                            if (err.isNotBlank()) toastMessage = err
                                        }
                                    )
                                }
                            },
                            enabled = !isLoggingIn,
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.pillBg,
                                contentColor = colors.pillText
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("me_login_btn")
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = colors.pillText,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Log In",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.accent.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.35f)),
                            modifier = Modifier.testTag("me_logged_in_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Google",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent
                                )
                            }
                        }
                    }
                }
            }

            // 3. VIP / PRO STUDIO GLASS CARD
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, colors.border),
                    shadowElevation = if (colors.isDark) 0.dp else 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(colors.glassStart, colors.glassEnd)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pro Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary
                            )
                            Surface(
                                onClick = onOpenProSuite,
                                shape = RoundedCornerShape(999.dp),
                                color = colors.pillBg,
                                modifier = Modifier.testTag("me_subscribe_btn")
                            ) {
                                Text(
                                    text = "Subscribe",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.pillText,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = userEmail?.let { "Signed in as $it • VIP Cloud Ready" }
                                ?: "Enjoy Cloud Processing, Batch & 4K UHD",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VipPerkItem(
                                badge = "☁",
                                label = "Cloud Processing",
                                modifier = Modifier.weight(1f)
                            )
                            VipPerkItem(
                                badge = "📁",
                                label = "Batch Processing",
                                modifier = Modifier.weight(1f)
                            )
                            VipPerkItem(
                                badge = "✦",
                                label = "Bonus Credits",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = colors.border)
                        Spacer(modifier = Modifier.height(9.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenProSuite() },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Makes every moment shine.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                            Text(
                                text = "Learn more >",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }

            // 4. TWO FEATURE CARDS (Cloud Space & Quality Enhancer)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onOpenCloudSpace,
                        shape = RoundedCornerShape(15.dp),
                        color = colors.card,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("me_cloud_space_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "☁", fontSize = 16.sp, color = colors.accent)
                            Text(
                                text = "Cloud Space",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        onClick = onOpenQualityEnhancer,
                        shape = RoundedCornerShape(15.dp),
                        color = colors.card,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("me_quality_enhancer_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.cardSecondary)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "HD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "Quality Enhancer",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 5. DRAFTS HEADER (WITH SELECT ALL & SELECTED X OF Y COUNT WHEN IN SELECT MODE)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = if (isSelectMode) {
                                "Selected ${selectedIds.size} of ${savedCreations.size}"
                            } else {
                                "Drafts ${savedCreations.size}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            modifier = Modifier.testTag(
                                if (isSelectMode) "me_selected_count_label" else "me_drafts_count_label"
                            )
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(colors.accent)
                        )
                    }

                    if (savedCreations.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSelectMode) {
                                Surface(
                                    onClick = {
                                        selectedIds = if (allSelected) {
                                            emptySet()
                                        } else {
                                            savedCreations.map { it.id }.toSet()
                                        }
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (allSelected) colors.accent.copy(alpha = 0.14f) else colors.card,
                                    border = BorderStroke(
                                        1.dp,
                                        if (allSelected) colors.accent else colors.border
                                    ),
                                    modifier = Modifier.testTag("me_select_all_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (allSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                            contentDescription = "Select All",
                                            tint = if (allSelected) colors.accent else colors.textPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (allSelected) "Deselect All" else "Select All",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (allSelected) colors.accent else colors.textPrimary
                                        )
                                    }
                                }
                            }

                            Surface(
                                onClick = {
                                    isSelectMode = !isSelectMode
                                    if (!isSelectMode) selectedIds = emptySet()
                                },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelectMode) colors.accent else colors.card,
                                border = BorderStroke(1.dp, if (isSelectMode) colors.accent else colors.border),
                                modifier = Modifier.testTag("me_select_mode_btn")
                            ) {
                                Text(
                                    text = if (isSelectMode) "Done" else "Select",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelectMode) Color.White else colors.textPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. DRAFTS LIST
            if (savedCreations.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.card,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(colors.cardSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    tint = colors.accent
                                )
                            }
                            Text(
                                text = "No drafts yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Tap the layers button above or start editing a photo or video to create a draft.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onStartNewProject,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.pillBg,
                                    contentColor = colors.pillText
                                ),
                                modifier = Modifier.testTag("me_start_editing_btn")
                            ) {
                                Text("Start Editing", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(savedCreations, key = { it.id }) { creation ->
                    val isSelected = creation.id in selectedIds
                    DraftProjectCard(
                        creation = creation,
                        isSelectMode = isSelectMode,
                        isSelected = isSelected,
                        onCardClick = {
                            if (isSelectMode) {
                                selectedIds = if (isSelected) {
                                    selectedIds - creation.id
                                } else {
                                    selectedIds + creation.id
                                }
                            } else {
                                onOpenCreationInEditor(creation)
                            }
                        },
                        onMoreClick = { actionTargetDraft = creation }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(if (isSelectMode) 88.dp else 16.dp)) }
        }

        // FLOATING BOTTOM BAR DURING SELECT MODE
        AnimatedVisibility(
            visible = isSelectMode && savedCreations.isNotEmpty(),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.card,
                border = BorderStroke(1.5.dp, colors.accent.copy(alpha = 0.28f)),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("me_floating_select_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val count = selectedIds.size
                            onDeleteSelectedCreations(selectedIds)
                            selectedIds = emptySet()
                            isSelectMode = false
                            toastMessage = "Deleted $count draft(s)"
                        },
                        enabled = selectedIds.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.danger,
                            contentColor = Color.White,
                            disabledContainerColor = colors.danger.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("me_bulk_delete_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Delete Selected",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = {
                            val chosen = savedCreations.filter { it.id in selectedIds }
                            if (chosen.isNotEmpty()) {
                                onUploadDraftsToCloud(chosen) { msg ->
                                    toastMessage = msg
                                    isSelectMode = false
                                    selectedIds = emptySet()
                                }
                            }
                        },
                        enabled = selectedIds.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = Color.White,
                            disabledContainerColor = colors.accent.copy(alpha = 0.35f),
                            disabledContentColor = Color.White.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("me_bulk_upload_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Upload Selected",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // FLOATING TOAST FEEDBACK
        AnimatedVisibility(
            visible = toastMessage != null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = colors.pillBg,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = toastMessage.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.pillText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

    // POPUP 1: SMALL SETTINGS POPUP
    if (showSettingsModal) {
        SettingsPopupDialog(
            isLoggedIn = isLoggedIn,
            preferences = preferences,
            onSetThemeMode = onSetThemeMode,
            onToggleAutoWhiteOnClose = onToggleAutoWhiteOnClose,
            onToggleAutoSaveDrafts = onToggleAutoSaveDrafts,
            onToggleResolution = onToggleResolution,
            onToggleCloudAutoBackup = onToggleCloudAutoBackup,
            onGoogleLogInClick = {
                showSettingsModal = false
                if (!isLoggingIn) {
                    isLoggingIn = true
                    onGoogleLogInClick(
                        {
                            isLoggingIn = false
                            toastMessage = "Signed in with Google"
                        },
                        { err ->
                            isLoggingIn = false
                            if (err.isNotBlank()) toastMessage = err
                        }
                    )
                }
            },
            onSignOutClick = {
                showSettingsModal = false
                onSignOutClick()
                toastMessage = "Signed out"
            },
            onDismiss = { showSettingsModal = false }
        )
    }

    // POPUP 1B: EDIT PROFILE DIALOG (Photo & Name)
    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = resolvedName,
            savedAvatarFile = savedAvatarFile,
            googlePhotoUrl = if (isLoggedIn) userPhotoUrl else null,
            onPickNewPhoto = {
                profilePhotoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = {
                onClearProfilePhoto()
                toastMessage = "Profile photo reset"
            },
            onSaveName = { newName ->
                onSaveProfileName(newName)
                toastMessage = "Profile saved"
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }

    // POPUP 2: PROJECT THREE-DOTS BOTTOM ACTION SHEET
    val sheetDraft = actionTargetDraft
    if (sheetDraft != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { actionTargetDraft = null },
            sheetState = sheetState,
            containerColor = colors.sheetBg,
            contentColor = colors.textPrimary,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActionSheetRowButton(
                    icon = Icons.Outlined.Delete,
                    label = "Delete",
                    tint = colors.danger,
                    testTag = "draft_sheet_delete",
                    onClick = {
                        actionTargetDraft = null
                        onDeleteCreation(sheetDraft.id)
                        toastMessage = "Draft deleted"
                    }
                )
                ActionSheetRowButton(
                    icon = Icons.Outlined.CloudUpload,
                    label = "Upload to Cloud",
                    tint = colors.textPrimary,
                    testTag = "draft_sheet_cloud",
                    onClick = {
                        actionTargetDraft = null
                        onUploadDraftsToCloud(listOf(sheetDraft)) { msg ->
                            toastMessage = msg
                        }
                    }
                )
                ActionSheetRowButton(
                    icon = Icons.Outlined.ContentCopy,
                    label = "Copy",
                    tint = colors.textPrimary,
                    testTag = "draft_sheet_copy",
                    onClick = {
                        actionTargetDraft = null
                        onDuplicateCreation(sheetDraft.id)
                        toastMessage = "Draft copied"
                    }
                )
                ActionSheetRowButton(
                    icon = Icons.Outlined.Edit,
                    label = "Rename",
                    tint = colors.textPrimary,
                    testTag = "draft_sheet_rename",
                    onClick = {
                        actionTargetDraft = null
                        renameTargetDraft = sheetDraft
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    onClick = { actionTargetDraft = null },
                    shape = RoundedCornerShape(999.dp),
                    color = colors.cancelBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("draft_sheet_cancel")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }
    }

    // RENAME DIALOG
    val renaming = renameTargetDraft
    if (renaming != null) {
        var nameInput by remember(renaming.id) { mutableStateOf(renaming.title) }
        AlertDialog(
            onDismissRequest = { renameTargetDraft = null },
            containerColor = colors.sheetBg,
            title = { Text("Rename Draft", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_draft_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = nameInput.trim()
                        if (trimmed.isNotEmpty()) {
                            onRenameCreation(renaming.id, trimmed)
                            toastMessage = "Renamed to $trimmed"
                        }
                        renameTargetDraft = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetDraft = null }) {
                    Text("Cancel", color = colors.textMuted)
                }
            }
        )
    }
}

@Composable
private fun VipPerkItem(
    badge: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(colors.cardSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = badge, fontSize = 11.sp, color = colors.textPrimary)
        }
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DraftProjectCard(
    creation: SavedCreation,
    isSelectMode: Boolean,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val colors = StudioTheme.colors
    val relTime = DateUtils.getRelativeTimeSpanString(
        creation.createdAtMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()

    Surface(
        onClick = onCardClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.card,
        border = BorderStroke(
            width = if (isSelected) 1.6.dp else 1.dp,
            color = if (isSelected) colors.accent else colors.border
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_creation_${creation.id}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isSelectMode) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "Selected" else "Not selected",
                    tint = if (isSelected) colors.accent else colors.textMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(82.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(colors.cardSecondary)
                    .border(1.dp, colors.border, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    creation.sampleResId != null -> {
                        Image(
                            painter = painterResource(id = creation.sampleResId),
                            contentDescription = creation.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    !creation.isVideo && creation.uriString.isNotBlank() -> {
                        AsyncImage(
                            model = Uri.parse(creation.uriString),
                            contentDescription = creation.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    creation.isVideo -> {
                        Icon(
                            imageVector = Icons.Filled.Videocam,
                            contentDescription = "Saved video",
                            tint = colors.accent
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Studio draft",
                            tint = colors.textMuted
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = creation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (creation.isUploadedToCloud) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.accent.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Cloud",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        }
                    }
                }

                Text(
                    text = relTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${creation.modeLabel} • ${creation.resolutionLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!isSelectMode) {
                        IconButton(
                            onClick = onMoreClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("draft_more_${creation.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreHoriz,
                                contentDescription = "More options for ${creation.title}",
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionSheetRowButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = tint
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    savedAvatarFile: File?,
    googlePhotoUrl: String?,
    onPickNewPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSaveName: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = StudioTheme.colors
    var nameInput by remember(initialName) { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.sheetBg,
        title = {
            Text(
                text = "Edit Profile",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(colors.cardSecondary)
                        .border(1.5.dp, colors.accent.copy(alpha = 0.4f), CircleShape)
                        .clickable { onPickNewPhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        savedAvatarFile != null -> {
                            AsyncImage(
                                model = savedAvatarFile,
                                contentDescription = "Current profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        !googlePhotoUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = googlePhotoUrl,
                                contentDescription = "Google profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = "Default profile icon",
                                tint = colors.textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPickNewPhoto,
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, colors.accent),
                        modifier = Modifier.testTag("edit_profile_change_photo_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Change Photo",
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (savedAvatarFile != null) {
                        TextButton(
                            onClick = onRemovePhoto,
                            modifier = Modifier.testTag("edit_profile_reset_photo_btn")
                        ) {
                            Text("Reset", color = colors.textMuted)
                        }
                    }
                }

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = nameInput.trim()
                    if (trimmed.isNotEmpty()) {
                        onSaveName(trimmed)
                    }
                },
                enabled = nameInput.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("edit_profile_save_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.textMuted)
            }
        }
    )
}

@Composable
private fun SettingsPopupDialog(
    isLoggedIn: Boolean,
    preferences: StudioPreferences,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onToggleAutoWhiteOnClose: (Boolean) -> Unit,
    onToggleAutoSaveDrafts: (Boolean) -> Unit,
    onToggleResolution: (ExportResolution) -> Unit,
    onToggleCloudAutoBackup: (Boolean) -> Unit,
    onGoogleLogInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = StudioTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = colors.sheetBg,
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = 20.dp,
            modifier = Modifier
                .widthIn(max = 350.dp)
                .fillMaxWidth()
                .testTag("settings_popup_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textPrimary
                    )
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(999.dp),
                        color = colors.card,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.testTag("settings_done_btn")
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }

                Text(
                    text = "App Theme",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isWhite = preferences.themeMode == AppThemeMode.WHITE
                    ThemeChoiceButton(
                        label = "☀ White Theme",
                        selected = isWhite,
                        onClick = { onSetThemeMode(AppThemeMode.WHITE) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_white_theme_btn")
                    )
                    ThemeChoiceButton(
                        label = "🌙 Black Theme",
                        selected = !isWhite,
                        onClick = { onSetThemeMode(AppThemeMode.BLACK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_black_theme_btn")
                    )
                }

                SettingCheckRow(
                    label = "ऐप बंद करते ही White Theme हो जाए",
                    checked = preferences.autoWhiteOnClose,
                    onCheckedChange = onToggleAutoWhiteOnClose,
                    testTag = "settings_auto_white_toggle"
                )
                SettingCheckRow(
                    label = "Auto-Save Settings & Drafts",
                    checked = preferences.autoSaveDrafts,
                    onCheckedChange = onToggleAutoSaveDrafts,
                    testTag = "settings_auto_save_toggle"
                )
                SettingCheckRow(
                    label = "HD / 4K Export Default",
                    checked = preferences.defaultResolution == ExportResolution.UHD_4K,
                    onCheckedChange = { checked ->
                        onToggleResolution(if (checked) ExportResolution.UHD_4K else ExportResolution.HD_1080)
                    },
                    testTag = "settings_hd_export_toggle"
                )
                SettingCheckRow(
                    label = "Cloud Auto-Backup",
                    checked = preferences.cloudAutoBackup,
                    onCheckedChange = onToggleCloudAutoBackup,
                    testTag = "settings_cloud_backup_toggle"
                )

                HorizontalDivider(color = colors.border)

                if (isLoggedIn) {
                    Surface(
                        onClick = onSignOutClick,
                        shape = RoundedCornerShape(12.dp),
                        color = colors.danger.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_sign_out_btn")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = colors.danger,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign Out",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.danger
                            )
                        }
                    }
                } else {
                    Surface(
                        onClick = onGoogleLogInClick,
                        shape = RoundedCornerShape(12.dp),
                        color = colors.accent.copy(alpha = 0.14f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_login_btn")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Log In with Google",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeChoiceButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = colors.cardSecondary,
        border = BorderStroke(1.5.dp, if (selected) colors.accent else colors.border),
        modifier = modifier.height(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) colors.accent else colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SettingCheckRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val colors = StudioTheme.colors
    HorizontalDivider(color = colors.border)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = colors.accent,
                checkmarkColor = Color.White,
                uncheckedColor = colors.textMuted
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
