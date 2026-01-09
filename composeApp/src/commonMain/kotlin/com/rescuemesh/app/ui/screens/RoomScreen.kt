package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.model.*
import com.rescuemesh.app.ui.components.NetworkStatusBanner
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
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(room.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = "$connectedPeers ${strings.connected} • ${if (isAdvertising) "Signal:" else "⏳"}",
                            fontSize = 12.sp,
                            color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    // Summary button
                    IconButton(onClick = onShowAISummary) {
                        Text(text = "", fontSize = 20.sp)
                    }
                    // Network status button
                    IconButton(onClick = onShowNetworkStatus) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = strings.networkStatus,
                            tint = if (isAdvertising && isDiscovering) 
                                RescueMeshColors.Success 
                            else if (isAdvertising || isDiscovering)
                                RescueMeshColors.Warning
                            else 
                                RescueMeshColors.Danger
                        )
                    }
                    IconButton(onClick = onShowRoomInfo) {
                        Icon(Icons.Default.Info, contentDescription = strings.roomInfo)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = strings.menu)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(" ${strings.situationSummary}") },
                            onClick = {
                                showMenu = false
                                onShowAISummary()
                            },
                            leadingIcon = {
                                Text(text = "")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.networkStatus) },
                            onClick = {
                                showMenu = false
                                onShowNetworkStatus()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.leaveRoom) },
                            onClick = {
                                showMenu = false
                                onLeaveRoom()
                            },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Surface,
                    titleContentColor = RescueMeshColors.OnSurface,
                    actionIconContentColor = RescueMeshColors.OnSurface
                )
            )
        },
        bottomBar = {
            Column {
                // Chat input - visible cuando showChatInput es true
                if (showChatInput) {
                    ChatInputBar(
                        message = chatMessage,
                        onMessageChange = { chatMessage = it },
                        onSend = {
                            if (chatMessage.isNotBlank()) {
                                onSendChat(chatMessage)
                                chatMessage = ""
                                showChatInput = false
                            }
                        },
                        onClose = { 
                            showChatInput = false
                            chatMessage = ""
                        }
                    )
                }
                
                ActionBar(
                    onSendSos = onSendSos,
                    onSendImOk = onSendImOk,
                    onSendResource = onSendResource,
                    onSendDanger = onSendDanger,
                    onSendChat = { showChatInput = !showChatInput },
                    isChatOpen = showChatInput
                )
            }
        },
        containerColor = RescueMeshColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Banner de estado de red siempre visible
            NetworkStatusBanner(
                isAdvertising = isAdvertising,
                isDiscovering = isDiscovering,
                connectedPeers = connectedPeers,
                onClick = onShowNetworkStatus
            )
            
            if (messages.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = strings.noMessagesYet,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RescueMeshColors.OnBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.sendFirstMessage,
                            fontSize = 14.sp,
                            color = RescueMeshColors.OnBackground.copy(alpha = 0.7f),
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(messages) { message ->
                        MessageCard(message)
                    }
                }
            }
        }
    }
}

/**
 * Network status banner always visible in the room
 */
@Composable
private fun NetworkStatusBanner(
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    connectedPeers: Int,
    onClick: () -> Unit
) {
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    
    val isActive = isAdvertising || isDiscovering
    val statusColor = when {
        isAdvertising && isDiscovering -> RescueMeshColors.Success
        isActive -> RescueMeshColors.Warning
        else -> RescueMeshColors.Danger
    }
    
    Surface(
        onClick = onClick,
        color = statusColor.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = " Nearby Connections: ${if (isActive) strings.active.uppercase() else strings.inactive.uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = statusColor
                )
                Text(
                    text = buildString {
                        if (isAdvertising) append(if (isEnglish) "Signal: Visible" else "Signal: Visible")
                        if (isAdvertising && isDiscovering) append(" | ")
                        if (isDiscovering) append(if (isEnglish) " Searching" else " Buscando")
                        append(" | $connectedPeers peers")
                    },
                    fontSize = 11.sp,
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                )
            }
            
            Text(
                text = if (isEnglish) "See details →" else "Ver detalles →",
                fontSize = 11.sp,
                color = statusColor
            )
        }
    }
}

