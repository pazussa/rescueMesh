package com.rescuemesh.app.localization

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Language Manager - Handles app localization
 * Default: English, Optional: Spanish
 */
object LanguageManager {
    
    enum class Language(val code: String, val displayName: String) {
        ENGLISH("en", "English"),
        SPANISH("es", "Español")
    }
    
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()
    
    fun setLanguage(language: Language) {
        _currentLanguage.value = language
    }
    
    fun toggleLanguage() {
        _currentLanguage.value = when (_currentLanguage.value) {
            Language.ENGLISH -> Language.SPANISH
            Language.SPANISH -> Language.ENGLISH
        }
    }
}

/**
 * Composable to provide current strings based on language
 */
@Composable
fun rememberStrings(): Strings {
    val language by LanguageManager.currentLanguage.collectAsState()
    return when (language) {
        LanguageManager.Language.ENGLISH -> EnglishStrings
        LanguageManager.Language.SPANISH -> SpanishStrings
    }
}

/**
 * String resources interface
 */
interface Strings {
    // App
    val appName: String
    val appTagline: String
    
    // Welcome Screen
    val yourName: String
    val createIncidentRoom: String
    val joinIncidentRoom: String
    val enterYourName: String
    
    // Create Room Screen
    val createRoom: String
    val roomName: String
    val roomDescription: String
    val pin: String
    val create: String
    val back: String
    val roomNameHint: String
    val descriptionHint: String
    val pinHint: String
    
    // Join Room Screen
    val joinRoom: String
    val roomCode: String
    val roomCodeHint: String
    val pinHintJoin: String
    val join: String
    val manualEntry: String
    
    // Room Screen
    val connected: String
    val connectedPeers: String
    val sendSos: String
    val imOk: String
    val requestResources: String
    val reportDanger: String
    val noMessages: String
    val noMessagesYet: String
    val sendFirstMessage: String
    val leaveRoom: String
    val roomInfo: String
    val networkStatus: String
    val aiSummary: String
    val menu: String
    
    // SOS Screen
    val sosTitle: String
    val sosCategory: String
    val description: String
    val peopleAffected: String
    val person: String
    val people: String
    val send: String
    
    // SOS Categories
    val sosMedical: String
    val sosFire: String
    val sosTrapped: String
    val sosChildren: String
    val sosElderly: String
    val sosInjured: String
    val sosOther: String
    
    // Resource Request Screen
    val resourceRequest: String
    val resourceType: String
    val quantity: String
    val urgent: String
    val requestDescription: String
    
    // Resource Types
    val resourceWater: String
    val resourceFood: String
    val resourceMedicine: String
    val resourceFirstAid: String
    val resourceTransport: String
    val resourceShelter: String
    val resourceBlankets: String
    val resourceFlashlight: String
    val resourceBattery: String
    val resourceOther: String
    
    // Danger Report Screen
    val dangerReport: String
    val dangerType: String
    val severity: String
    val blocksPassage: String
    val dangerDescription: String
    
    // Danger Types
    val dangerFire: String
    val dangerCollapse: String
    val dangerGasLeak: String
    val dangerFlood: String
    val dangerElectrical: String
    val dangerBlockedRoad: String
    val dangerUnsafeBuilding: String
    val dangerOther: String
    
    // Severity
    val severityLow: String
    val severityMedium: String
    val severityHigh: String
    
    // Network Status Screen
    val networkStatusTitle: String
    val meshStatus: String
    val advertising: String
    val discovering: String
    val active: String
    val inactive: String
    val connectedDevices: String
    val devicesNearby: String
    val messagesInNetwork: String
    val pendingForward: String
    val refreshInventory: String
    
    // AI Summary Screen
    val situationSummary: String
    val refresh: String
    val criticalEmergencies: String
    val activeSOS: String
    val peopleAffectedSummary: String
    val reportedDangers: String
    val confirmedSafe: String
    val urgentResources: String
    val quickStats: String
    val dangerZones: String
    val resourceNeeds: String
    val noReportsYet: String
    val networkActive: String
    
    // Bluetooth Warning
    val bluetoothDisabled: String
    val enableBluetooth: String
    val tapToEnable: String
    
    // Room Info
    val shareRoom: String
    val roomCodeLabel: String
    val pinLabel: String
    val createdBy: String
    val roomInfoTitle: String
    val share: String
    val meshActive: String
    val shareAppButton: String
    val joinInstructionsTitle: String
    val joinInstructionsBody: String
    
    // Share App Screen
    val shareAppTitle: String
    val shareAppDescription: String
    val shareViaBluetooth: String
    val shareViaOther: String
    val appVersion: String
    val appSize: String
    val shareAppInstructions: String
    val shareAppNote: String
    
