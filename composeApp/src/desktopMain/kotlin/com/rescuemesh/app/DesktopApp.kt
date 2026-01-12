package com.rescuemesh.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.model.*
import com.rescuemesh.app.platform.*
import com.rescuemesh.app.ai.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * RescueMesh Desktop - Centro de Gestión de Emergencias
 * 
 * Esta versión es SOLO para gestión:
 * - NO crea salas (solo Android puede crear)
 * - NO requiere PIN (acceso directo)
 * - Muestra salas activas automáticamente
 * - Un clic para entrar a gestionar
 * 
 * Conexión: UDP Multicast en red local (239.255.42.99:45678)
 * Requiere al menos 1 dispositivo Android conectado al mismo WiFi
 */
@Composable
fun DesktopApp() {
    val scope = rememberCoroutineScope()
    
    // Platform services
    val storage = remember { DesktopLocalStorage() }
    val meshTransport = remember { DesktopMeshTransport() }
    val aiEngine = remember { OfflineAIEngine() }
    
    // State
    var currentScreen by remember { mutableStateOf(DesktopScreen.Dashboard) }
    var currentRoom by remember { mutableStateOf<IncidentRoom?>(null) }
    var messages by remember { mutableStateOf<List<MeshMessage>>(emptyList()) }
    var attendedMessageIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Network state
    val isAdvertising by meshTransport.isAdvertising.collectAsState()
    val isDiscovering by meshTransport.isDiscovering.collectAsState()
    val connectedPeers by meshTransport.connectedPeers.collectAsState()
    val peersCount by meshTransport.discoveredPeersCount.collectAsState()
    val discoveredRooms by meshTransport.discoveredRooms.collectAsState()
    
    // Auto-start discovery on launch
    LaunchedEffect(Unit) {
        val deviceId = storage.getOrCreateDeviceId()
        meshTransport.startDiscovery(deviceId, "Desktop-Coordinador")
    }
    
    // Collect incoming messages when in a room
    LaunchedEffect(meshTransport, currentRoom) {
        if (currentRoom != null) {
            for (message in meshTransport.incomingMessages) {
                if (message.roomId == currentRoom?.id) {
                    messages = (messages + message).distinctBy { it.id }.sortedBy { it.timestamp }
                }
            }
        }
    }
    
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF4CAF50),
            secondary = Color(0xFF2196F3),
            tertiary = Color(0xFFFF9800),
            background = Color(0xFF0D1117),
            surface = Color(0xFF161B22),
            surfaceVariant = Color(0xFF21262D),
            error = Color(0xFFFF5252),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFFC9D1D9),
            onSurface = Color(0xFFC9D1D9)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                DesktopScreen.Dashboard -> {
                    MainDashboard(
                        discoveredRooms = discoveredRooms,
                        isDiscovering = isDiscovering,
                        onJoinRoom = { room ->
                            val deviceId = storage.getOrCreateDeviceId()
                            meshTransport.joinRoom(
                                deviceId = deviceId,
                                deviceName = "Desktop-Coordinador",
                                roomId = room.id,
                                roomName = room.name,
                                roomDescription = room.description
                            )
                            currentRoom = IncidentRoom(
                                id = room.id,
                                name = room.name,
                                description = room.description,
                                creatorId = "unknown",
                                pin = "",
                                createdAt = 0
                            )
                            currentScreen = DesktopScreen.CommandCenter
                        },
                        onRefresh = {
                            scope.launch {
                                val deviceId = storage.getOrCreateDeviceId()
                                meshTransport.stopDiscovery()
                                delay(500)
                                meshTransport.startDiscovery(deviceId, "Desktop-Coordinador")
                            }
                        }
                    )
                }
                
                DesktopScreen.CommandCenter -> {
                    currentRoom?.let { room ->
                        CommandCenterScreen(
                            room = room,
                            messages = messages,
                            attendedIds = attendedMessageIds,
                            userName = "Coordinador",
                            deviceId = storage.getOrCreateDeviceId(),
                            peersCount = peersCount,
                            connectedPeers = connectedPeers,
                            isNetworkActive = isAdvertising && isDiscovering,
                            aiEngine = aiEngine,
                            onMarkAttended = { id -> 
                                attendedMessageIds = attendedMessageIds + id 
                            },
                            onUnmarkAttended = { id ->
                                attendedMessageIds = attendedMessageIds - id
                            },
                            onSendMessage = { msg ->
                                messages = messages + msg
                                meshTransport.broadcastMessage(msg)
                            },
                            onExportLogs = { 
                                exportLogs(messages, room, storage)
                            },
                            onLeaveRoom = {
                                meshTransport.stopMesh()
                                currentRoom = null
                                messages = emptyList()
                                attendedMessageIds = emptySet()
                                currentScreen = DesktopScreen.Dashboard
                                // Restart discovery
                                scope.launch {
                                    delay(500)
                                    val deviceId = storage.getOrCreateDeviceId()
                                    meshTransport.startDiscovery(deviceId, "Desktop-Coordinador")
                                }
                            }
                        )
                    }
                }
                
                // Legacy screens - not used anymore
                else -> {
                    currentScreen = DesktopScreen.Dashboard
                }
            }
        }
    }
}

