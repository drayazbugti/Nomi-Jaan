package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoFilter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FilterCategory
import com.example.model.FxCategory
import com.example.model.FxEffectSpec
import com.example.model.FxTab
import com.example.model.StudioCatalog
import com.example.model.StudioEditState
import com.example.model.StudioFilterSpec
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePill
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextOnWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
internal fun ToolConfirmCancelBar(
    title: String = "",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    centerContent: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.7f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("panel_cancel_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cancel changes",
                    tint = TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (centerContent != null) {
                    centerContent()
                } else if (title.isNotEmpty()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                onClick = onConfirm,
                shape = CircleShape,
                color = AccentPurple,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("panel_confirm_btn")
            ) {
                Box(
                    modifier = Modifier.size(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Confirm changes",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun FxEffectsPickerPanel(
    editState: StudioEditState,
    onSelectFx: (String) -> Unit,
    onUpdateFxIntensity: (Float) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val activeSpec = remember(editState.fxEffectId) {
        StudioCatalog.fxEffects.firstOrNull { it.id == editState.fxEffectId }
    }
    var activeTab by remember(activeSpec?.tab) {
        mutableStateOf(activeSpec?.tab ?: FxTab.BASIC)
    }
    var activeCategory by remember(activeTab) {
        mutableStateOf(
            if (activeSpec != null && activeSpec.tab == activeTab) {
                activeSpec.category
            } else if (activeTab == FxTab.BASIC) {
                FxCategory.BASIC
            } else {
                FxCategory.BODY_AURA
            }
        )
    }

    // Top Tabs: Basic Effect | Body Effect
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FxTab.entries.forEach { tab ->
            val selected = tab == activeTab
            Surface(
                onClick = {
                    activeTab = tab
                    activeCategory = if (tab == FxTab.BASIC) FxCategory.BASIC else FxCategory.BODY_AURA
                },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) AccentPurple else SurfacePill,
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("fx_tab_${tab.name.lowercase()}")
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else TextSecondary
                    )
                }
            }
        }
    }

    // Category Pills
    val tabCategories = remember(activeTab) {
        FxCategory.entries.filter { it.tab == activeTab }
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(tabCategories, key = { it.name }) { cat ->
            val isSel = cat == activeCategory
            Surface(
                onClick = { activeCategory = cat },
                shape = RoundedCornerShape(999.dp),
                color = if (isSel) SurfaceBright else SurfaceElevated,
                border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle),
                modifier = Modifier.testTag("fx_cat_${cat.name.lowercase()}")
            ) {
                Text(
                    text = cat.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) TextOnWhite else TextSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }

    // Effect Thumbnail Cards
    val visibleEffects = remember(activeTab, activeCategory) {
        val noneItem = StudioCatalog.fxEffects.firstOrNull {
            it.tab == activeTab && (it.id == "fx_none" || it.id == "fx_body_none")
        }
        val catItems = StudioCatalog.fxEffects.filter { it.tab == activeTab && it.category == activeCategory }
        if (noneItem != null && catItems.none { it.id == noneItem.id }) {
            listOf(noneItem) + catItems
        } else {
            catItems
        }
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(visibleEffects, key = { it.id }) { fx ->
            val isSelected = editState.fxEffectId == fx.id
            FxEffectThumbnailCard(
                fx = fx,
                isSelected = isSelected,
                onClick = { onSelectFx(fx.id) }
            )
        }
    }

    if (editState.fxEffectId != "fx_none" && editState.fxEffectId != "fx_body_none") {
        LabeledStudioSlider(
            label = "FX Intensity",
            value = editState.fxIntensity,
            valueRange = 0.1f..1f,
            displayValue = "${(editState.fxIntensity * 100).toInt()}%",
            onValueChange = onUpdateFxIntensity
        )
    }

    ToolConfirmCancelBar(
        title = activeSpec?.name ?: "FX Effects",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

@Composable
private fun FxEffectThumbnailCard(
    fx: FxEffectSpec,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accent = Color(fx.accentArgb)
    val icon = when (fx.category) {
        FxCategory.ROTATE -> Icons.Filled.Refresh
        FxCategory.BLUR -> Icons.Filled.BlurOn
        FxCategory.BEATS, FxCategory.GLITCH -> Icons.Filled.FlashOn
        FxCategory.BODY_AURA, FxCategory.BODY_CLONE, FxCategory.BODY_OUTLINE, FxCategory.BODY_HALO, FxCategory.BODY_LASER ->
            Icons.Outlined.AccessibilityNew
        FxCategory.GLOW -> Icons.Filled.AutoAwesome
        else -> Icons.Filled.PhotoFilter
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("fx_card_${fx.id}")
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            accent.copy(alpha = 0.45f),
                            Color(0xFF1D1B22)
                        )
                    )
                )
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color.White else BorderSubtle,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = fx.name,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AccentPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = fx.name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextPrimary else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun FilterAndAdjustPanel(
    editState: StudioEditState,
    onSelectFilterSpec: (StudioFilterSpec) -> Unit,
    onUpdateIntensity: (Float) -> Unit,
    onUpdateEditState: (Boolean, (StudioEditState) -> StudioEditState) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0 = Filter, 1 = Adjust
    val activeSpec = remember(editState.filterSpecId) {
        StudioCatalog.filterPresetsCatalog.firstOrNull { it.id == editState.filterSpecId }
    }
    var selectedCategory by remember(activeSpec?.category) {
        mutableStateOf(activeSpec?.category ?: FilterCategory.AESTHETIC)
    }

    // Filter | Adjust Top Tabs
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            onClick = { activeSubTab = 0 },
            shape = RoundedCornerShape(999.dp),
            color = if (activeSubTab == 0) AccentPurple else SurfacePill,
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("filter_tab_filter")
        ) {
            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Filter (18 Categories)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeSubTab == 0) Color.White else TextSecondary
                )
            }
        }
        Surface(
            onClick = { activeSubTab = 1 },
            shape = RoundedCornerShape(999.dp),
            color = if (activeSubTab == 1) AccentPurple else SurfacePill,
            modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("filter_tab_adjust")
        ) {
            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Adjust",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeSubTab == 1) Color.White else TextSecondary
                )
            }
        }
    }

    if (activeSubTab == 0) {
        // 18 Category Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(FilterCategory.entries, key = { it.name }) { category ->
                val isSel = category == selectedCategory
                Surface(
                    onClick = { selectedCategory = category },
                    shape = RoundedCornerShape(999.dp),
                    color = if (isSel) SurfaceBright else SurfaceElevated,
                    border = BorderStroke(1.dp, if (isSel) SurfaceBright else BorderSubtle),
                    modifier = Modifier.testTag("filter_cat_${category.name.lowercase()}")
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) TextOnWhite else TextSecondary,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Swatch Cards for Selected Category (+ Original reset shortcut if not Aesthetic)
        val swatches = remember(selectedCategory) {
            val orig = StudioCatalog.filterPresetsCatalog.firstOrNull { it.id == "natural" }
            val catSwatches = StudioCatalog.filterPresetsCatalog.filter { it.category == selectedCategory }
            if (orig != null && catSwatches.none { it.id == orig.id }) {
                listOf(orig) + catSwatches
            } else {
                catSwatches
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(swatches, key = { it.id }) { spec ->
                val isSelected = editState.filterSpecId == spec.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectFilterSpec(spec) }
                        .testTag("filter_swatch_${spec.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(spec.swatchHex), Color(spec.secondaryHex))
                                )
                            )
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else BorderSubtle,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xAA000000)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected filter",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = spec.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (editState.filterSpecId != "natural") {
            LabeledStudioSlider(
                label = "Intensity",
                value = editState.filterIntensity,
                valueRange = 0f..1f,
                displayValue = "${(editState.filterIntensity * 100).toInt()}",
                onValueChange = onUpdateIntensity
            )
        }
    } else {
        AdjustPanel(
            editState = editState,
            onUpdateEditState = onUpdateEditState
        )
    }

    ToolConfirmCancelBar(
        title = if (activeSubTab == 0) (activeSpec?.name ?: "Filter") else "Adjust",
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}