    // Message Types
    val messageTypeSos: String
    val messageTypeImOk: String
    val messageTypeResource: String
    val messageTypeDanger: String
    val messageTypeChat: String
    
    // Priority
    val priorityCritical: String
    val priorityHigh: String
    val priorityMedium: String
    val priorityLow: String
    
    // Time
    val justNow: String
    val minutesAgo: String
    val hoursAgo: String
    
    // Settings
    val settings: String
    val language: String
    val changeLanguage: String
    
    // Common
    val cancel: String
    val confirm: String
    val ok: String
    val yes: String
    val no: String
    val error: String
    val loading: String
    
    companion object {
        /**
         * Get string by key for dynamic access
         */
        fun get(key: String): String {
            val strings = when (LanguageManager.currentLanguage.value) {
                LanguageManager.Language.ENGLISH -> EnglishStrings
                LanguageManager.Language.SPANISH -> SpanishStrings
            }
            return when (key) {
                "app_name" -> strings.appName
                "app_tagline" -> strings.appTagline
                "your_name" -> strings.yourName
                "create_incident_room" -> strings.createIncidentRoom
                "join_incident_room" -> strings.joinIncidentRoom
                "enter_your_name" -> strings.enterYourName
                "create_room" -> strings.createRoom
                "room_name" -> strings.roomName
                "room_description" -> strings.roomDescription
                "pin" -> strings.pin
                "create" -> strings.create
                "back" -> strings.back
                "room_name_hint" -> strings.roomNameHint
                "description_hint" -> strings.descriptionHint
                "pin_hint" -> strings.pinHint
                "join_room" -> strings.joinRoom
                "room_code" -> strings.roomCode
                "room_code_hint" -> strings.roomCodeHint
                "pin_hint_join" -> strings.pinHintJoin
                "join" -> strings.join
                "manual_entry" -> strings.manualEntry
                "connected" -> strings.connected
                "connected_peers" -> strings.connectedPeers
                "send_sos" -> strings.sendSos
                "im_ok" -> strings.imOk
                "request_resources" -> strings.requestResources
                "report_danger" -> strings.reportDanger
                "no_messages" -> strings.noMessages
                "no_messages_yet" -> strings.noMessagesYet
                "send_first_message" -> strings.sendFirstMessage
                "leave_room" -> strings.leaveRoom
                "room_info" -> strings.roomInfo
                "room_info_title" -> strings.roomInfoTitle
                "network_status" -> strings.networkStatus
                "ai_summary" -> strings.aiSummary
                "menu" -> strings.menu
                "share" -> strings.share
                "mesh_active" -> strings.meshActive
                "share_app_button" -> strings.shareAppButton
                "join_instructions_title" -> strings.joinInstructionsTitle
                "join_instructions_body" -> strings.joinInstructionsBody
                "share_app_title" -> strings.shareAppTitle
                "share_app_description" -> strings.shareAppDescription
                "share_via_bluetooth" -> strings.shareViaBluetooth
                "share_via_other" -> strings.shareViaOther
                "app_version" -> strings.appVersion
                "app_size" -> strings.appSize
                "share_app_instructions" -> strings.shareAppInstructions
                "share_app_note" -> strings.shareAppNote
                "cancel" -> strings.cancel
                "confirm" -> strings.confirm
                "ok" -> strings.ok
                "yes" -> strings.yes
                "no" -> strings.no
                "error" -> strings.error
                "loading" -> strings.loading
                else -> key // Fallback to key if not found
            }
        }
    }
}

/**
 * English Strings (Default)
 */
object EnglishStrings : Strings {
    // App
    override val appName = "RescueMesh"
    override val appTagline = "Offline emergency network"
    
    // Welcome Screen
    override val yourName = "Your name"
    override val createIncidentRoom = "Create Incident Room"
    override val joinIncidentRoom = "Join Incident Room"
    override val enterYourName = "Enter your name to continue"
    
    // Create Room Screen
    override val createRoom = "Create Room"
    override val roomName = "Room Name"
    override val roomDescription = "Description"
    override val pin = "PIN"
    override val create = "Create"
    override val back = "Back"
    override val roomNameHint = "e.g., Building A Emergency"
    override val descriptionHint = "Optional description"
    override val pinHint = "4-digit PIN"
    
    // Join Room Screen
    override val joinRoom = "Join Room"
    override val roomCode = "Room Code"
    override val roomCodeHint = "Enter room code"
    override val pinHintJoin = "Enter PIN"
    override val join = "Join"
    override val manualEntry = "Manual Entry"
    
