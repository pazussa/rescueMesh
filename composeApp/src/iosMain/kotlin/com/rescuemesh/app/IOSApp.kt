package com.rescuemesh.app

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import com.rescuemesh.app.platform.*
import com.rescuemesh.app.ai.OfflineAIEngine
import com.rescuemesh.app.ai.SituationSummary
import com.rescuemesh.app.mesh.MeshEngine
import com.rescuemesh.app.mesh.RoomManager
import com.rescuemesh.app.model.*
import com.rescuemesh.app.ui.components.BluetoothWarningBanner
import com.rescuemesh.app.ui.screens.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * iOS App - Shared Compose UI with iOS-specific platform implementations
 */
@Composable
fun IOSApp() {
    val viewModel = remember { IOSViewModel() }
    
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val connectedPeers by viewModel.connectedPeers.collectAsState()
    val isAdvertising by viewModel.isAdvertising.collectAsState()
    val isDiscovering by viewModel.isDiscovering.collectAsState()
    val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsState()
    val situationSummary by viewModel.situationSummary.collectAsState()
    
    Column {
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
                        viewModel.sendSos(category, description, peopleCount)
                        viewModel.navigateTo(Screen.Room)
                    }
                )
            }
            
            is Screen.SendResourceRequest -> {
                SendResourceRequestScreen(
                    onBack = { viewModel.navigateTo(Screen.Room) },
                    onSend = { resourceType, quantity, urgent, description ->
                        viewModel.sendResourceRequest(resourceType, quantity, urgent, description)
                        viewModel.navigateTo(Screen.Room)
                    }
                )
            }
            
            is Screen.SendDangerReport -> {
                SendDangerReportScreen(
                    onBack = { viewModel.navigateTo(Screen.Room) },
                    onSend = { dangerType, severity, description, isBlocking ->
                        viewModel.sendDangerReport(dangerType, severity, description, isBlocking)
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
                        onShareRoom = { },
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
                        discoveredPeers = 0,
                        totalMessages = messages.size,
                        pendingForward = 0,
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
                ShareAppScreen(
                    onBack = { viewModel.navigateTo(Screen.RoomInfo) },
                    onShareBluetooth = { },
                    onShareOther = { },
                    isEmergencyBroadcastActive = false,
                    emergencyBroadcastStatus = "",
                    onStartEmergencyBroadcast = { },
                    onStopEmergencyBroadcast = { }
                )
            }
        }
    }
}

/**
 * Navigation screens
 */
sealed class Screen {
    object Welcome : Screen()
    object CreateRoom : Screen()
    object JoinRoom : Screen()
    object Room : Screen()
    object SendSos : Screen()
    object SendResourceRequest : Screen()
    object SendDangerReport : Screen()
    object RoomInfo : Screen()
    object NetworkStatus : Screen()
    object SituationSummary : Screen()
    object ShareApp : Screen()
}

/**
 * iOS ViewModel - manages state without Android ViewModel dependency
 */
class IOSViewModel {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Platform implementations
    private val storage = LocalStorageFactory().create()
    private val meshTransport = MeshTransportFactory().create()
    private val bluetoothManager = BluetoothManagerFactory().create()
    
    // Engines
    private val meshEngine = MeshEngine()
    private val roomManager = RoomManager()
    private val aiEngine = OfflineAIEngine()
    
    // Device info
    private var deviceId: String = storage.getOrCreateDeviceId()
    
