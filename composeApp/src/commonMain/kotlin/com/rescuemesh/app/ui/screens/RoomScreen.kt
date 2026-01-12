package com.rescuemesh.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.model.*
import com.rescuemesh.app.ui.theme.RescueMeshColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    room: IncidentRoom,
    messages: List<MeshMessage>,
    connectedPeers: Int,
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    onSendSos: () -> Unit,
    onSendImOk: () -> Unit,
    onSendResource: () -> Unit,
    onSendDanger: () -> Unit,
    onSendChat: (String) -> Unit,
    onShowRoomInfo: () -> Unit,
    onShowNetworkStatus: () -> Unit,
    onShowAISummary: () -> Unit,
    onLeaveRoom: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var chatMessage by remember { mutableStateOf("") }
    var showChatInput by remember { mutableStateOf(false) }
    val strings = rememberStrings()
    val listState = rememberLazyListState()
    
    val isActive = isAdvertising || isDiscovering
    val statusColor by animateColorAsState(
        targetValue = when {
            isAdvertising && isDiscovering -> RescueMeshColors.Success
            isActive -> RescueMeshColors.Warning
            else -> RescueMeshColors.Danger
        },
        animationSpec = tween(300)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RescueMeshColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RescueMeshColors.Surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RescueMeshColors.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$connectedPeers connected",
                            fontSize = 12.sp,
                            color = RescueMeshColors.OnSurfaceVariant
                        )
                    }
                    
                    IconButton(onClick = onShowRoomInfo, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Share, "Share", tint = RescueMeshColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = RescueMeshColors.OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Summary", fontSize = 14.sp) },
                            onClick = { showMenu = false; onShowAISummary() },
                            leadingIcon = { Text("📊", fontSize = 16.sp) }
                        )
                        DropdownMenuItem(
                            text = { Text("Network", fontSize = 14.sp) },
                            onClick = { showMenu = false; onShowNetworkStatus() },
                            leadingIcon = { Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp)) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Leave", fontSize = 14.sp, color = RescueMeshColors.Danger) },
                            onClick = { showMenu = false; onLeaveRoom() },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = RescueMeshColors.Danger, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }
            
            // Messages
            Box(modifier = Modifier.weight(1f)) {
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📭", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No messages yet", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = RescueMeshColors.OnBackground)
                            Text("Use the buttons below to send", fontSize = 13.sp, color = RescueMeshColors.OnSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message -> MessageCard(message) }
                    }
                }
            }
            
            // Chat Input
            AnimatedVisibility(
                visible = showChatInput,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(modifier = Modifier.fillMaxWidth(), color = RescueMeshColors.Surface) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showChatInput = false; chatMessage = "" }) {
                            Icon(Icons.Default.Close, "Close", tint = RescueMeshColors.OnSurfaceVariant)
                        }
                        
                        OutlinedTextField(
                            value = chatMessage,
                            onValueChange = { chatMessage = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Message...", fontSize = 14.sp, color = RescueMeshColors.TextHint) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RescueMeshColors.Primary,
                                unfocusedBorderColor = RescueMeshColors.Divider
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = {
                                if (chatMessage.isNotBlank()) {
                                    onSendChat(chatMessage)
                                    chatMessage = ""
                                    showChatInput = false
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (chatMessage.isNotBlank()) RescueMeshColors.Primary else RescueMeshColors.Divider)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            
            // Action Bar - All buttons visible
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RescueMeshColors.Surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // SOS Button
                    Button(
                        onClick = onSendSos,
                        modifier = Modifier.weight(1f).height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RescueMeshColors.Danger),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("🆘 SOS", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    // I'm OK Button
                    OutlinedButton(
                        onClick = onSendImOk,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RescueMeshColors.Success),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RescueMeshColors.Success),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("✓ I'm OK", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    
                    // Chat Button
                    OutlinedButton(
                        onClick = { showChatInput = !showChatInput },
                        modifier = Modifier.weight(0.7f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showChatInput) RescueMeshColors.Primary.copy(alpha = 0.1f) else Color.Transparent,
                            contentColor = RescueMeshColors.Primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, RescueMeshColors.Primary),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("💬", fontSize = 18.sp)
                    }
                    
                    // More Button
                    var showMore by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showMore = true },
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RescueMeshColors.OnSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, RescueMeshColors.Divider),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("•••", fontSize = 14.sp)
                        }
                        
                        DropdownMenu(expanded = showMore, onDismissRequest = { showMore = false }) {
                            DropdownMenuItem(
                                text = { Text("Request resources", fontSize = 14.sp) },
                                onClick = { showMore = false; onSendResource() },
                                leadingIcon = { Text("📦", fontSize = 16.sp) }
                            )
                            DropdownMenuItem(
                                text = { Text("Report danger", fontSize = 14.sp) },
                                onClick = { showMore = false; onSendDanger() },
                                leadingIcon = { Text("⚠️", fontSize = 16.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageCard(message: MeshMessage) {
    val priorityColor = when (message.priority) {
        MessagePriority.CRITICAL -> RescueMeshColors.PriorityCritical
        MessagePriority.HIGH -> RescueMeshColors.PriorityHigh
        MessagePriority.MEDIUM -> RescueMeshColors.PriorityMedium
        MessagePriority.LOW -> RescueMeshColors.PriorityLow
        MessagePriority.INFO -> RescueMeshColors.PriorityInfo
    }
    
    val (emoji, title, content) = when (val c = message.content) {
        is MessageContent.Sos -> Triple(
            "🆘",
            getSosCategoryText(c.category),
            if (c.peopleCount > 1) "${c.peopleCount} people" else ""
        )
        is MessageContent.ImOk -> Triple("✅", "I'm OK", c.message.takeIf { it != "Estoy bien" && it != "I'm OK" } ?: "")
        is MessageContent.ResourceRequest -> Triple("📦", getResourceTypeText(c.resourceType), "Qty: ${c.quantity}${if (c.urgent) " • ⚡" else ""}")
        is MessageContent.DangerReport -> Triple("⚠️", getDangerTypeText(c.dangerType), if (c.isBlocking) "🚫 Blocks access" else "")
        is MessageContent.Chat -> Triple("💬", "", c.text)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RescueMeshColors.Surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(priorityColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(message.senderName, fontWeight = FontWeight.SemiBold, color = RescueMeshColors.OnSurface, fontSize = 13.sp)
                        if (message.hopCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("↗${message.hopCount}", fontSize = 10.sp, color = RescueMeshColors.OnSurfaceVariant,
                                modifier = Modifier.background(RescueMeshColors.SurfaceVariant, RoundedCornerShape(3.dp)).padding(horizontal = 3.dp))
                        }
                    }
                    Text(formatTime(message.timestamp), fontSize = 11.sp, color = RescueMeshColors.OnSurfaceVariant)
                }
                
                if (title.isNotEmpty()) {
                    Text(title, fontWeight = FontWeight.Medium, color = priorityColor, fontSize = 14.sp)
                }
                
                if (content.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(content, fontSize = 13.sp, color = RescueMeshColors.OnSurface.copy(alpha = 0.8f))
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}

private fun getSosCategoryText(category: SosCategory): String = when (category) {
    SosCategory.MEDICAL -> "Medical emergency"
    SosCategory.FIRE -> "Fire"
    SosCategory.TRAPPED -> "Trapped"
    SosCategory.CHILDREN -> "Children in danger"
    SosCategory.ELDERLY -> "Elderly"
    SosCategory.INJURED -> "Injured"
    SosCategory.OTHER -> "Emergency"
}

private fun getResourceTypeText(type: ResourceType): String = when (type) {
    ResourceType.WATER -> "Water"
    ResourceType.FOOD -> "Food"
    ResourceType.FIRST_AID -> "First Aid"
    ResourceType.TRANSPORT -> "Transport"
    ResourceType.SHELTER -> "Shelter"
    ResourceType.BLANKETS -> "Blankets"
    ResourceType.FLASHLIGHT -> "Flashlight"
    ResourceType.BATTERY -> "Batteries"
    ResourceType.MEDICINE -> "Medicine"
    ResourceType.OTHER -> "Other"
}

private fun getDangerTypeText(type: DangerType): String = when (type) {
    DangerType.FIRE -> "Fire"
    DangerType.COLLAPSE -> "Collapse"
    DangerType.FLOOD -> "Flood"
    DangerType.GAS_LEAK -> "Gas Leak"
    DangerType.BLOCKED_ROAD -> "Blocked Road"
    DangerType.UNSAFE_BUILDING -> "Unsafe Building"
    DangerType.ELECTRICAL -> "Electrical Hazard"
    DangerType.OTHER -> "Danger"
}