    // Room Screen
    override val connected = "connected"
    override val connectedPeers = "connected"
    override val sendSos = "SOS"
    override val imOk = "I'm OK"
    override val requestResources = "Request"
    override val reportDanger = "Danger"
    override val noMessages = "No messages"
    override val noMessagesYet = "No messages yet"
    override val sendFirstMessage = "Send the first message!"
    override val leaveRoom = "Leave Room"
    override val roomInfo = "Room Info"
    override val networkStatus = "Network Status"
    override val aiSummary = "AI Summary"
    override val menu = "Menu"
    
    // SOS Screen
    override val sosTitle = "Send SOS"
    override val sosCategory = "Category"
    override val description = "Description"
    override val peopleAffected = "People affected"
    override val person = "person"
    override val people = "people"
    override val send = "Send"
    
    // SOS Categories
    override val sosMedical = "Medical Emergency"
    override val sosFire = "Fire"
    override val sosTrapped = "Trapped"
    override val sosChildren = "Children in Danger"
    override val sosElderly = "Elderly in Danger"
    override val sosInjured = "Injured"
    override val sosOther = "Other"
    
    // Resource Request Screen
    override val resourceRequest = "Request Resources"
    override val resourceType = "Resource Type"
    override val quantity = "Quantity"
    override val urgent = "Urgent"
    override val requestDescription = "Description"
    
    // Resource Types
    override val resourceWater = "Water"
    override val resourceFood = "Food"
    override val resourceMedicine = "Medicine"
    override val resourceFirstAid = "First Aid Kit"
    override val resourceTransport = "Transport"
    override val resourceShelter = "Shelter"
    override val resourceBlankets = "Blankets"
    override val resourceFlashlight = "Flashlight"
    override val resourceBattery = "Battery"
    override val resourceOther = "Other"
    
    // Danger Report Screen
    override val dangerReport = "Report Danger"
    override val dangerType = "Danger Type"
    override val severity = "Severity"
    override val blocksPassage = "Blocks passage"
    override val dangerDescription = "Description"
    
    // Danger Types
    override val dangerFire = "Fire"
    override val dangerCollapse = "Collapse"
    override val dangerGasLeak = "Gas Leak"
    override val dangerFlood = "Flood"
    override val dangerElectrical = "Electrical Hazard"
    override val dangerBlockedRoad = "Blocked Road"
    override val dangerUnsafeBuilding = "Unsafe Building"
    override val dangerOther = "Other"
    
    // Severity
    override val severityLow = "Low"
    override val severityMedium = "Medium"
    override val severityHigh = "High"
    
    // Network Status Screen
    override val networkStatusTitle = "Network Status"
    override val meshStatus = "Mesh Status"
    override val advertising = "Advertising"
    override val discovering = "Discovering"
    override val active = "Active"
    override val inactive = "Inactive"
    override val connectedDevices = "Connected Devices"
    override val devicesNearby = "Devices Nearby"
    override val messagesInNetwork = "Messages in Network"
    override val pendingForward = "Pending Forward"
    override val refreshInventory = "Sync Inventory"
    
    // AI Summary Screen
    override val situationSummary = "Situation Summary"
    override val refresh = "Refresh"
    override val criticalEmergencies = "CRITICAL EMERGENCIES"
    override val activeSOS = "Active SOS"
    override val peopleAffectedSummary = "people affected"
    override val reportedDangers = "Dangers Reported"
    override val confirmedSafe = "Confirmed Safe"
    override val urgentResources = "urgent resources"
    override val quickStats = "Quick Stats"
    override val dangerZones = "Danger Zones"
    override val resourceNeeds = "Resource Needs"
    override val noReportsYet = "No reports yet"
    override val networkActive = "Network is active"
    
    // Bluetooth Warning
    override val bluetoothDisabled = "Bluetooth Disabled"
    override val enableBluetooth = "Enable Bluetooth"
    override val tapToEnable = "Tap to enable"
    
    // Room Info
    override val shareRoom = "Share Room"
    override val roomCodeLabel = "Room Code"
    override val pinLabel = "PIN"
    override val createdBy = "Created by"
    override val roomInfoTitle = "Room Info"
    override val share = "Share"
    override val meshActive = "Mesh active"
    override val shareAppButton = "Share App with Others"
    override val joinInstructionsTitle = " For others to join:"
    override val joinInstructionsBody = "1. Share the code and PIN\n2. Or show QR code\n3. Make sure to be nearby (~100m)"
    