    private val _userName = MutableStateFlow(storage.loadUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    val currentRoom = roomManager.currentRoom
    val messages = meshEngine.messages
    val connectedPeers = meshTransport.connectedPeers
    val isAdvertising = meshTransport.isAdvertising
    val isDiscovering = meshTransport.isDiscovering
    val isBluetoothEnabled = bluetoothManager.isBluetoothEnabled
    
    private val _situationSummary = MutableStateFlow<SituationSummary?>(null)
    val situationSummary: StateFlow<SituationSummary?> = _situationSummary.asStateFlow()
    
    init {
        setupCallbacks()
        bluetoothManager.startMonitoring()
        loadPersistedData()
    }
    
    private fun setupCallbacks() {
        meshEngine.onMessageToForward = { message ->
            meshTransport.broadcastMessage(message)
        }
        
        meshTransport.getLocalInventory = {
            meshEngine.getMessageInventory()
        }
        
        meshTransport.getMessageById = { id ->
            meshEngine.getMessageById(id)
        }
        
        scope.launch {
            for (message in meshTransport.incomingMessages) {
                meshEngine.receiveMessage(message)
            }
        }
    }
    
    private fun loadPersistedData() {
        scope.launch {
            val savedMessages = storage.loadMessages()
            savedMessages.forEach { meshEngine.receiveMessage(it) }
            
            val savedRoom = storage.loadCurrentRoom()
            if (savedRoom != null) {
                roomManager.setCurrentRoom(savedRoom)
                meshTransport.startMesh(deviceId, _userName.value, savedRoom)
                _currentScreen.value = Screen.Room
            }
        }
    }
    
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    
    fun setUserName(name: String) {
        _userName.value = name
        storage.saveUserName(name)
    }
    
    fun createRoom(name: String, description: String) {
        val room = roomManager.createRoom(name, description, _userName.value)
        scope.launch {
            storage.saveCurrentRoom(room)
        }
        meshTransport.startMesh(deviceId, _userName.value, room)
        _currentScreen.value = Screen.Room
    }
    
    fun joinRoom(roomId: String, pin: String, roomName: String) {
        val room = roomManager.joinRoom(roomId, pin, roomName)
        scope.launch {
            storage.saveCurrentRoom(room)
        }
        meshTransport.startMesh(deviceId, _userName.value, room)
        _currentScreen.value = Screen.Room
    }
    
    fun leaveRoom() {
        meshTransport.stopMesh()
        roomManager.leaveRoom()
        meshEngine.clearMessages()
        scope.launch {
            storage.saveCurrentRoom(null)
        }
        _currentScreen.value = Screen.Welcome
    }
    
    fun sendSos(category: SosCategory, description: String, peopleCount: Int) {
        val message = meshEngine.createSosMessage(
            senderId = deviceId,
            senderName = _userName.value,
            roomId = currentRoom.value?.id ?: "",
            category = category,
            description = description,
            peopleCount = peopleCount
        )
        meshEngine.sendMessage(message)
    }
    
    fun sendImOk() {
        val message = meshEngine.createImOkMessage(
            senderId = deviceId,
            senderName = _userName.value,
            roomId = currentRoom.value?.id ?: ""
        )
        meshEngine.sendMessage(message)
    }
    
    fun sendResourceRequest(resourceType: ResourceType, quantity: Int, urgent: Boolean, description: String) {
        val message = meshEngine.createResourceRequestMessage(
            senderId = deviceId,
            senderName = _userName.value,
            roomId = currentRoom.value?.id ?: "",
            resourceType = resourceType,
            quantity = quantity,
            urgent = urgent,
            description = description
        )
        meshEngine.sendMessage(message)
    }
    
    fun sendDangerReport(dangerType: DangerType, severity: Int, description: String, isBlocking: Boolean) {
        val message = meshEngine.createDangerReportMessage(
            senderId = deviceId,
            senderName = _userName.value,
            roomId = currentRoom.value?.id ?: "",
            dangerType = dangerType,
            severity = severity,
            description = description,
            isBlocking = isBlocking
        )
        meshEngine.sendMessage(message)
    }
    
    fun sendChat(text: String) {
        val message = meshEngine.createChatMessage(
            senderId = deviceId,
            senderName = _userName.value,
            roomId = currentRoom.value?.id ?: "",
            text = text
        )
        meshEngine.sendMessage(message)
    }
    
    fun generateSituationSummary(): SituationSummary {
        val summary = aiEngine.generateSituationSummary(messages.value)
        _situationSummary.value = summary
        return summary
    }
    
    fun generateRoomQrData(): String {
        return currentRoom.value?.let { room ->
            "${room.id}|${room.pin}"
        } ?: ""
    }
    
    fun requestInventorySync() {
        meshTransport.requestInventorySync()
    }
    
    fun openBluetoothSettings() {
        bluetoothManager.openBluetoothSettings()
    }
}
