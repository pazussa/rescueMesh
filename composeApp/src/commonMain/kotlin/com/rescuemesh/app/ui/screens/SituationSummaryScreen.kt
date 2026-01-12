package com.rescuemesh.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.ai.SituationSummary
import com.rescuemesh.app.ai.DangerZoneInfo
import com.rescuemesh.app.ai.ResourceNeedInfo
import com.rescuemesh.app.ai.PriorityMessageInfo
import com.rescuemesh.app.model.DangerType
import com.rescuemesh.app.model.MessagePriority
import com.rescuemesh.app.model.ResourceType
import com.rescuemesh.app.ui.theme.RescueMeshColors

/**
 * Pantalla de Resumen de Situación
 * "Qué está pasando en 30 segundos"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SituationSummaryScreen(
    summary: SituationSummary,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Situation Summary")
                        Text(
                            text = " Automatically updated",
                            fontSize = 12.sp,
                            color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Surface,
                    titleContentColor = RescueMeshColors.OnSurface
                )
            )
        },
        containerColor = RescueMeshColors.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen principal
            item {
                SummaryCard(summary)
            }
            
            // Estadísticas rápidas
            item {
                QuickStatsRow(summary)
            }
            
            // Alertas críticas
            if (summary.criticalCount > 0) {
                item {
                    CriticalAlertCard(summary.criticalCount)
                }
            }
            
            // Mensajes prioritarios
            if (summary.priorityMessages.isNotEmpty()) {
                item {
                    Text(
                        text = " Priority Messages (${summary.priorityMessages.size})",
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.OnBackground,
                        fontSize = 16.sp
                    )
                }
                
                items(summary.priorityMessages) { msg ->
                    PriorityMessageCard(msg)
                }
            }
            
            // Zonas de peligro
            if (summary.dangerZones.isNotEmpty()) {
                item {
                    Text(
                        text = "WARNING: Danger Zones (${summary.dangerZones.size})",
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.OnBackground,
                        fontSize = 16.sp
                    )
                }
                
                items(summary.dangerZones) { danger ->
                    DangerZoneCard(danger)
                }
            }
            
            // Necesidades de recursos
            if (summary.resourceNeeds.isNotEmpty()) {
                item {
                    Text(
                        text = "📦 Resource Needs",
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.OnBackground,
                        fontSize = 16.sp
                    )
                }
                
                items(summary.resourceNeeds) { need ->
                    ResourceNeedCard(need)
                }
            }
            
            // Info sobre resumen
            item {
                SummaryInfoCard()
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: SituationSummary) {
    val bgColor = when {
        summary.criticalCount > 0 -> RescueMeshColors.Danger.copy(alpha = 0.2f)
        summary.activeSOSCount > 0 -> RescueMeshColors.Warning.copy(alpha = 0.2f)
        else -> RescueMeshColors.Success.copy(alpha = 0.2f)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        summary.criticalCount > 0 -> ""
                        summary.activeSOSCount > 0 -> "WARNING:"
                        else -> "OK:"
                    },
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when {
                        summary.criticalCount > 0 -> "CRITICAL SITUATION"
                        summary.activeSOSCount > 0 -> "Active Emergencies"
                        else -> "Stable Situation"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = RescueMeshColors.OnBackground
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = summary.summaryText,
                fontSize = 15.sp,
                color = RescueMeshColors.OnBackground,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun QuickStatsRow(summary: SituationSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatBox(
            modifier = Modifier.weight(1f),
            emoji = "",
            value = summary.activeSOSCount.toString(),
            label = "SOS",
            color = RescueMeshColors.Danger
        )
        StatBox(
            modifier = Modifier.weight(1f),
            emoji = "",
            value = summary.totalPeopleAffected.toString(),
            label = "People",
            color = RescueMeshColors.Warning
        )
        StatBox(
            modifier = Modifier.weight(1f),
            emoji = "✓",
            value = summary.safeCount.toString(),
            label = "Safe",
            color = RescueMeshColors.Success
        )
        StatBox(
            modifier = Modifier.weight(1f),
            emoji = "",
            value = summary.totalMessages.toString(),
            label = "Messages",
            color = RescueMeshColors.Info
        )
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = RescueMeshColors.OnBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CriticalAlertCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Danger
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$count CRITICAL EMERGENCIES",
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = "Require immediate attention",
                    color = RescueMeshColors.OnPrimary.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DangerZoneCard(danger: DangerZoneInfo) {
    val emoji = when (danger.type) {
        DangerType.FIRE -> "Fire:"
        DangerType.COLLAPSE -> "Collapse:"
        DangerType.FLOOD -> "Flood:"
        DangerType.GAS_LEAK -> ""
        DangerType.ELECTRICAL -> "Electrical:"
        DangerType.BLOCKED_ROAD -> "Blocked:"
        DangerType.UNSAFE_BUILDING -> ""
        DangerType.OTHER -> "WARNING:"
    }
    
    val severityColor = when (danger.severity) {
        3 -> RescueMeshColors.Danger
        2 -> RescueMeshColors.Warning
        else -> RescueMeshColors.Info
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = danger.type.name.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnSurface
                )
                Text(
                    text = danger.description,
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 2
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(severityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Level ${danger.severity}",
                        fontSize = 11.sp,
                        color = severityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (danger.isBlocking) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ Blocks passage",
                        fontSize = 11.sp,
                        color = RescueMeshColors.Danger
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourceNeedCard(need: ResourceNeedInfo) {
    val emoji = when (need.type) {
        ResourceType.WATER -> "Water:"
        ResourceType.FOOD -> "Food:"
        ResourceType.MEDICINE -> "Medicine:"
        ResourceType.FIRST_AID -> "Injured:"
        ResourceType.BLANKETS -> "Blankets:"
        ResourceType.TRANSPORT -> "Transport:"
        ResourceType.SHELTER -> "Shelter:"
        ResourceType.FLASHLIGHT -> "Flashlight:"
        ResourceType.BATTERY -> "Battery:"
        ResourceType.OTHER -> "Package:"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (need.urgent) 
                RescueMeshColors.Warning.copy(alpha = 0.15f) 
            else 
                RescueMeshColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = need.type.name.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnSurface
                )
                if (need.urgent) {
                    Text(
                        text = "⚡ URGENT",
                        color = RescueMeshColors.Warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = "x${need.quantity}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = RescueMeshColors.Primary
            )
        }
    }
}

@Composable
private fun SummaryInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Info.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ℹ️ About This Summary",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.Info,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This summary is automatically generated by analyzing received messages. " +
                       "It detects urgencies and prioritizes the most important information to give you a quick overview of the situation.",
                fontSize = 12.sp,
                color = RescueMeshColors.OnBackground.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(text = "✓ ", color = RescueMeshColors.Success, fontSize = 12.sp)
                Text(
                    text = "100% offline • Local analysis • Automatic updates",
                    fontSize = 12.sp,
                    color = RescueMeshColors.OnBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun PriorityMessageCard(msg: PriorityMessageInfo) {
    val priorityColor = when (msg.priority) {
        MessagePriority.CRITICAL -> RescueMeshColors.Danger
        MessagePriority.HIGH -> RescueMeshColors.Warning
        else -> RescueMeshColors.Info
    }
    
    val priorityLabel = when (msg.priority) {
        MessagePriority.CRITICAL -> "CRITICAL"
        MessagePriority.HIGH -> "HIGH"
        else -> "NORMAL"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = priorityColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = msg.senderName,
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnSurface,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(priorityColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = priorityLabel,
                        fontSize = 10.sp,
                        color = if (msg.priority == MessagePriority.HIGH) 
                            androidx.compose.ui.graphics.Color.Black 
                        else 
                            androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = msg.summary,
                color = RescueMeshColors.OnSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Tiempo relativo
            val timeAgo = formatTimeAgo(msg.timestamp)
            Text(
                text = timeAgo,
                color = RescueMeshColors.OnSurface.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Formatea timestamp en tiempo relativo
 */
private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} min ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        else -> "${diff / 86400_000} days ago"
    }
}