    // Share App Screen
    override val shareAppTitle = "Share RescueMesh"
    override val shareAppDescription = "Share the app so others can join the emergency network"
    override val shareViaBluetooth = "Share via Bluetooth"
    override val shareViaOther = "Share via Other Apps"
    override val appVersion = "Version"
    override val appSize = "Size"
    override val shareAppInstructions = "1. Select a nearby device\n2. Accept the file transfer\n3. Install the APK\n4. Enable 'Unknown sources' if needed"
    override val shareAppNote = "The receiving device must allow app installation from unknown sources"
    
    // Message Types
    override val messageTypeSos = "SOS"
    override val messageTypeImOk = "I'm OK"
    override val messageTypeResource = "Resource Request"
    override val messageTypeDanger = "Danger Report"
    override val messageTypeChat = "Message"
    
    // Priority
    override val priorityCritical = "CRITICAL"
    override val priorityHigh = "HIGH"
    override val priorityMedium = "MEDIUM"
    override val priorityLow = "LOW"
    
    // Time
    override val justNow = "Just now"
    override val minutesAgo = "min ago"
    override val hoursAgo = "h ago"
    
    // Settings
    override val settings = "Settings"
    override val language = "Language"
    override val changeLanguage = "Change Language"
    
    // Common
    override val cancel = "Cancel"
    override val confirm = "Confirm"
    override val ok = "OK"
    override val yes = "Yes"
    override val no = "No"
    override val error = "Error"
    override val loading = "Loading..."
}

/**
 * Spanish Strings
 */
object SpanishStrings : Strings {
    // App
    override val appName = "RescueMesh"
    override val appTagline = "Red de emergencia offline"
    
    // Welcome Screen
    override val yourName = "Tu nombre"
    override val createIncidentRoom = "Crear Sala de Incidente"
    override val joinIncidentRoom = "Unirse a Sala"
    override val enterYourName = "Ingresa tu nombre para continuar"
    
    // Create Room Screen
    override val createRoom = "Crear Sala"
    override val roomName = "Nombre de la Sala"
    override val roomDescription = "Descripción"
    override val pin = "PIN"
    override val create = "Crear"
    override val back = "Atrás"
    override val roomNameHint = "ej., Emergencia Edificio A"
    override val descriptionHint = "Descripción opcional"
    override val pinHint = "PIN de 4 dígitos"
    
    // Join Room Screen
    override val joinRoom = "Unirse a Sala"
    override val roomCode = "Código de Sala"
    override val roomCodeHint = "Ingresa el código"
    override val pinHintJoin = "Ingresa el PIN"
    override val join = "Unirse"
    override val manualEntry = "Entrada Manual"
    
    // Room Screen
    override val connected = "conectados"
    override val connectedPeers = "conectados"
    override val sendSos = "SOS"
    override val imOk = "Estoy Bien"
    override val requestResources = "Solicitar"
    override val reportDanger = "Peligro"
    override val noMessages = "Sin mensajes"
    override val noMessagesYet = "Aún no hay mensajes"
    override val sendFirstMessage = "¡Envía el primer mensaje!"
    override val leaveRoom = "Salir de la Sala"
    override val roomInfo = "Info de Sala"
    override val networkStatus = "Estado de Red"
    override val aiSummary = "Resumen IA"
    override val menu = "Menú"
    
    // SOS Screen
    override val sosTitle = "Enviar SOS"
    override val sosCategory = "Categoría"
    override val description = "Descripción"
    override val peopleAffected = "Personas afectadas"
    override val person = "persona"
    override val people = "personas"
    override val send = "Enviar"
    
    // SOS Categories
    override val sosMedical = "Emergencia Médica"
    override val sosFire = "Incendio"
    override val sosTrapped = "Atrapado"
    override val sosChildren = "Niños en Peligro"
    override val sosElderly = "Ancianos en Peligro"
    override val sosInjured = "Herido"
    override val sosOther = "Otro"
    
    // Resource Request Screen
    override val resourceRequest = "Solicitar Recursos"
    override val resourceType = "Tipo de Recurso"
    override val quantity = "Cantidad"
    override val urgent = "Urgente"
    override val requestDescription = "Descripción"
    
    // Resource Types
    override val resourceWater = "Agua"
    override val resourceFood = "Comida"
    override val resourceMedicine = "Medicina"
    override val resourceFirstAid = "Botiquín"
    override val resourceTransport = "Transporte"
    override val resourceShelter = "Refugio"
    override val resourceBlankets = "Mantas"
    override val resourceFlashlight = "Linterna"
    override val resourceBattery = "Batería"
    override val resourceOther = "Otro"
    
