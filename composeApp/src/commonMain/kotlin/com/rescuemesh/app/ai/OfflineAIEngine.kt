package com.rescuemesh.app.ai

import com.rescuemesh.app.model.*

/**
 * Motor de IA Offline para RescueMesh
 * 
 * Funciona completamente sin conexión a internet, usando análisis
 * de patrones locales para:
 * 1. Triage/Priorización automática de mensajes
 * 2. Resumen rápido del estado de situación
 * 3. Traducción básica para brigadas mixtas
 */
class OfflineAIEngine {
    
    // Palabras clave que indican urgencia crítica (multi-idioma básico)
    private val criticalKeywords = setOf(
        // Español
        "herido", "herida", "heridos", "sangre", "sangrando", "inconsciente",
        "no respira", "muerto", "atrapado", "atrapada", "atrapados",
        "niño", "niña", "niños", "bebé", "bebe", "anciano", "anciana",
        "fuego", "incendio", "quemadura", "explosion", "explosión",
        "derrumbe", "colapso", "enterrado", "aplastado",
        "ahogando", "ahogarse", "inundación", "inundacion",
        "ayuda urgente", "emergencia", "auxilio", "socorro",
        // English
        "injured", "bleeding", "unconscious", "trapped", "fire",
        "children", "child", "baby", "elderly", "collapse", "explosion",
        "drowning", "flood", "help", "emergency", "urgent"
    )
    
    // Palabras clave de urgencia alta (no crítica)
    private val highUrgencyKeywords = setOf(
        "dolor", "fractura", "roto", "cortada", "corte", "caída", "caida",
        "mareo", "desmayo", "fiebre", "enfermo", "enferma",
        "agua", "comida", "medicinas", "medicina", "medicamento",
        "lost", "pain", "broken", "sick", "water", "food", "medicine"
    )
    
    // Palabras de peligro/bloqueo
    private val dangerKeywords = setOf(
        "peligro", "peligroso", "bloqueado", "cerrado", "no pasar",
        "gas", "fuga", "cables", "electricidad", "derrumbe",
        "danger", "dangerous", "blocked", "closed", "gas leak"
    )
    
    /**
     * Analiza un mensaje y calcula su puntuación de urgencia (0-100)
     */
    fun calculateUrgencyScore(message: MeshMessage): Int {
        var score = 0
        
        // Puntuación base por tipo de mensaje
        score += when (message.type) {
            MessageType.SOS -> 50
            MessageType.DANGER_REPORT -> 30
            MessageType.RESOURCE_REQUEST -> 20
            MessageType.IM_OK -> 5
            MessageType.CHAT -> 0
        }
        
        // Analizar contenido
        val textToAnalyze = getMessageText(message).lowercase()
        
        // Palabras críticas (+30 cada una, máx 50)
        val criticalCount = criticalKeywords.count { textToAnalyze.contains(it) }
        score += minOf(criticalCount * 30, 50)
        
        // Palabras de alta urgencia (+15 cada una, máx 30)
        val highCount = highUrgencyKeywords.count { textToAnalyze.contains(it) }
        score += minOf(highCount * 15, 30)
        
        // Factores adicionales
        when (val content = message.content) {
            is MessageContent.Sos -> {
                // Número de personas afectadas
                if (content.peopleCount > 1) score += 10
                if (content.peopleCount > 5) score += 10
                
                // Categoría del SOS
                score += when (content.category) {
                    SosCategory.MEDICAL -> 20
                    SosCategory.FIRE -> 25
                    SosCategory.TRAPPED -> 30
                    SosCategory.CHILDREN -> 35
                    SosCategory.ELDERLY -> 25
                    SosCategory.INJURED -> 20
                    SosCategory.OTHER -> 10
                }
            }
            is MessageContent.DangerReport -> {
                if (content.isBlocking) score += 15
                score += when (content.severity) {
                    3 -> 20
                    2 -> 10
                    else -> 5
                }
            }
            is MessageContent.ResourceRequest -> {
                if (content.urgent) score += 20
            }
            else -> {}
        }
        
        return minOf(score, 100) // Máximo 100
    }
    
    /**
     * Clasifica la prioridad de un mensaje según su urgencia
     */
    fun classifyPriority(message: MeshMessage): MessagePriority {
        val score = calculateUrgencyScore(message)
        
        return when {
            score >= 70 -> MessagePriority.CRITICAL
            score >= 50 -> MessagePriority.HIGH
            score >= 30 -> MessagePriority.MEDIUM
            else -> MessagePriority.LOW
        }
    }
    