@Composable
private fun ActionBar(
    onSendSos: () -> Unit,
    onSendImOk: () -> Unit,
    onSendResource: () -> Unit,
    onSendDanger: () -> Unit,
    onSendChat: () -> Unit,
    isChatOpen: Boolean
) {
    val strings = rememberStrings()
    
    Surface(
        color = RescueMeshColors.Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // SOS Button
            ActionButton(
                emoji = "",
                label = strings.sendSos,
                color = RescueMeshColors.Danger,
                onClick = onSendSos
            )
            
            // I'm OK Button
            ActionButton(
                emoji = "OK:",
                label = strings.imOk,
                color = RescueMeshColors.Success,
                onClick = onSendImOk
            )
            
            // Chat Button
            ActionButton(
                emoji = "Chat:",
                label = "Chat",
                color = if (isChatOpen) RescueMeshColors.Primary else RescueMeshColors.Surface,
                onClick = onSendChat,
                outlined = !isChatOpen
            )
            
            // Resource Request
            ActionButton(
                emoji = "Package:",
                label = strings.requestResources,
                color = RescueMeshColors.Info,
                onClick = onSendResource
            )
            
            // Danger Report
            ActionButton(
                emoji = "WARNING:",
                label = strings.reportDanger,
                color = RescueMeshColors.Warning,
                onClick = onSendDanger
            )
        }
    }
}