    // Danger Report Screen
    override val dangerReport = "Reportar Peligro"
    override val dangerType = "Tipo de Peligro"
    override val severity = "Severidad"
    override val blocksPassage = "Bloquea el paso"
    override val dangerDescription = "Descripción"
    
    // Danger Types
    override val dangerFire = "Incendio"
    override val dangerCollapse = "Derrumbe"
    override val dangerGasLeak = "Fuga de Gas"
    override val dangerFlood = "Inundación"
    override val dangerElectrical = "Peligro Eléctrico"
    override val dangerBlockedRoad = "Vía Bloqueada"
    override val dangerUnsafeBuilding = "Edificio Inseguro"
    override val dangerOther = "Otro"
    
    // Severity
    override val severityLow = "Baja"
    override val severityMedium = "Media"
    override val severityHigh = "Alta"
    
    // Network Status Screen
    override val networkStatusTitle = "Estado de la Red"
    override val meshStatus = "Estado Mesh"
    override val advertising = "Anunciando"
    override val discovering = "Descubriendo"
    override val active = "Activo"
    override val inactive = "Inactivo"
    override val connectedDevices = "Dispositivos Conectados"
    override val devicesNearby = "Dispositivos Cercanos"
    override val messagesInNetwork = "Mensajes en la Red"
    override val pendingForward = "Pendientes de Reenvío"
    override val refreshInventory = "Sincronizar Inventario"
    
    // AI Summary Screen
    override val situationSummary = "Resumen de Situación"
    override val refresh = "Actualizar"
    override val criticalEmergencies = "EMERGENCIAS CRÍTICAS"
    override val activeSOS = "SOS Activos"
    override val peopleAffectedSummary = "personas afectadas"
    override val reportedDangers = "Peligros Reportados"
    override val confirmedSafe = "Confirmados a Salvo"
    override val urgentResources = "recursos urgentes"
    override val quickStats = "Estadísticas Rápidas"
    override val dangerZones = "Zonas de Peligro"
    override val resourceNeeds = "Necesidades de Recursos"
    override val noReportsYet = "Sin reportes aún"
    override val networkActive = "La red está activa"
    
    // Bluetooth Warning
    override val bluetoothDisabled = "Bluetooth Desactivado"
    override val enableBluetooth = "Activar Bluetooth"
    override val tapToEnable = "Toca para activar"
    
    // Room Info
    override val shareRoom = "Compartir Sala"
    override val roomCodeLabel = "Código de Sala"
    override val pinLabel = "PIN"
    override val createdBy = "Creado por"
    override val roomInfoTitle = "Info de Sala"
    override val share = "Compartir"
    override val meshActive = "Mesh activo"
    override val shareAppButton = "Compartir App con Otros"
    override val joinInstructionsTitle = " Para que otros se unan:"
    override val joinInstructionsBody = "1. Comparte el código y PIN\n2. O muestra el código QR\n3. Asegúrate de estar cerca (~100m)"
    
    // Share App Screen
    override val shareAppTitle = "Compartir RescueMesh"
    override val shareAppDescription = "Comparte la app para que otros puedan unirse a la red de emergencia"
    override val shareViaBluetooth = "Compartir por Bluetooth"
    override val shareViaOther = "Compartir por Otras Apps"
    override val appVersion = "Versión"
    override val appSize = "Tamaño"
    override val shareAppInstructions = "1. Selecciona un dispositivo cercano\n2. Acepta la transferencia\n3. Instala el APK\n4. Habilita 'Orígenes desconocidos' si es necesario"
    override val shareAppNote = "El dispositivo receptor debe permitir instalación de apps de orígenes desconocidos"
    
    // Message Types
    override val messageTypeSos = "SOS"
    override val messageTypeImOk = "Estoy Bien"
    override val messageTypeResource = "Solicitud de Recursos"
    override val messageTypeDanger = "Reporte de Peligro"
    override val messageTypeChat = "Mensaje"
    
    // Priority
    override val priorityCritical = "CRÍTICO"
    override val priorityHigh = "ALTO"
    override val priorityMedium = "MEDIO"
    override val priorityLow = "BAJO"
    
    // Time
    override val justNow = "Ahora"
    override val minutesAgo = "min"
    override val hoursAgo = "h"
    
    // Settings
    override val settings = "Configuración"
    override val language = "Idioma"
    override val changeLanguage = "Cambiar Idioma"
    
    // Common
    override val cancel = "Cancelar"
    override val confirm = "Confirmar"
    override val ok = "OK"
    override val yes = "Sí"
    override val no = "No"
    override val error = "Error"
    override val loading = "Cargando..."
}
