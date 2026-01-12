package com.rescuemesh.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.model.MeshPeer
import com.rescuemesh.app.ui.theme.RescueMeshColors

/**
 * Pantalla de estado de Nearby Connections
 * Muestra evidencia visual del funcionamiento de la red mesh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkStatusScreen(
    roomId: String,
    roomName: String,
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    connectedPeers: Map<String, MeshPeer>,
    discoveredPeers: Int,
    totalMessages: Int,
    pendingForward: Int,
    onBack: () -> Unit,
    onRefreshInventory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mesh Network Status") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshInventory) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Surface,
                    titleContentColor = RescueMeshColors.OnSurface,
                    navigationIconContentColor = RescueMeshColors.OnSurface
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
            // Header con animación de estado
            item {
                NetworkStatusHeader(
                    isAdvertising = isAdvertising,
                    isDiscovering = isDiscovering
                )
            }
            
            // Información de la sala
            item {
                RoomInfoCard(roomId = roomId, roomName = roomName)
            }
            
            // Estado de Nearby Connections
            item {
                NearbyConnectionsStatusCard(
                    isAdvertising = isAdvertising,
                    isDiscovering = isDiscovering,
                    connectedCount = connectedPeers.size,
                    discoveredCount = discoveredPeers
                )
            }
            
            // Estadísticas del Mesh
            item {
                MeshStatsCard(
                    totalMessages = totalMessages,
                    pendingForward = pendingForward,
                    connectedPeers = connectedPeers.size
                )
            }
            
            // Lista de peers conectados
            item {
                Text(
                    text = "Connected Devices (${connectedPeers.size})",
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnBackground,
                    fontSize = 16.sp
                )
            }
            
            if (connectedPeers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = RescueMeshColors.Surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📡",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Searching for nearby devices...",
                                color = RescueMeshColors.OnSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Make sure other devices have\nthe app open in the same room",
                                color = RescueMeshColors.OnSurface.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(connectedPeers.entries.toList()) { (endpointId, peer) ->
                    PeerCard(peer = peer, endpointId = endpointId)
                }
            }
            
            // Explicación técnica
            item {
                TechnicalInfoCard()
            }
        }
    }
}

@Composable
private fun NetworkStatusHeader(
    isAdvertising: Boolean,
    isDiscovering: Boolean
) {
    val isActive = isAdvertising || isDiscovering
    
    // Animación de pulso cuando está activo
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val statusColor by animateColorAsState(
        targetValue = if (isActive) RescueMeshColors.Success else RescueMeshColors.Warning
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador animado
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(if (isActive) scale else 1f)
                    .clip(CircleShape)
                    .background(statusColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isActive) "📡" else "⏳",
                    fontSize = 28.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = if (isActive) "NEARBY CONNECTIONS ACTIVE" else "STARTING...",
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontSize = 16.sp
                )
                Text(
                    text = if (isActive) 
                        "Mesh network running correctly" 
                    else 
                        "Waiting for permissions or connection",
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun RoomInfoCard(roomId: String, roomName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Active Room",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.Primary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = roomName,
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnSurface,
                fontSize = 18.sp
            )
            Text(
                text = "ID: $roomId",
                fontFamily = FontFamily.Monospace,
                color = RescueMeshColors.OnSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NearbyConnectionsStatusCard(
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    connectedCount: Int,
    discoveredCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = " Google Nearby Connections API",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnSurface,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Estado de Advertising
            StatusRow(
                label = "Advertising (visible to others)",
                isActive = isAdvertising,
                description = if (isAdvertising) "Other devices can find you" else "Not visible"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Estado de Discovery
            StatusRow(
                label = "Discovery (searching for others)",
                isActive = isDiscovering,
                description = if (isDiscovering) "Searching for nearby devices" else "Not searching"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = RescueMeshColors.OnSurface.copy(alpha = 0.2f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = connectedCount.toString(),
                    label = "Connected",
                    color = RescueMeshColors.Success
                )
                StatItem(
                    value = discoveredCount.toString(),
                    label = "Discovered",
                    color = RescueMeshColors.Info
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    isActive: Boolean,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isActive) RescueMeshColors.Success else RescueMeshColors.Danger)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = RescueMeshColors.OnSurface,
                fontSize = 13.sp
            )
            Text(
                text = description,
                color = RescueMeshColors.OnSurface.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = color,
            fontSize = 28.sp
        )
        Text(
            text = label,
            color = RescueMeshColors.OnSurface.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MeshStatsCard(
    totalMessages: Int,
    pendingForward: Int,
    connectedPeers: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Mesh Statistics",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnSurface,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalMessages.toString(),
                    label = "Messages",
                    color = RescueMeshColors.Primary
                )
                StatItem(
                    value = pendingForward.toString(),
                    label = "Pending forward",
                    color = RescueMeshColors.Warning
                )
                StatItem(
                    value = (connectedPeers * 100).toString() + "m",
                    label = "Approx. range",
                    color = RescueMeshColors.Info
                )
            }
        }
    }
}

@Composable
private fun PeerCard(
    peer: MeshPeer,
    endpointId: String
) {
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
            // Avatar con indicador de estado
            Box {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(RescueMeshColors.Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = peer.deviceName.firstOrNull()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.Primary,
                        fontSize = 20.sp
                    )
                }
                // Indicador de conexión
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(RescueMeshColors.Success)
                        .border(2.dp, RescueMeshColors.Surface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peer.deviceName,
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnSurface,
                    fontSize = 15.sp
                )
                Text(
                    text = "Endpoint: ${endpointId.take(8)}...",
                    fontFamily = FontFamily.Monospace,
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }
            
            // Estado de conexión
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "✓ Connected",
                    color = RescueMeshColors.Success,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "via Nearby",
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun TechnicalInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = RescueMeshColors.Info.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ℹ️ How Nearby Connections Works",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.Info,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val technicalInfo = listOf(
                "📶 Uses WiFi Direct, Bluetooth and BLE automatically",
                "🔒 End-to-end encrypted connections",
                "📡 Range: ~100m per hop (without obstacles)",
                "🔄 Multi-hop: messages are forwarded automatically",
                "💾 Store-and-forward: messages persist locally",
                "🔗 Strategy: P2P_CLUSTER (multiple connections)"
            )
            
            technicalInfo.forEach { info ->
                Text(
                    text = info,
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
