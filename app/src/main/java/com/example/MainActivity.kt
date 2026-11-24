package com.example

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.attemptAutoSignIn
import com.example.auth.authStateFlow
import com.example.auth.onGoogleSignInClicked
import com.example.auth.signOutUser
import com.example.data.CloudBackendRepository
import com.example.model.AppThemeMode
import com.example.model.EditorMainMode
import com.example.model.EnhanceMode
import com.example.model.EnhanceSegment
import com.example.model.HomeTab
import com.example.model.StudioEditState
import com.example.model.StudioToolId
import com.example.network.NetworkConnectivityMonitor
import com.example.ui.components.AllToolsSheet
import com.example.ui.components.BugtiSplashOverlay
import com.example.ui.components.ExportStatusDialog
import com.example.ui.components.MediaPickSheet
import com.example.ui.components.NoInternetPopupDialog
import com.example.ui.components.ProSuiteSheet
import com.example.ui.screens.CloudBackendScreen
import com.example.ui.screens.CloudSignedOutScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MeScreen
import com.example.ui.screens.QualityEnhancerScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.theme.BugtaEditzTheme
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.CloudBackendViewModel
import com.example.viewmodel.StudioViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay

private const val ROUTE_HUB = "hub"
private const val ROUTE_QUALITY_ENHANCER = "quality_enhancer"
private const val ROUTE_EDITOR = "editor"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            BugtiEditzRoot()
        }
    }
}

@Composable
fun BugtiEditzRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val studioViewModel: StudioViewModel = viewModel()
    val preferences by studioViewModel.preferences.collectAsStateWithLifecycle()
    val isDarkTheme = preferences.themeMode == AppThemeMode.BLACK

    // Update system bar icon tint when switching between White Theme & Black Theme
    LaunchedEffect(isDarkTheme) {
        val activity = context as? ComponentActivity ?: return@LaunchedEffect
        val style = if (isDarkTheme) {
            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        }
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    // Automatically restore White Theme on app exit/stop when enabled
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        val activity = context as? Activity
        if (activity?.isChangingConfigurations != true) {
            studioViewModel.onAppStopped()
        }
    }

    val networkMonitor = remember(context) { NetworkConnectivityMonitor(context) }
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle()
    DisposableEffect(networkMonitor) {
        onDispose { networkMonitor.unregister() }
    }

    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(2200L)
        showSplash = false
    }

    var offlinePopupDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(isOnline) {
        offlinePopupDismissed = isOnline
    }

    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = scope
        )
    }

    LaunchedEffect(currentUser) {
        studioViewModel.onAuthUserObserved(currentUser)
    }

    val retryConnectivity: () -> Unit = {
        val connected = networkMonitor.refreshConnectivity()
        offlinePopupDismissed = connected
    }

    BugtaEditzTheme(isDarkTheme = isDarkTheme) {
        val colors = StudioTheme.colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            BugtiEditzStudioApp(
                user = currentUser,
                isOnline = isOnline,
                onRetryInternet = retryConnectivity,
                onGoogleLogInClick = { onSuccess, onError ->
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = onSuccess,
                        onAuthError = onError,
                        scope = scope,
                        onAuthCancelled = { onError("") }
                    )
                },
                onSignOutClick = {
                    signOutUser(
                        credentialManager = credentialManager,
                        scope = scope
                    ) {
                        studioViewModel.onUserSignedOut()
                    }
                },
                viewModel = studioViewModel
            )

            if (!showSplash && !isOnline && !offlinePopupDismissed) {
                NoInternetPopupDialog(
                    onRetryClick = retryConnectivity,
                    onContinueOffline = { offlinePopupDismissed = true }
                )
            }

            BugtiSplashOverlay(visible = showSplash)
        }
    }
}