    /**
     * Genera un resumen ejecutivo del estado de situación
     * "Qué está pasando en 30 segundos"
     */
    fun generateSituationSummary(messages: List<MeshMessage>): SituationSummary {
        if (messages.isEmpty()) {
            return SituationSummary(
                totalMessages = 0,
                criticalCount = 0,
                activeSOSCount = 0,
                totalPeopleAffected = 0,
                dangerZones = emptyList(),
                resourceNeeds = emptyList(),
                safeCount = 0,
                summaryText = "Sin reportes aún. La red está activa."
            )
        }
        
        // Contar por tipo
        val sosList = messages.filter { it.type == MessageType.SOS }
        val dangerList = messages.filter { it.type == MessageType.DANGER_REPORT }
        val resourceList = messages.filter { it.type == MessageType.RESOURCE_REQUEST }
        val safeList = messages.filter { it.type == MessageType.IM_OK }
        
        // Contar urgencias
        val criticalMessages = messages.filter { calculateUrgencyScore(it) >= 70 }
        
        // Personas afectadas
        val peopleAffected = sosList.sumOf { msg ->
            (msg.content as? MessageContent.Sos)?.peopleCount ?: 1
        }
        
        // Zonas de peligro
        val dangerZones = dangerList.mapNotNull { msg ->
            (msg.content as? MessageContent.DangerReport)?.let { danger ->
                DangerZoneInfo(
                    type = danger.dangerType,
                    severity = danger.severity,
                    description = danger.description,
                    isBlocking = danger.isBlocking
                )
            }
        }
        
        // Necesidades de recursos
        val resourceNeeds = resourceList.mapNotNull { msg ->
            (msg.content as? MessageContent.ResourceRequest)?.let { req ->
                ResourceNeedInfo(
                    type = req.resourceType,
                    quantity = req.quantity,
                    urgent = req.urgent
                )
            }
        }.groupBy { it.type }.map { (type, needs) ->
            ResourceNeedInfo(
                type = type,
                quantity = needs.sumOf { it.quantity },
                urgent = needs.any { it.urgent }
            )
        }
        
        // Generar texto de resumen
        val summaryText = buildString {
            if (criticalMessages.isNotEmpty()) {
                append("WARNING: ${criticalMessages.size} EMERGENCIAS CRÍTICAS. ")
            }
            
            if (sosList.isNotEmpty()) {
                append(" ${sosList.size} SOS activos")
                if (peopleAffected > sosList.size) {
                    append(" (~$peopleAffected personas)")
                }
                append(". ")
            }
            
            if (dangerZones.isNotEmpty()) {
                val blocking = dangerZones.count { it.isBlocking }
                append("WARNING: ${dangerZones.size} peligros reportados")
                if (blocking > 0) {
                    append(" ($blocking bloquean paso)")
                }
                append(". ")
            }
            
            if (resourceNeeds.isNotEmpty()) {
                val urgentNeeds = resourceNeeds.filter { it.urgent }
                if (urgentNeeds.isNotEmpty()) {
                    append("Package: ${urgentNeeds.size} recursos urgentes. ")
                }
            }
            
            if (safeList.isNotEmpty()) {
                append("OK: ${safeList.size} confirmados a salvo.")
            }
            
            if (isEmpty()) {
                append("Situación estable. ${messages.size} mensajes en la red.")
            }
        }
        
        // Crear lista de mensajes prioritarios (CRITICAL y HIGH)
        val priorityMessages = messages
            .filter { it.priority == MessagePriority.CRITICAL || it.priority == MessagePriority.HIGH }
            .sortedByDescending { calculateUrgencyScore(it) }
            .take(10)  // Máximo 10 mensajes prioritarios
            .map { msg ->
                PriorityMessageInfo(
                    id = msg.id,
                    senderName = msg.senderName,
                    type = msg.type,
                    summary = createMessageSummary(msg),
                    priority = msg.priority,
                    timestamp = msg.timestamp
                )
            }
        
        return SituationSummary(
            totalMessages = messages.size,
            criticalCount = criticalMessages.size,
            activeSOSCount = sosList.size,
            totalPeopleAffected = peopleAffected,
            dangerZones = dangerZones,
            resourceNeeds = resourceNeeds,
            safeCount = safeList.size,
            summaryText = summaryText,
            priorityMessages = priorityMessages
        )
    }
    
