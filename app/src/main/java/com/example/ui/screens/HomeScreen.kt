package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.StudioBadge
import com.example.model.StudioCatalog
import com.example.model.StudioToolId
import com.example.ui.components.NetworkStatusPill
import com.example.ui.components.StudioBadgePill
import com.example.ui.components.toolIconFor
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BadgeHot
import com.example.ui.theme.StudioTheme

@Composable
fun HomeScreen(
    isOnline: Boolean,
    onOpenSearch: () -> Unit,
    onOpenProSuite: () -> Unit,
    onHeroEditClick: () -> Unit,
    onHeroRetouchClick: () -> Unit,
    onToolClick: (StudioToolId) -> Unit,
    onTryShowcaseSample: (resId: Int, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header matching Screenshot 3
            item {
                HomeTopHeader(
                    isOnline = isOnline,
                    onOpenSearch = onOpenSearch,
                    onOpenProSuite = onOpenProSuite
                )
            }

            // Dual Hero Action Pills: Edit & Retouch
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onHeroEditClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .testTag("home_edit_button"),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.card,
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCut,
                                contentDescription = "Edit photo or video",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.action_edit),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Surface(
                        onClick = onHeroRetouchClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .testTag("home_retouch_button"),
                        shape = RoundedCornerShape(18.dp),
                        color = colors.contrastSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FaceRetouchingNatural,
                                contentDescription = "Retouch portrait",
                                tint = colors.onContrastSurface,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.action_retouch),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.onContrastSurface
                            )
                        }
                    }
                }
            }

            // 3x4 Studio Tool Grid (12 tools matching Screenshot 3)
            item {
                val rows = StudioCatalog.homeGridTools.chunked(3)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    rows.forEach { rowTools ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowTools.forEach { tool ->
                                HomeToolGridItem(
                                    tool = tool,
                                    onClick = { onToolClick(tool) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Smart Enhance Showcase Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smart Enhance",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "4K Demo >",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToolClick(StudioToolId.QUALITY_ENHANCER) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    ShowcaseFeatureCard(
                        resId = R.drawable.img_showcase_cinema,
                        title = "Night Cinema UHD",
                        subtitle = "Denoise low-light streets & boost neon clarity",
                        tag = "Ultra HD",
                        testTag = "showcase_cinema_card",
                        onClick = { onTryShowcaseSample(R.drawable.img_showcase_cinema, "Night Cinema") }
                    )

                    ShowcaseFeatureCard(
                        resId = R.drawable.img_demo_skyline,
                        title = "Architectural 4K Clarity",
                        subtitle = "Crisp edge reconstruction with natural HDR tone",
                        tag = "Hot",
                        testTag = "showcase_skyline_card",
                        onClick = { onTryShowcaseSample(R.drawable.img_demo_skyline, "Skyline 4K") }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HomeTopHeader(
    isOnline: Boolean,
    onOpenSearch: () -> Unit,
    onOpenProSuite: () -> Unit
) {
    val colors = StudioTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Bugti",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 26.sp, lineHeight = 28.sp),
                    color = colors.textPrimary
                )
                Text(
                    text = "Editz Studio",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = colors.textMuted
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 128.dp)
                        .testTag("home_search_bar"),
                    shape = RoundedCornerShape(999.dp),
                    color = colors.card,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search tools",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.search_tools_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AccentPurple, BadgeHot))
                        )
                        .clickable(onClick = onOpenProSuite)
                        .testTag("home_pro_diamond_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Diamond,
                        contentDescription = "Pro Studio settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkStatusPill(isOnline = isOnline)
            Surface(
                onClick = onOpenProSuite,
                shape = RoundedCornerShape(999.dp),
                color = colors.card,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.testTag("home_pro_pill")
            ) {
                Text(
                    text = stringResource(R.string.pro_banner_pill),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeToolGridItem(
    tool: StudioToolId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val isAllTools = tool == StudioToolId.ALL_TOOLS
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag("home_tool_${tool.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isAllTools) colors.cardSecondary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = toolIconFor(tool),
                    contentDescription = tool.label,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(27.dp)
                )
            }
            if (tool.badge != StudioBadge.NONE) {
                StudioBadgePill(
                    badge = tool.badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-2).dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tool.label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun ShowcaseFeatureCard(
    resId: Int,
    title: String,
    subtitle: String,
    tag: String,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = StudioTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0x22000000), Color(0xE0121112))
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xAA121112))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFD5D5DC)
                )
            }
        }
    }
}