enum class DesktopScreen {
    Dashboard, CommandCenter,
    // Legacy (not used)
    Welcome, CreateRoom, JoinRoom
}

// ============== MAIN DASHBOARD - ROOM DISCOVERY ==============

@Composable
private fun MainDashboard(
    discoveredRooms: Map<String, DiscoveredRoom>,
    isDiscovering: Boolean,
    onJoinRoom: (DiscoveredRoom) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🆘", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "RescueMesh",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Centro de Gestión de Emergencias",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Network status
                    NetworkStatusIndicator(isDiscovering)
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Refresh button
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Content
        if (discoveredRooms.isEmpty()) {
            // No rooms found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    // Animated scanning indicator
                    val infiniteTransition = rememberInfiniteTransition()
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    
                    Text(
                        text = "📡",
                        fontSize = 64.sp,
                        modifier = Modifier.graphicsLayer(alpha = alpha)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Buscando salas de emergencia...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.widthIn(max = 500.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "¿No aparecen salas?",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            ConnectionHelpItem(
                                emoji = "📱",
                                text = "Al menos un dispositivo Android con RescueMesh debe estar activo"
                            )
                            ConnectionHelpItem(
                                emoji = "📶",
                                text = "Desktop y Android deben estar en el mismo WiFi"
                            )
                            ConnectionHelpItem(
                                emoji = "🔄",
                                text = "El Android actúa como 'gateway' entre el mesh y esta PC"
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Diagram
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🖥️ Desktop ←WiFi→ 📱 Gateway ←Mesh→ 📱📱📱",
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Actualizar búsqueda")
                    }
                }
            }
        } else {
            // Show discovered rooms
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text(
                    text = "Salas de Emergencia Activas",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(discoveredRooms.values.toList().sortedByDescending { it.lastActivity }) { room ->
                        RoomCard(
                            room = room,
                            onJoin = { onJoinRoom(room) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionHelpItem(emoji: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NetworkStatusIndicator(isActive: Boolean) {
    val color = if (isActive) Color(0xFF4CAF50) else Color(0xFF757575)
    val text = if (isActive) "Escuchando red" else "Sin conexión"
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun RoomCard(
    room: DiscoveredRoom,
    onJoin: () -> Unit
) {
    val timeSinceActivity = remember(room.lastActivity) {
        val seconds = (System.currentTimeMillis() - room.lastActivity) / 1000
        when {
            seconds < 10 -> "Ahora"
            seconds < 60 -> "Hace ${seconds}s"
            seconds < 3600 -> "Hace ${seconds / 60}m"
            else -> "Hace ${seconds / 3600}h"
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoin() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚨", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (room.description.isNotEmpty()) {
                            Text(
                                text = room.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Peers count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${room.peersCount} dispositivo${if (room.peersCount != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Last activity
                    Text(
                        text = "• $timeSinceActivity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Join button - one click!
            Button(
                onClick = onJoin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("ENTRAR")
            }
        }
    }
}

// ============== COMMAND CENTER SCREEN ==============

@Composable
private fun CommandCenterScreen(
    room: IncidentRoom,
    messages: List<MeshMessage>,
    attendedIds: Set<String>,
    userName: String,
    deviceId: String,
    peersCount: Int,
    connectedPeers: Map<String, MeshPeer>,
    isNetworkActive: Boolean,
    aiEngine: OfflineAIEngine,
    onMarkAttended: (String) -> Unit,
    onUnmarkAttended: (String) -> Unit,
    onSendMessage: (MeshMessage) -> Unit,
    onExportLogs: () -> Unit,
    onLeaveRoom: () -> Unit
) {
    val situationSummary = remember(messages) { aiEngine.generateSituationSummary(messages) }
    
    // Sort messages by urgency
    val sortedMessages = remember(messages) {
        messages.sortedByDescending { aiEngine.calculateUrgencyScore(it) }
    }
    
    val activeSOSMessages = remember(messages, attendedIds) {
        messages.filter { it.type == MessageType.SOS && it.id !in attendedIds }
    }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT PANEL - Network Status & Stats
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            // Network Status Card
            NetworkStatusCard(
                isActive = isNetworkActive,
                peersCount = peersCount,
                connectedPeers = connectedPeers,
                roomCode = room.id
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Stats
            QuickStatsCard(
                summary = situationSummary,
                attendedCount = attendedIds.size
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Export & Leave buttons
            OutlinedButton(
                onClick = onExportLogs,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Logs")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onLeaveRoom,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salir")
            }
        }
        
        // MAIN CONTENT
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            // Header
            CommandCenterHeader(
                roomName = room.name,
                roomDescription = room.description,
                activeAlerts = activeSOSMessages.size
            )
            
            // Main content area
            Row(modifier = Modifier.weight(1f)) {
                // CENTER - Message Feed (Priority Sorted)
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    // Tab bar for filtering
                    var selectedTab by remember { mutableStateOf(0) }
                    val tabs = listOf("Todos", "SOS Activos", "Peligros", "Recursos", "OK")
                    
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        edgePadding = 8.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontSize = 12.sp) }
                            )
                        }
                    }
                    
                    val filteredMessages = when (selectedTab) {
                        1 -> sortedMessages.filter { it.type == MessageType.SOS && it.id !in attendedIds }
                        2 -> sortedMessages.filter { it.type == MessageType.DANGER_REPORT }
                        3 -> sortedMessages.filter { it.type == MessageType.RESOURCE_REQUEST }
                        4 -> sortedMessages.filter { it.type == MessageType.IM_OK }
                        else -> sortedMessages
                    }
                    
                    // Message list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMessages, key = { it.id }) { message ->
                            CommandCenterMessageCard(
                                message = message,
                                urgencyScore = aiEngine.calculateUrgencyScore(message),
                                isAttended = message.id in attendedIds,
                                onMarkAttended = { onMarkAttended(message.id) },
                                onUnmarkAttended = { onUnmarkAttended(message.id) }
                            )
                        }
                        
                        if (filteredMessages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Sin mensajes en esta categoría",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // RIGHT PANEL - Quick Actions & AI Summary
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    // AI Summary
                    AISummaryCard(summary = situationSummary)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quick Broadcast Actions
                    QuickBroadcastCard(
                        deviceId = deviceId,
                        userName = userName,
                        roomId = room.id,
                        onSendMessage = onSendMessage
                    )
                }
            }
        }
    }
}

// ============== NETWORK STATUS CARD ==============

@Composable
private fun NetworkStatusCard(
    isActive: Boolean,
    peersCount: Int,
    connectedPeers: Map<String, MeshPeer>,
    roomCode: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Animated network indicator
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) Color(0xFF4CAF50).copy(alpha = alpha)
                            else Color(0xFFFF5252)
                        )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = if (isActive) "RED ACTIVA" else "DESCONECTADO",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isActive) Color(0xFF4CAF50) else Color(0xFFFF5252)
                    )
                    Text(
                        text = "UDP Multicast",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Room code
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Código de Sala", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(roomCode, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 2.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Peers count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Dispositivos Conectados", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$peersCount dispositivo${if (peersCount != 1) "s" else ""}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // List connected peers
            if (connectedPeers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                connectedPeers.values.take(5).forEach { peer ->
                    Text(
                        text = "• ${peer.deviceName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
                if (connectedPeers.size > 5) {
                    Text(
                        text = "... y ${connectedPeers.size - 5} más",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }
            }
        }
    }
}

// ============== QUICK STATS CARD ==============

@Composable
private fun QuickStatsCard(
    summary: SituationSummary,
    attendedCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "ESTADÍSTICAS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatRow("Total Mensajes", summary.totalMessages.toString(), Color.White)
            StatRow("SOS Activos", summary.activeSOSCount.toString(), Color(0xFFFF5252))
            StatRow("Críticos", summary.criticalCount.toString(), Color(0xFFFF9800))
            StatRow("Atendidos", attendedCount.toString(), Color(0xFF4CAF50))
            StatRow("Personas afectadas", summary.totalPeopleAffected.toString(), Color(0xFF2196F3))
            StatRow("Confirmados OK", summary.safeCount.toString(), Color(0xFF4CAF50))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, color = color)
    }
}