@Composable
fun BugtiEditzStudioApp(
    user: FirebaseUser?,
    isOnline: Boolean,
    onRetryInternet: () -> Unit,
    onGoogleLogInClick: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onSignOutClick: () -> Unit,
    viewModel: StudioViewModel
) {
    val cloudBackendViewModel: CloudBackendViewModel? = if (user != null) {
        viewModel(
            key = user.uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                    CloudBackendViewModel(CloudBackendRepository(app), user.uid)
                }
            }
        )
    } else {
        null
    }
    val context = LocalContext.current
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedCreations by viewModel.savedCreations.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val colors = StudioTheme.colors

    var showMediaPickSheet by remember { mutableStateOf(false) }
    var mediaSheetTitle by remember { mutableStateOf("Choose Photo or Video") }
    var mediaSheetSubtitle by remember { mutableStateOf("Select media from your gallery or start with a sample photo") }
    var showAllToolsSheet by remember { mutableStateOf(false) }
    var showProSuiteSheet by remember { mutableStateOf(false) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val mime = context.contentResolver.getType(uri).orEmpty()
            val isVideo = mime.startsWith("video/")
            viewModel.openMediaUri(uri, isVideo)
            if (navController.currentDestination?.route != ROUTE_EDITOR) {
                navController.navigate(ROUTE_EDITOR)
            }
        }
    }

    val batchPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 12)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.runBatchEnhance(
                uris = uris,
                mode = uiState.selectedQualityMode
            )
        }
    }

    fun openPickerSheet(
        title: String,
        subtitle: String,
        mainMode: EditorMainMode = EditorMainMode.EDIT,
        initialTool: StudioToolId? = StudioToolId.ENHANCE,
        presetState: StudioEditState? = null
    ) {
        viewModel.preparePickerContext(mainMode, initialTool, presetState)
        mediaSheetTitle = title
        mediaSheetSubtitle = subtitle
        showMediaPickSheet = true
    }

    fun handleStudioToolAction(tool: StudioToolId) {
        when (tool) {
            StudioToolId.QUALITY_ENHANCER -> {
                viewModel.selectQualitySegment(EnhanceSegment.GENERAL)
                viewModel.selectQualityMode(EnhanceMode.ULTRA_HD)
                navController.navigate(ROUTE_QUALITY_ENHANCER)
            }
            StudioToolId.SMART_ENHANCE -> {
                viewModel.selectQualitySegment(EnhanceSegment.GENERAL)
                viewModel.selectQualityMode(EnhanceMode.AI_UHD)
                navController.navigate(ROUTE_QUALITY_ENHANCER)
            }
            StudioToolId.SUPER_RESOLUTION -> {
                viewModel.selectQualitySegment(EnhanceSegment.GENERAL)
                viewModel.selectQualityMode(EnhanceMode.ULTRA_HD)
                navController.navigate(ROUTE_QUALITY_ENHANCER)
            }
            StudioToolId.BATCH_ENHANCE -> {
                batchPhotoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            StudioToolId.ALL_TOOLS -> {
                showAllToolsSheet = true
            }
            StudioToolId.FACE,
            StudioToolId.BODY,
            StudioToolId.AI_RETOUCH,
            StudioToolId.HAIR,
            StudioToolId.MAKEUP -> {
                openPickerSheet(
                    title = "${tool.label} Studio",
                    subtitle = "Pick a portrait or video clip to apply ${tool.label}",
                    mainMode = EditorMainMode.RETOUCH,
                    initialTool = tool
                )
            }
            else -> {
                openPickerSheet(
                    title = "${tool.label} Tool",
                    subtitle = "Pick a photo or video to open in ${tool.label}",
                    mainMode = EditorMainMode.EDIT,
                    initialTool = tool
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = ROUTE_HUB
        ) {
            composable(ROUTE_HUB) {
                Scaffold(
                    containerColor = colors.background,
                    bottomBar = {
                        StudioBottomTabBar(
                            currentTab = uiState.currentHomeTab,
                            onSelectTab = { viewModel.selectHomeTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(innerPadding)
                    ) {
                        when (uiState.currentHomeTab) {
                            HomeTab.HOME -> HomeScreen(
                                isOnline = isOnline,
                                onOpenSearch = { showAllToolsSheet = true },
                                onOpenProSuite = { showProSuiteSheet = true },
                                onHeroEditClick = {
                                    openPickerSheet(
                                        title = "Open in Studio Editor",
                                        subtitle = "Choose a photo or video to trim, color grade, and enhance",
                                        mainMode = EditorMainMode.EDIT,
                                        initialTool = StudioToolId.ENHANCE
                                    )
                                },
                                onHeroRetouchClick = {
                                    openPickerSheet(
                                        title = "Open in Retouch Studio",
                                        subtitle = "Choose a portrait photo or video for skin, face & glow retouch",
                                        mainMode = EditorMainMode.RETOUCH,
                                        initialTool = StudioToolId.FACE
                                    )
                                },
                                onToolClick = { handleStudioToolAction(it) },
                                onTryShowcaseSample = { resId, title ->
                                    viewModel.preparePickerContext(
                                        mainMode = EditorMainMode.EDIT,
                                        initialTool = StudioToolId.ENHANCE,
                                        presetState = StudioEditState(
                                            enhanceMode = EnhanceMode.ULTRA_HD,
                                            enhanceStrength = 0.85f
                                        )
                                    )
                                    viewModel.openSampleDrawable(resId, title)
                                    navController.navigate(ROUTE_EDITOR)
                                }
                            )

                            HomeTab.TEMPLATES -> TemplatesScreen(
                                onUseTemplateWithPicker = { template ->
                                    openPickerSheet(
                                        title = template.title,
                                        subtitle = template.subtitle,
                                        mainMode = EditorMainMode.EDIT,
                                        initialTool = StudioToolId.ENHANCE,
                                        presetState = template.editState
                                    )
                                },
                                onTryTemplateSample = { template ->
                                    viewModel.applyTemplateAndOpenSample(template)
                                    navController.navigate(ROUTE_EDITOR)
                                }
                            )

                            HomeTab.CLOUD -> {
                                if (user != null && cloudBackendViewModel != null) {
                                    CloudBackendScreen(
                                        viewModel = cloudBackendViewModel,
                                        userEmail = user.email,
                                        isOnline = isOnline,
                                        onRetryInternet = onRetryInternet,
                                        onSignOutClick = onSignOutClick
                                    )
                                } else {
                                    CloudSignedOutScreen(isOnline = isOnline)
                                }
                            }

                            HomeTab.ME -> MeScreen(
                                isLoggedIn = user != null,
                                userDisplayName = user?.displayName,
                                userEmail = user?.email,
                                userPhotoUrl = user?.photoUrl?.toString(),
                                savedCreations = savedCreations,
                                preferences = preferences,
                                onGoogleLogInClick = onGoogleLogInClick,
                                onSaveProfileName = { viewModel.saveProfileName(it) },
                                onPickProfilePhoto = { uri -> viewModel.saveProfilePhoto(uri) },
                                onClearProfilePhoto = { viewModel.clearProfilePhoto() },
                                onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                onToggleAutoWhiteOnClose = { enabled ->
                                    viewModel.updatePreferences { it.copy(autoWhiteOnClose = enabled) }
                                },
                                onToggleAutoSaveDrafts = { enabled ->
                                    viewModel.updatePreferences { it.copy(autoSaveDrafts = enabled) }
                                },
                                onToggleResolution = { res ->
                                    viewModel.updatePreferences { it.copy(defaultResolution = res) }
                                },
                                onToggleCloudAutoBackup = { enabled ->
                                    viewModel.updatePreferences { it.copy(cloudAutoBackup = enabled) }
                                },
                                onClaimBonusCredits = { viewModel.claimBonusCredits(10) },
                                onOpenProSuite = { showProSuiteSheet = true },
                                onOpenCloudSpace = { viewModel.selectHomeTab(HomeTab.CLOUD) },
                                onOpenQualityEnhancer = {
                                    viewModel.selectQualitySegment(EnhanceSegment.GENERAL)
                                    viewModel.selectQualityMode(EnhanceMode.ULTRA_HD)
                                    navController.navigate(ROUTE_QUALITY_ENHANCER)
                                },
                                onCreateNewDraft = { viewModel.createQuickDraft() },
                                onOpenCreationInEditor = { creation ->
                                    viewModel.preparePickerContext(
                                        mainMode = EditorMainMode.EDIT,
                                        initialTool = StudioToolId.ENHANCE
                                    )
                                    if (creation.sampleResId != null) {
                                        viewModel.openSampleDrawable(creation.sampleResId, creation.title)
                                    } else if (creation.uriString.isNotBlank()) {
                                        viewModel.openMediaUri(Uri.parse(creation.uriString), creation.isVideo)
                                    } else {
                                        viewModel.openSampleDrawable(R.drawable.img_demo_skyline, creation.title)
                                    }
                                    navController.navigate(ROUTE_EDITOR)
                                },
                                onDeleteCreation = { viewModel.removeSavedCreation(it) },
                                onDeleteSelectedCreations = { viewModel.removeSelectedCreations(it) },
                                onDuplicateCreation = { viewModel.duplicateCreation(it) },
                                onRenameCreation = { id, title -> viewModel.renameCreation(id, title) },
                                onUploadDraftsToCloud = { drafts, onDone ->
                                    if (cloudBackendViewModel == null) {
                                        onDone("Please Log In with Google first to upload")
                                    } else if (!isOnline) {
                                        onRetryInternet()
                                        onDone("Offline — check your internet connection")
                                    } else {
                                        cloudBackendViewModel.uploadDrafts(drafts) { uploadedIds, message ->
                                            viewModel.markCreationsUploadedToCloud(uploadedIds)
                                            onDone(message)
                                        }
                                    }
                                },
                                onStartNewProject = {
                                    openPickerSheet(
                                        title = "Start New Project",
                                        subtitle = "Pick a photo or video to enhance & save",
                                        mainMode = EditorMainMode.EDIT,
                                        initialTool = StudioToolId.ENHANCE
                                    )
                                },
                                onSignOutClick = onSignOutClick
                            )
                        }
                    }
                }
            }

            composable(ROUTE_QUALITY_ENHANCER) {
                QualityEnhancerScreen(
                    segment = uiState.qualitySegment,
                    selectedMode = uiState.selectedQualityMode,
                    splitPosition = uiState.demoSplitPosition,
                    isOnline = isOnline,
                    onBackClick = { navController.popBackStack() },
                    onSelectSegment = { viewModel.selectQualitySegment(it) },
                    onSelectMode = { viewModel.selectQualityMode(it) },
                    onSplitChange = { viewModel.updateDemoSplitPosition(it) },
                    onTryNowClick = {
                        val mode = uiState.selectedQualityMode
                        openPickerSheet(
                            title = "Try ${mode.title}",
                            subtitle = mode.description,
                            mainMode = EditorMainMode.EDIT,
                            initialTool = StudioToolId.ENHANCE,
                            presetState = StudioEditState(
                                enhanceMode = mode,
                                enhanceStrength = mode.defaultStrength
                            )
                        )
                    },
                    onBatchEnhanceClick = {
                        batchPhotoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            composable(ROUTE_EDITOR) {
                EditorScreen(
                    uiState = uiState,
                    preferences = preferences,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddClipClick = {
                        openPickerSheet(
                            title = "Switch Media Clip",
                            subtitle = "Choose another photo or video to edit",
                            mainMode = uiState.editorMainMode,
                            initialTool = uiState.selectedEditorTool
                        )
                    }
                )
            }
        }

        // Modals & Bottom Sheets
        if (showMediaPickSheet) {
            MediaPickSheet(
                title = mediaSheetTitle,
                subtitle = mediaSheetSubtitle,
                onDismiss = { showMediaPickSheet = false },
                onPickPhoto = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onPickVideo = {
                    mediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onSelectSample = { resId, title ->
                    viewModel.openSampleDrawable(resId, title)
                    if (navController.currentDestination?.route != ROUTE_EDITOR) {
                        navController.navigate(ROUTE_EDITOR)
                    }
                }
            )
        }

        if (showAllToolsSheet) {
            AllToolsSheet(
                initialQuery = uiState.searchQuery,
                onDismiss = { showAllToolsSheet = false },
                onSelectTool = { handleStudioToolAction(it) }
            )
        }

        if (showProSuiteSheet) {
            ProSuiteSheet(
                currentResolution = preferences.defaultResolution,
                onSelectResolution = { res ->
                    viewModel.updatePreferences { it.copy(defaultResolution = res) }
                },
                onLaunch4kEnhancer = {
                    viewModel.selectQualitySegment(EnhanceSegment.GENERAL)
                    viewModel.selectQualityMode(EnhanceMode.AI_UHD)
                    navController.navigate(ROUTE_QUALITY_ENHANCER)
                },
                onDismiss = { showProSuiteSheet = false }
            )
        }

        ExportStatusDialog(
            status = uiState.exportStatus,
            onCancel = { viewModel.cancelExport() },
            onDismiss = { viewModel.dismissExportStatus() }
        )
    }
}

@Composable
private fun StudioBottomTabBar(
    currentTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit
) {
    val colors = StudioTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.navBg)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                HomeTab.HOME to stringResource(R.string.tab_home),
                HomeTab.TEMPLATES to stringResource(R.string.tab_templates),
                HomeTab.CLOUD to stringResource(R.string.tab_cloud),
                HomeTab.ME to stringResource(R.string.tab_me)
            )
            tabs.forEach { (tab, label) ->
                val selected = tab == currentTab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("bottom_tab_${tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) colors.textPrimary else colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (selected) colors.textPrimary else Color.Transparent)
                    )
                }
            }
        }
    }
}