@Composable
private fun ActionButton(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    outlined: Boolean = false
) {
    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(RescueMeshColors.Primary)
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = RescueMeshColors.OnSurface)
            }
        }
    } else {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(emoji, fontSize = 20.sp)
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit
) {
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    
    Surface(
        color = RescueMeshColors.Primary.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = if (isEnglish) "Close" else "Cerrar",
                    tint = RescueMeshColors.OnBackground
                )
            }
            
            // Text input
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                placeholder = { 
                    Text(
                        if (isEnglish) "Type a message..." else "Escribe un mensaje...",
                        color = RescueMeshColors.OnBackground.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    ) 
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RescueMeshColors.Primary,
                    unfocusedBorderColor = RescueMeshColors.OnBackground.copy(alpha = 0.3f),
                    focusedTextColor = RescueMeshColors.OnBackground,
                    unfocusedTextColor = RescueMeshColors.OnBackground,
                    focusedContainerColor = RescueMeshColors.Surface,
                    unfocusedContainerColor = RescueMeshColors.Surface,
                    cursorColor = RescueMeshColors.Primary
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (message.isNotBlank()) 
                            RescueMeshColors.Primary 
                        else 
                            RescueMeshColors.Primary.copy(alpha = 0.3f)
                    ),
                enabled = message.isNotBlank()
            ) {
                Icon(
                    @Suppress("DEPRECATION")
                    Icons.Default.Send,
                    contentDescription = if (isEnglish) "Send" else "Enviar",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun MessageCard(message: MeshMessage) {
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    
    val priorityColor = when (message.priority) {
        MessagePriority.CRITICAL -> RescueMeshColors.PriorityCritical
        MessagePriority.HIGH -> RescueMeshColors.PriorityHigh
        MessagePriority.MEDIUM -> RescueMeshColors.PriorityMedium
        MessagePriority.LOW -> RescueMeshColors.PriorityLow
        MessagePriority.INFO -> RescueMeshColors.PriorityInfo
    }
    
    val (emoji, title, content) = when (val c = message.content) {
        is MessageContent.Sos -> Triple(
            "",
            "${strings.messageTypeSos} - ${getSosCategoryText(c.category, isEnglish)}",
            "${c.description}\n${c.peopleCount} ${if (c.peopleCount == 1) strings.person else strings.people}"
        )
        is MessageContent.ImOk -> Triple(
            "OK:",
            strings.messageTypeImOk,
            c.message
        )
        is MessageContent.ResourceRequest -> Triple(
            "Package:",
            "${if (isEnglish) "Request" else "Solicitud"}: ${getResourceTypeText(c.resourceType, isEnglish)}",
            "${c.description}\n${strings.quantity}: ${c.quantity}${if (c.urgent) " Electrical: ${strings.urgent.uppercase()}" else ""}"
        )
        is MessageContent.DangerReport -> Triple(
            "WARNING:",
            "${if (isEnglish) "Danger" else "Peligro"}: ${getDangerTypeText(c.dangerType, isEnglish)}",
            "${c.description}\n${strings.severity}: ${c.severity}/10${if (c.isBlocking) " Blocked: ${strings.blocksPassage}" else ""}"
        )
        is MessageContent.Chat -> Triple(
            "Chat:",
            strings.messageTypeChat,
            c.text
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Indicador de prioridad
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(priorityColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            color = RescueMeshColors.OnSurface,
                            fontSize = 15.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Sender
                Text(
                    text = "De: ${message.senderName}",
                    fontSize = 12.sp,
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Content
                Text(
                    text = content,
                    fontSize = 14.sp,
                    color = RescueMeshColors.OnSurface,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        fontSize = 11.sp,
                        color = RescueMeshColors.OnSurface.copy(alpha = 0.5f)
                    )
                    
                    if (message.hopCount > 0) {
                        Text(
                            text = " ${message.hopCount} ${if (isEnglish) "hop(s)" else "salto(s)"}",
                            fontSize = 11.sp,
                            color = RescueMeshColors.OnSurface.copy(alpha = 0.5f)
                        )
                    }
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

private fun getSosCategoryText(category: SosCategory, isEnglish: Boolean): String = when (category) {
    SosCategory.MEDICAL -> if (isEnglish) "Medical" else "Médico"
    SosCategory.FIRE -> if (isEnglish) "Fire" else "Fuego"
    SosCategory.TRAPPED -> if (isEnglish) "Trapped" else "Atrapado"
    SosCategory.CHILDREN -> if (isEnglish) "Children" else "Niños"
    SosCategory.ELDERLY -> if (isEnglish) "Elderly" else "Adulto mayor"
    SosCategory.INJURED -> if (isEnglish) "Injured" else "Herido"
    SosCategory.OTHER -> if (isEnglish) "Other" else "Otro"
}

private fun getResourceTypeText(type: ResourceType, isEnglish: Boolean): String = when (type) {
    ResourceType.WATER -> if (isEnglish) "Water" else "Agua"
    ResourceType.FOOD -> if (isEnglish) "Food" else "Comida"
    ResourceType.FIRST_AID -> if (isEnglish) "First Aid" else "Botiquín"
    ResourceType.TRANSPORT -> if (isEnglish) "Transport" else "Transporte"
    ResourceType.SHELTER -> if (isEnglish) "Shelter" else "Refugio"
    ResourceType.BLANKETS -> if (isEnglish) "Blankets" else "Mantas"
    ResourceType.FLASHLIGHT -> if (isEnglish) "Flashlight" else "Linterna"
    ResourceType.BATTERY -> if (isEnglish) "Battery" else "Baterías"
    ResourceType.MEDICINE -> if (isEnglish) "Medicine" else "Medicinas"
    ResourceType.OTHER -> if (isEnglish) "Other" else "Otro"
}

private fun getDangerTypeText(type: DangerType, isEnglish: Boolean): String = when (type) {
    DangerType.FIRE -> if (isEnglish) "Fire" else "Fuego"
    DangerType.COLLAPSE -> if (isEnglish) "Collapse" else "Derrumbe"
    DangerType.FLOOD -> if (isEnglish) "Flood" else "Inundación"
    DangerType.GAS_LEAK -> if (isEnglish) "Gas Leak" else "Fuga de gas"
    DangerType.BLOCKED_ROAD -> if (isEnglish) "Blocked Road" else "Camino bloqueado"
    DangerType.UNSAFE_BUILDING -> if (isEnglish) "Unsafe Building" else "Edificio inseguro"
    DangerType.ELECTRICAL -> if (isEnglish) "Electrical Hazard" else "Peligro eléctrico"
    DangerType.OTHER -> if (isEnglish) "Other" else "Otro"
}
