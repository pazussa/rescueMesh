package com.rescuemesh.app.viewmodel

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rescuemesh.app.ai.OfflineAIEngine
import com.rescuemesh.app.ai.SituationSummary
import com.rescuemesh.app.bluetooth.BluetoothStateMonitor
import com.rescuemesh.app.mesh.MeshEngine
import com.rescuemesh.app.mesh.RoomManager
import com.rescuemesh.app.model.*
import com.rescuemesh.app.nearby.NearbyConnectionsManager
import com.rescuemesh.app.persistence.LocalPersistence
import com.rescuemesh.app.sharing.AppSharing
import com.rescuemesh.app.sharing.AutoApkBroadcaster
import com.rescuemesh.app.sharing.EmergencyBroadcaster
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel principal de la aplicación
 */
class RescueMeshViewModel(
    private val context: Context
) : ViewModel() {
    
    companion object {
        private const val TAG = "RescueMeshVM"
        private const val FORWARD_QUEUE_INTERVAL_MS = 2000L  // Procesar cola cada 2s
        private const val INVENTORY_SYNC_INTERVAL_MS = 30000L // Sincronizar cada 30s
        private const val PERSISTENCE_SAVE_INTERVAL_MS = 10000L // Guardar cada 10s
    }
    
    // Managers
    private val meshEngine = MeshEngine()
    private val roomManager = RoomManager()
    private val nearbyManager = NearbyConnectionsManager(context)
    private val persistence = LocalPersistence(context)
    private val bluetoothMonitor = BluetoothStateMonitor(context)
    private val aiEngine = OfflineAIEngine()
    
    // Device info (persistente)
    private var deviceId: String = persistence.getOrCreateDeviceId()
    private var _userName = MutableStateFlow(persistence.loadUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()
    
    // UI State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Welcome)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    // Exponer estados de los managers
    val currentRoom = roomManager.currentRoom
    val messages = meshEngine.messages
    val connectedPeers = nearbyManager.connectedPeers
    val isAdvertising = nearbyManager.isAdvertising
    val isDiscovering = nearbyManager.isDiscovering
    val discoveredPeers = nearbyManager.discoveredPeers
    
    // Estado de Bluetooth
    val isBluetoothEnabled = bluetoothMonitor.isBluetoothEnabled
    val bluetoothState = bluetoothMonitor.bluetoothState
    
    // Estado de error/info
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()
    
    // Resumen de situación por IA
    private val _situationSummary = MutableStateFlow<SituationSummary?>(null)
    val situationSummary: StateFlow<SituationSummary?> = _situationSummary.asStateFlow()
    
    // Estadísticas para diagnóstico
    val pendingForwardCount: Int
        get() = meshEngine.getPendingForwardCount()
    
    init {
        setupMeshCallbacks()
        loadPersistedData()
        startBackgroundTasks()
        bluetoothMonitor.startMonitoring()
    }
    
    /**
     * Configura los callbacks entre MeshEngine y NearbyConnectionsManager
     */
    private fun setupMeshCallbacks() {
        // Callback de reenvío: cuando MeshEngine quiere enviar un mensaje
        meshEngine.onMessageToForward = { message ->
            nearbyManager.broadcastMessage(message)
        }
        
        // Callback de inventario: para sincronización con otros peers
        nearbyManager.getLocalInventory = {
            meshEngine.getMessageInventory()
        }
        
        // Callback para obtener mensaje por ID (para request-missing)
        nearbyManager.getMessageById = { id ->
            meshEngine.getMessageById(id)
        }
        
        // Escuchar mensajes entrantes
        viewModelScope.launch {
            for (message in nearbyManager.incomingMessages) {
                val isNew = meshEngine.processIncomingMessage(message)
                if (isNew) {
                    Log.d(TAG, "Nuevo mensaje recibido: ${message.type} de ${message.senderName}")
                }
            }
        }
    }
    
    /**
     * Carga datos persistidos al iniciar
     */
    private fun loadPersistedData() {
        viewModelScope.launch {
            // Cargar mensajes guardados
            val savedMessages = persistence.loadMessages()
            if (savedMessages.isNotEmpty()) {
                Log.d(TAG, "Cargando ${savedMessages.size} mensajes guardados")
                savedMessages.forEach { meshEngine.processIncomingMessage(it) }
            }
            
            // Cargar IDs vistos
            val seenIds = persistence.loadSeenMessageIds()
            meshEngine.restoreSeenIds(seenIds)
            
            // Restaurar sala si estábamos en una
            val savedRoom = persistence.loadCurrentRoom()
            if (savedRoom != null) {
                Log.d(TAG, "Restaurando sala: ${savedRoom.name}")
                roomManager.joinRoom(savedRoom)
                nearbyManager.startMesh(deviceId, _userName.value, savedRoom)
                _currentScreen.value = Screen.Room
            }
        }
    }
    
    /**
     * Inicia tareas en segundo plano
     */
    private fun startBackgroundTasks() {
        // Tarea: procesar cola de reenvío periódicamente
        viewModelScope.launch {
            while (true) {
                delay(FORWARD_QUEUE_INTERVAL_MS)
                if (currentRoom.value != null) {
                    meshEngine.processForwardQueue()
                }
            }
        }
        
        // Tarea: sincronizar inventario con peers periódicamente
        viewModelScope.launch {
            while (true) {
                delay(INVENTORY_SYNC_INTERVAL_MS)
                if (currentRoom.value != null && nearbyManager.getConnectedPeerCount() > 0) {
                    Log.d(TAG, "Sincronizando inventario con peers...")
                    nearbyManager.broadcastInventory()
                }
            }
        }
        
        // Tarea: persistir datos periódicamente
        viewModelScope.launch {
            while (true) {
                delay(PERSISTENCE_SAVE_INTERVAL_MS)
                saveCurrentState()
            }
        }
    }
    
    /**
     * Guarda el estado actual en persistencia
     */
    private suspend fun saveCurrentState() {
        persistence.saveMessages(meshEngine.messages.value)
        persistence.saveSeenMessageIds(meshEngine.getMessageInventory())
        persistence.saveCurrentRoom(currentRoom.value)
    }
    
    /**
     * Solicita sincronización de inventario a todos los peers
     */
    fun requestInventorySync() {
        viewModelScope.launch {
            Log.d(TAG, "Solicitando sincronización de inventario...")
            nearbyManager.broadcastInventory()
        }
    }
    
    /**
     * Establece el nombre del usuario
     */
    fun setUserName(name: String) {
        _userName.value = name
        persistence.saveUserName(name)
    }
    
    /**
     * Navega a una pantalla
     */
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    
    /**
     * Crea un nuevo Incident Room
     * Automáticamente inicia el broadcast del APK por Bluetooth
     */
    fun createRoom(name: String, description: String) {
        viewModelScope.launch {
            val room = roomManager.createRoom(name, description, deviceId)
            nearbyManager.startMesh(deviceId, _userName.value, room)
            persistence.saveCurrentRoom(room)
            _currentScreen.value = Screen.Room
            _statusMessage.value = "Sala creada: ${room.name}"
            Log.d(TAG, "Sala creada: ${room.id} - ${room.name}")
            
            // Auto-broadcast APK to all nearby Bluetooth devices
            // El nombre del APK sirve como mensaje de emergencia
            delay(1000) // Esperar un segundo para que la sala esté lista
            startAutoApkBroadcast(room.id)
        }
    }
    
    /**
     * Inicia el broadcast automático del APK a todos los dispositivos Bluetooth cercanos
     */
    fun startAutoApkBroadcast(roomCode: String = "") {
        AutoApkBroadcaster.startAutoBroadcast(context, roomCode)
        _statusMessage.value = "📡 Enviando APK de emergencia por Bluetooth..."
    }
    
    /**
     * Detiene el broadcast automático del APK
     */
    fun stopAutoApkBroadcast() {
        AutoApkBroadcaster.stopAutoBroadcast(context)
    }
    
    /**
     * Se une a una sala existente
     */
    fun joinRoom(roomId: String, pin: String, roomName: String) {
        viewModelScope.launch {
            val room = IncidentRoom(
                id = roomId,
                name = roomName,
                pin = pin,
                createdAt = System.currentTimeMillis(),
                creatorId = "",
                description = ""
            )
            roomManager.joinRoom(room)
            nearbyManager.startMesh(deviceId, _userName.value, room)
            persistence.saveCurrentRoom(room)
            _currentScreen.value = Screen.Room
            _statusMessage.value = "Te uniste a: ${room.name}"
            Log.d(TAG, "Unido a sala: ${room.id} - ${room.name}")
        }
    }
    
    /**
     * Procesa datos de QR escaneado
     */
    fun processQrCode(data: String): Boolean {
        val parsed = roomManager.parseQrData(data)
        if (parsed != null) {
            joinRoom(parsed.first, parsed.second, parsed.third)
            return true
        }
        _statusMessage.value = "Código QR inválido"
        return false
    }
    
    /**
     * Genera datos para QR de la sala actual
     */
    fun generateRoomQrData(): String? {
        return currentRoom.value?.let { roomManager.generateQrData(it) }
    }
    
    /**
     * Sale de la sala actual
     */
    fun leaveRoom() {
        viewModelScope.launch {
            Log.d(TAG, "Saliendo de la sala...")
            nearbyManager.stopMesh()
            meshEngine.clear()
            roomManager.leaveRoom()
            persistence.saveCurrentRoom(null)
            _currentScreen.value = Screen.Welcome
        }
    }
    
    /**
     * Envía un SOS
     */
    fun sendSos(category: SosCategory, description: String, peopleCount: Int, latitude: Double?, longitude: Double?) {
        viewModelScope.launch {
            val room = currentRoom.value ?: return@launch
            
            val priority = when (category) {
                SosCategory.MEDICAL, SosCategory.FIRE, SosCategory.CHILDREN, SosCategory.TRAPPED -> MessagePriority.CRITICAL
                else -> MessagePriority.HIGH
            }
            
            val message = MeshMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderId = deviceId,
                senderName = _userName.value,
                type = MessageType.SOS,
                content = MessageContent.Sos(
                    category = category,
                    description = description,
                    peopleCount = peopleCount
                ),
                timestamp = System.currentTimeMillis(),
                priority = priority,
                latitude = latitude,
                longitude = longitude
            )
            
            meshEngine.sendMessage(message)
            _statusMessage.value = "SOS enviado"
        }
    }
    
    /**
     * Envía "Estoy bien"
     */
    fun sendImOk(message: String = "Estoy bien") {
        viewModelScope.launch {
            val room = currentRoom.value ?: return@launch
            
            val meshMessage = MeshMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderId = deviceId,
                senderName = _userName.value,
                type = MessageType.IM_OK,
                content = MessageContent.ImOk(message),
                timestamp = System.currentTimeMillis(),
                priority = MessagePriority.INFO
            )
            
            meshEngine.sendMessage(meshMessage)
            _statusMessage.value = "Estado enviado"
        }
    }
    
    /**
     * Envía solicitud de recursos
     */
    fun sendResourceRequest(
        resourceType: ResourceType,
        quantity: Int,
        urgent: Boolean,
        description: String,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            val room = currentRoom.value ?: return@launch
            
            val message = MeshMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderId = deviceId,
                senderName = _userName.value,
                type = MessageType.RESOURCE_REQUEST,
                content = MessageContent.ResourceRequest(
                    resourceType = resourceType,
                    quantity = quantity,
                    urgent = urgent,
                    description = description
                ),
                timestamp = System.currentTimeMillis(),
                priority = if (urgent) MessagePriority.MEDIUM else MessagePriority.LOW,
                latitude = latitude,
                longitude = longitude
            )
            
            meshEngine.sendMessage(message)
            _statusMessage.value = "Solicitud enviada"
        }
    }
    
    /**
     * Envía reporte de peligro
     */
    fun sendDangerReport(
        dangerType: DangerType,
        severity: Int,
        description: String,
        isBlocking: Boolean,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            val room = currentRoom.value ?: return@launch
            
            val message = MeshMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderId = deviceId,
                senderName = _userName.value,
                type = MessageType.DANGER_REPORT,
                content = MessageContent.DangerReport(
                    dangerType = dangerType,
                    severity = severity,
                    description = description,
                    isBlocking = isBlocking
                ),
                timestamp = System.currentTimeMillis(),
                priority = MessagePriority.MEDIUM,
                latitude = latitude,
                longitude = longitude
            )
            
            meshEngine.sendMessage(message)
            _statusMessage.value = "Reporte enviado"
        }
    }
    
    /**
     * Envía mensaje de chat
     */
    fun sendChat(text: String) {
        viewModelScope.launch {
            val room = currentRoom.value ?: return@launch
            
            val message = MeshMessage(
                id = UUID.randomUUID().toString(),
                roomId = room.id,
                senderId = deviceId,
                senderName = _userName.value,
                type = MessageType.CHAT,
                content = MessageContent.Chat(text),
                timestamp = System.currentTimeMillis(),
                priority = MessagePriority.INFO
            )
            
            meshEngine.sendMessage(message)
        }
    }
    
    /**
     * Limpia mensaje de status
     */
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
    
    /**
     * Obtiene estadísticas
     */
    fun getMeshStats() = meshEngine.getStats()
    
    /**
     * Abre configuración de Bluetooth del sistema
     */
    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo configuración Bluetooth", e)
            _statusMessage.value = "No se pudo abrir la configuración de Bluetooth"
        }
    }
    
    /**
     * Genera resumen de situación usando IA offline
     */
    fun generateSituationSummary(): SituationSummary {
        val currentMessages = messages.value
        val summary = aiEngine.generateSituationSummary(currentMessages)
        _situationSummary.value = summary
        return summary
    }
    
    /**
     * Obtiene mensajes ordenados por prioridad usando IA
     */
    fun getMessagesSortedByAI(): List<MeshMessage> {
        return aiEngine.sortByPriority(messages.value)
    }
    
    /**
     * Calcula puntuación de urgencia de un mensaje
     */
    fun getUrgencyScore(message: MeshMessage): Int {
        return aiEngine.calculateUrgencyScore(message)
    }
    
    /**
     * Translates basic text (Spanish <-> English)
     */
    fun translateBasic(text: String, toEnglish: Boolean): String {
        return aiEngine.translateBasic(text, toEnglish)
    }
    
    // ============================================================
    // App Sharing Functions
    // ============================================================
    
    /**
     * Gets app version information for sharing screen
     */
    fun getAppVersionInfo(): com.rescuemesh.app.sharing.AppVersionInfo {
        return AppSharing.getAppVersionInfo(context)
    }
    
    /**
     * Shares the APK via Bluetooth specifically
     */
    fun shareAppViaBluetooth(): Boolean {
        val intent = AppSharing.createBluetoothShareIntent(context)
        return if (intent != null) {
            try {
                context.startActivity(intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            } catch (e: Exception) {
                Log.w(TAG, "Bluetooth share not available, falling back to general share")
                shareAppViaAny()
            }
        } else {
            false
        }
    }
    
    /**
     * Shares the APK via any available method
     */
    fun shareAppViaAny(): Boolean {
        return AppSharing.shareApp(context)
    }
    
    // ============================================================
    // Emergency Broadcast Functions (HACK)
    // ============================================================
    
    /** State of emergency broadcast */
    val isEmergencyBroadcastActive = EmergencyBroadcaster.isActive
    val emergencyBroadcastStatus = EmergencyBroadcaster.statusMessage
    
    /**
     * Start emergency broadcast - makes device visible to ALL nearby phones
     * via Bluetooth name, WiFi Direct, etc.
     */
    fun startEmergencyBroadcast() {
        val roomCode = currentRoom.value?.id ?: ""
        EmergencyBroadcaster.startEmergencyBroadcast(context, roomCode)
    }
    
    /**
     * Stop emergency broadcast and restore original settings
     */
    fun stopEmergencyBroadcast() {
        EmergencyBroadcaster.stopEmergencyBroadcast()
    }
    
    /**
     * Get explanation of what the broadcast does
     */
    fun getEmergencyBroadcastExplanation(isEnglish: Boolean): String {
        return EmergencyBroadcaster.getBroadcastExplanation(isEnglish)
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel destruido, limpiando...")
        // Guardar estado antes de cerrar
        viewModelScope.launch {
            saveCurrentState()
        }
        nearbyManager.stopMesh()
        bluetoothMonitor.stopMonitoring()
        EmergencyBroadcaster.stopEmergencyBroadcast()  // Stop emergency broadcast
    }
}

/**
 * Pantallas de la app
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
