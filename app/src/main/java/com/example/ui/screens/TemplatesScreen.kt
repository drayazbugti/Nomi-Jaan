package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.StudioCatalog
import com.example.model.StudioTemplate
import com.example.ui.theme.BadgeHot
import com.example.ui.theme.StudioTheme

@Composable
fun TemplatesScreen(
    onUseTemplateWithPicker: (StudioTemplate) -> Unit,
    onTryTemplateSample: (StudioTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = StudioTheme.colors
    val categories = remember {
        listOf("All", "Short Reels", "Cinematic", "Portrait", "Festive & Islamic", "Neon", "Film", "HDR")
    }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == "All") {
            StudioCatalog.templates
        } else {
            StudioCatalog.templates.filter { it.category == selectedCategory }
        }
    }

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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Studio Templates",
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.accent.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "${StudioCatalog.templates.size} Offline",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "100 ready-to-use short templates · Works 100% offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        val isSel = category == selectedCategory
                        Surface(
                            onClick = { selectedCategory = category },
                            shape = RoundedCornerShape(999.dp),
                            color = if (isSel) colors.contrastSurface else colors.cardSecondary,
                            border = BorderStroke(1.dp, if (isSel) colors.contrastSurface else colors.border),
                            modifier = Modifier.testTag("template_cat_${category.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = if (category == "All") "All (${StudioCatalog.templates.size})" else category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) colors.onContrastSurface else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            items(filteredTemplates, key = { it.id }) { template ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.card,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("template_card_${template.id}")
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(168.dp)
                        ) {
                            Image(
                                painter = painterResource(id = template.previewRes),
                                contentDescription = template.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color(0xD8121112))
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(BadgeHot)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = template.tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Color(0xB3121112))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${template.category} · ${template.durationSec}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = template.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = template.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD5D5DC)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                onClick = { onTryTemplateSample(template) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("template_sample_${template.id}"),
                                shape = RoundedCornerShape(999.dp),
                                color = colors.cardSecondary,
                                border = BorderStroke(1.dp, colors.border)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Try Sample",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.textPrimary
                                    )
                                }
                            }

                            Button(
                                onClick = { onUseTemplateWithPicker(template) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("template_apply_${template.id}"),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.contrastSurface,
                                    contentColor = colors.onContrastSurface
                                )
                            ) {
                                Text(
                                    text = "Use Media",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
