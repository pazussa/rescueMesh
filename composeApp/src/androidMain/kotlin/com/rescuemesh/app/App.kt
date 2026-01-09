package com.rescuemesh.app

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.rescuemesh.app.ai.SituationSummary
import com.rescuemesh.app.model.IncidentRoom
import com.rescuemesh.app.ui.components.BluetoothWarningBanner
import com.rescuemesh.app.ui.screens.*
import com.rescuemesh.app.viewmodel.RescueMeshViewModel
import com.rescuemesh.app.viewmodel.Screen

@Composable
fun App(viewModel: RescueMeshViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val isAdvertising by viewModel.isAdvertising.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()
    val discoveredPeers by viewModel.discoveredPeers.collectAsState()
    val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsState()
    val situationSummary by viewModel.situationSummary.collectAsState()
    
    Column {
        // Banner de advertencia de Bluetooth (siempre visible si está desactivado)
        BluetoothWarningBanner(
            isBluetoothEnabled = isBluetoothEnabled,
            onEnableBluetooth = { viewModel.openBluetoothSettings() }
        )
        
        when (currentScreen) {
            is Screen.Welcome -> {
                WelcomeScreen(
                    userName = userName,
                    onUserNameChange = { viewModel.setUserName(it) },
                    onCreateRoom = { viewModel.navigateTo(Screen.CreateRoom) },
                    onJoinRoom = { viewModel.navigateTo(Screen.JoinRoom) }
                )
            }
            
            is Screen.CreateRoom -> {
                CreateRoomScreen(
                    onBack = { viewModel.navigateTo(Screen.Welcome) },
                    onCreate = { name, description ->
                        viewModel.createRoom(name, description)
                    }
                )
            }
            
            is Screen.JoinRoom -> {
                JoinRoomScreen(
                    onBack = { viewModel.navigateTo(Screen.Welcome) },
                    onJoin = { roomId, pin, roomName ->
                        viewModel.joinRoom(roomId, pin, roomName)
                    }
                )
            }
            
            is Screen.Room -> {
                currentRoom?.let { room ->
                    RoomScreen(
                        room = room,
                        messages = messages.sortedWith(compareBy({ it.priority.value }, { -it.timestamp })),
                        connectedPeers = connectedPeers.size,
                        isAdvertising = isAdvertising,
                        isDiscovering = isDiscovering,
                        onSendSos = { viewModel.navigateTo(Screen.SendSos) },
                        onSendImOk = { viewModel.sendImOk() },
                        onSendResource = { viewModel.navigateTo(Screen.SendResourceRequest) },
                        onSendDanger = { viewModel.navigateTo(Screen.SendDangerReport) },
                        onSendChat = { text -> viewModel.sendChat(text) },
                        onShowRoomInfo = { viewModel.navigateTo(Screen.RoomInfo) },
                        onShowNetworkStatus = { viewModel.navigateTo(Screen.NetworkStatus) },
                        onShowAISummary = { 
                            viewModel.generateSituationSummary()
                            viewModel.navigateTo(Screen.SituationSummary) 
                        },
                        onLeaveRoom = { viewModel.leaveRoom() }
                    )
                }
            }
            
            is Screen.SendSos -> {
                SendSosScreen(
                    onBack = { viewModel.navigateTo(Screen.Room) },
                    onSend = { category, description, peopleCount ->
                        viewModel.sendSos(category, description, peopleCount, null, null)
                        viewModel.navigateTo(Screen.Room)
                    }
                )
            }
            
            is Screen.SendResourceRequest -> {
                SendResourceRequestScreen(
                    onBack = { viewModel.navigateTo(Screen.Room) },
                    onSend = { resourceType, quantity, urgent, description ->
                        viewModel.sendResourceRequest(resourceType, quantity, urgent, description, null, null)
                        viewModel.navigateTo(Screen.Room)
                    }
                )
            }
            
            is Screen.SendDangerReport -> {
                SendDangerReportScreen(
                    onBack = { viewModel.navigateTo(Screen.Room) },
                    onSend = { dangerType, severity, description, isBlocking ->
                        viewModel.sendDangerReport(dangerType, severity, description, isBlocking, null, null)
                        viewModel.navigateTo(Screen.Room)
                    }
                )
            }
            
            is Screen.RoomInfo -> {
                currentRoom?.let { room ->
                    RoomInfoScreen(
                        room = room,
                        connectedPeers = connectedPeers.size,
                        qrData = viewModel.generateRoomQrData(),
                        onBack = { viewModel.navigateTo(Screen.Room) },
                        onShareRoom = { /* Share functionality */ },
                        onShareApp = { viewModel.navigateTo(Screen.ShareApp) }
                    )
                }
            }
            
            is Screen.NetworkStatus -> {
                currentRoom?.let { room ->
                    NetworkStatusScreen(
                        roomId = room.id,
                        roomName = room.name,
                        isAdvertising = isAdvertising,
                        isDiscovering = isDiscovering,
                        connectedPeers = connectedPeers,
                        discoveredPeers = discoveredPeers.size,
                        totalMessages = messages.size,
                        pendingForward = viewModel.pendingForwardCount,
                        onBack = { viewModel.navigateTo(Screen.Room) },
                        onRefreshInventory = { viewModel.requestInventorySync() }
                    )
                }
            }
            
            is Screen.SituationSummary -> {
                SituationSummaryScreen(
                    summary = situationSummary ?: viewModel.generateSituationSummary(),
                    onBack = { viewModel.navigateTo(Screen.Room) },
                    onRefresh = { viewModel.generateSituationSummary() }
                )
            }
            
            is Screen.ShareApp -> {
                val appVersionInfo = viewModel.getAppVersionInfo()
                val isEmergencyBroadcastActive by viewModel.isEmergencyBroadcastActive.collectAsState()
                val emergencyBroadcastStatus by viewModel.emergencyBroadcastStatus.collectAsState()
                
                ShareAppScreen(
                    appVersionName = appVersionInfo.versionName,
                    appVersionCode = appVersionInfo.versionCode,
                    appSizeMb = appVersionInfo.fileSizeMb,
                    isEmergencyBroadcastActive = isEmergencyBroadcastActive,
                    emergencyBroadcastStatus = emergencyBroadcastStatus,
                    onBack = { viewModel.navigateTo(Screen.Room) },
                    onShareBluetooth = { viewModel.shareAppViaBluetooth() },
                    onShareGeneral = { viewModel.shareAppViaAny() },
                    onStartEmergencyBroadcast = { viewModel.startEmergencyBroadcast() },
                    onStopEmergencyBroadcast = { viewModel.stopEmergencyBroadcast() }
                )
            }
        }
    }
}