    /**
     * Crea un resumen corto del mensaje para mostrar en la lista de prioritarios
     */
    private fun createMessageSummary(message: MeshMessage): String {
        return when (val content = message.content) {
            is MessageContent.Sos -> {
                val category = when (content.category) {
                    SosCategory.MEDICAL -> "Medical: Médico"
                    SosCategory.FIRE -> "Fire: Fuego"
                    SosCategory.TRAPPED -> "Trapped: Atrapado"
                    SosCategory.CHILDREN -> "Children: Niños"
                    SosCategory.ELDERLY -> "Elderly: Adulto mayor"
                    SosCategory.INJURED -> "Injured: Herido"
                    SosCategory.OTHER -> "Other: Otro"
                }
                val people = if (content.peopleCount > 1) " (${content.peopleCount} personas)" else ""
                " SOS $category$people"
            }
            is MessageContent.DangerReport -> {
                val type = when (content.dangerType) {
                    DangerType.FIRE -> "Fire: Fuego"
                    DangerType.COLLAPSE -> "Collapse: Derrumbe"
                    DangerType.FLOOD -> "Flood: Inundación"
                    DangerType.GAS_LEAK -> "Gas: Fuga de gas"
                    DangerType.BLOCKED_ROAD -> "Blocked: Camino bloqueado"
                    DangerType.UNSAFE_BUILDING -> " Edificio inseguro"
                    DangerType.ELECTRICAL -> "Electrical: Eléctrico"
                    DangerType.OTHER -> "WARNING: Peligro"
                }
                val blocking = if (content.isBlocking) " - BLOQUEA PASO" else ""
                "WARNING: $type$blocking"
            }
            is MessageContent.ResourceRequest -> {
                val type = when (content.resourceType) {
                    ResourceType.WATER -> "Water: Agua"
                    ResourceType.FOOD -> "Food: Comida"
                    ResourceType.FIRST_AID -> "Injured: Botiquín"
                    ResourceType.TRANSPORT -> "Transport: Transporte"
                    ResourceType.SHELTER -> "Shelter: Refugio"
                    ResourceType.BLANKETS -> "Blankets: Mantas"
                    ResourceType.FLASHLIGHT -> "Flashlight: Linterna"
                    ResourceType.BATTERY -> "Battery: Baterías"
                    ResourceType.MEDICINE -> "Medicine: Medicinas"
                    ResourceType.OTHER -> "Package: Otro"
                }
                val urgent = if (content.urgent) " Electrical:URGENTE" else ""
                "Package: Necesita $type x${content.quantity}$urgent"
            }
            is MessageContent.Chat -> "Chat: ${content.text.take(50)}..."
            is MessageContent.ImOk -> "OK: Está bien"
        }
    }
    
    /**
     * Ordena mensajes por prioridad usando IA
     */
    fun sortByPriority(messages: List<MeshMessage>): List<MeshMessage> {
        return messages.sortedByDescending { calculateUrgencyScore(it) }
    }
    
    /**
     * Detecta palabras clave de emergencia en un texto
     */
    fun detectEmergencyKeywords(text: String): List<String> {
        val lowerText = text.lowercase()
        val found = mutableListOf<String>()
        
        criticalKeywords.forEach { keyword ->
            if (lowerText.contains(keyword)) {
                found.add(keyword)
            }
        }
        
        return found
    }
    
    /**
     * Traducciones básicas offline español <-> inglés
     * Para comunicación de emergencia con brigadas mixtas
     */
    fun translateBasic(text: String, toEnglish: Boolean): String {
        val translations = mapOf(
            // Emergencias
            "necesito ayuda" to "I need help",
            "hay heridos" to "there are injured people",
            "fuego" to "fire",
            "incendio" to "fire",
            "atrapado" to "trapped",
            "agua" to "water",
            "comida" to "food",
            "medicina" to "medicine",
            "doctor" to "doctor",
            "ambulancia" to "ambulance",
            "peligro" to "danger",
            "seguro" to "safe",
            "estoy bien" to "I'm OK",
            "niños" to "children",
            "ayuda urgente" to "urgent help",
            "no puedo moverme" to "I can't move",
            "derrumbe" to "collapse",
            "bloqueado" to "blocked"
        )
        
        var result = text.lowercase()
        
        if (toEnglish) {
            translations.forEach { (es, en) ->
                result = result.replace(es, en)
            }
        } else {
            translations.forEach { (es, en) ->
                result = result.replace(en.lowercase(), es)
            }
        }
        
        return result
    }
    
    /**
     * Extrae el texto principal de un mensaje
     */
    private fun getMessageText(message: MeshMessage): String {
        return when (val content = message.content) {
            is MessageContent.Sos -> content.description
            is MessageContent.ImOk -> content.message
            is MessageContent.ResourceRequest -> content.description
            is MessageContent.DangerReport -> content.description
            is MessageContent.Chat -> content.text
        }
    }
}

/**
 * Resumen de situación generado automáticamente
 */
data class SituationSummary(
    val totalMessages: Int,
    val criticalCount: Int,
    val activeSOSCount: Int,
    val totalPeopleAffected: Int,
    val dangerZones: List<DangerZoneInfo>,
    val resourceNeeds: List<ResourceNeedInfo>,
    val safeCount: Int,
    val summaryText: String,
    val priorityMessages: List<PriorityMessageInfo> = emptyList()
)

data class DangerZoneInfo(
    val type: DangerType,
    val severity: Int,
    val description: String,
    val isBlocking: Boolean
)

data class ResourceNeedInfo(
    val type: ResourceType,
    val quantity: Int,
    val urgent: Boolean
)

/**
 * Información de mensaje prioritario para mostrar en el resumen
 */
data class PriorityMessageInfo(
    val id: String,
    val senderName: String,
    val type: MessageType,
    val summary: String,
    val priority: MessagePriority,
    val timestamp: Long
)