// ============== HEADER ==============

@Composable
private fun CommandCenterHeader(
    roomName: String,
    roomDescription: String,
    activeAlerts: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (roomDescription.isNotEmpty()) {
                    Text(
                        text = roomDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Active alerts badge
            if (activeAlerts > 0) {
                Surface(
                    color = Color(0xFFFF5252),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$activeAlerts SOS ACTIVOS",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ============== MESSAGE CARD ==============

@Composable
private fun CommandCenterMessageCard(
    message: MeshMessage,
    urgencyScore: Int,
    isAttended: Boolean,
    onMarkAttended: () -> Unit,
    onUnmarkAttended: () -> Unit
) {
    val backgroundColor = when {
        isAttended -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        message.type == MessageType.SOS -> Color(0xFF4A1C1C)
        message.type == MessageType.DANGER_REPORT -> Color(0xFF4A3A1C)
        message.type == MessageType.RESOURCE_REQUEST -> Color(0xFF1C3A4A)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = when {
        isAttended -> Color.Transparent
        urgencyScore >= 70 -> Color(0xFFFF5252)
        urgencyScore >= 50 -> Color(0xFFFF9800)
        else -> Color.Transparent
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (borderColor != Color.Transparent) 
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Type icon and color
            val icon = when (message.type) {
                MessageType.SOS -> Icons.Default.Warning
                MessageType.DANGER_REPORT -> Icons.Default.Warning
                MessageType.RESOURCE_REQUEST -> Icons.Default.ShoppingCart
                MessageType.IM_OK -> Icons.Default.CheckCircle
                MessageType.CHAT -> Icons.Default.Email
            }
            val iconColor = when (message.type) {
                MessageType.SOS -> Color(0xFFFF5252)
                MessageType.DANGER_REPORT -> Color(0xFFFF9800)
                MessageType.RESOURCE_REQUEST -> Color(0xFF2196F3)
                MessageType.IM_OK -> Color(0xFF4CAF50)
                MessageType.CHAT -> Color.Gray
            }
            
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isAttended) iconColor.copy(alpha = 0.5f) else iconColor,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        color = if (isAttended) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                               else MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Urgency badge
                    if (urgencyScore >= 50 && !isAttended) {
                        Surface(
                            color = if (urgencyScore >= 70) Color(0xFFFF5252) else Color(0xFFFF9800),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (urgencyScore >= 70) "CRÍTICO" else "URGENTE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    if (isAttended) {
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ATENDIDO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Content
                val contentText = when (val content = message.content) {
                    is MessageContent.Sos -> "SOS ${content.category}: ${content.description} (${content.peopleCount} personas)"
                    is MessageContent.ImOk -> content.message
                    is MessageContent.ResourceRequest -> "Necesita: ${content.resourceType} x${content.quantity} - ${content.description}"
                    is MessageContent.DangerReport -> "PELIGRO ${content.dangerType}: ${content.description}"
                    is MessageContent.Chat -> content.text
                }
                
                Text(
                    text = contentText,
                    color = if (isAttended) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                           else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Action button
            if (message.type == MessageType.SOS || message.type == MessageType.RESOURCE_REQUEST) {
                IconButton(
                    onClick = { if (isAttended) onUnmarkAttended() else onMarkAttended() }
                ) {
                    Icon(
                        imageVector = if (isAttended) Icons.Default.Refresh else Icons.Default.CheckCircle,
                        contentDescription = if (isAttended) "Desmarcar" else "Marcar atendido",
                        tint = if (isAttended) MaterialTheme.colorScheme.onSurfaceVariant 
                               else Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

// ============== AI SUMMARY CARD ==============

@Composable
private fun AISummaryCard(summary: SituationSummary) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "RESUMEN IA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF9C27B0)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = summary.summaryText,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            
            if (summary.dangerZones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Zonas de Peligro:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFFF9800)
                )
                summary.dangerZones.take(3).forEach { zone ->
                    Text(
                        "• ${zone.type}: ${zone.description.take(40)}...",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============== QUICK BROADCAST CARD ==============

@Composable
private fun QuickBroadcastCard(
    deviceId: String,
    userName: String,
    roomId: String,
    onSendMessage: (MeshMessage) -> Unit
) {
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastText by remember { mutableStateOf("") }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "ACCIONES RÁPIDAS",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Broadcast button
            Button(
                onClick = { showBroadcastDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar Comunicado")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quick status buttons
            OutlinedButton(
                onClick = {
                    val msg = MeshMessage(
                        id = randomUUID(),
                        type = MessageType.CHAT,
                        senderId = deviceId,
                        senderName = "$userName (Coord)",
                        roomId = roomId,
                        content = MessageContent.Chat("[COORDINACIÓN] Recibiendo reportes. Mantengan la calma."),
                        timestamp = currentTimeMillis(),
                        priority = MessagePriority.MEDIUM
                    )
                    onSendMessage(msg)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Recepción", fontSize = 12.sp)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            OutlinedButton(
                onClick = {
                    val msg = MeshMessage(
                        id = randomUUID(),
                        type = MessageType.CHAT,
                        senderId = deviceId,
                        senderName = "$userName (Coord)",
                        roomId = roomId,
                        content = MessageContent.Chat("[COORDINACIÓN] Ayuda en camino. Manténganse en posición segura."),
                        timestamp = currentTimeMillis(),
                        priority = MessagePriority.HIGH
                    )
                    onSendMessage(msg)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ayuda en Camino", fontSize = 12.sp)
            }
        }
    }
    
    // Broadcast dialog
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("Enviar Comunicado") },
            text = {
                OutlinedTextField(
                    value = broadcastText,
                    onValueChange = { broadcastText = it },
                    label = { Text("Mensaje para todos") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (broadcastText.isNotBlank()) {
                            val msg = MeshMessage(
                                id = randomUUID(),
                                type = MessageType.CHAT,
                                senderId = deviceId,
                                senderName = "$userName (Coord)",
                                roomId = roomId,
                                content = MessageContent.Chat("[COMUNICADO] $broadcastText"),
                                timestamp = currentTimeMillis(),
                                priority = MessagePriority.HIGH
                            )
                            onSendMessage(msg)
                            broadcastText = ""
                            showBroadcastDialog = false
                        }
                    }
                ) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ============== UTILITY FUNCTIONS ==============

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        ""
    }
}

private fun exportLogs(messages: List<MeshMessage>, room: IncidentRoom, storage: DesktopLocalStorage) {
    try {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
        val fileName = "RescueMesh_${room.id}_$timestamp.txt"
        val desktop = File(System.getProperty("user.home"), "Desktop")
        val file = File(if (desktop.exists()) desktop else File(System.getProperty("user.home")), fileName)
        
        val content = buildString {
            appendLine("=" .repeat(60))
            appendLine("RESCUEMESH - LOG DE INCIDENTE")
            appendLine("=" .repeat(60))
            appendLine()
            appendLine("Sala: ${room.name} (${room.id})")
            appendLine("Descripción: ${room.description}")
            appendLine("Exportado: $timestamp")
            appendLine("Total mensajes: ${messages.size}")
            appendLine()
            appendLine("-".repeat(60))
            appendLine()
            
            messages.sortedBy { it.timestamp }.forEach { msg ->
                val time = formatTimestamp(msg.timestamp)
                val type = msg.type.name
                val sender = msg.senderName
                val content = when (val c = msg.content) {
                    is MessageContent.Sos -> "SOS ${c.category}: ${c.description} (${c.peopleCount} personas)"
                    is MessageContent.ImOk -> "OK: ${c.message}"
                    is MessageContent.ResourceRequest -> "RECURSO: ${c.resourceType} x${c.quantity} - ${c.description}"
                    is MessageContent.DangerReport -> "PELIGRO: ${c.dangerType} - ${c.description}"
                    is MessageContent.Chat -> c.text
                }
                
                appendLine("[$time] [$type] $sender")
                appendLine("   $content")
                appendLine()
            }
            
            appendLine("-".repeat(60))
            appendLine("FIN DEL LOG")
        }
        
        file.writeText(content)
        println("[RescueMesh] Log exported to: ${file.absolutePath}")
        
    } catch (e: Exception) {
        println("[RescueMesh] Error exporting logs: ${e.message}")
        e.printStackTrace()
    }
}
