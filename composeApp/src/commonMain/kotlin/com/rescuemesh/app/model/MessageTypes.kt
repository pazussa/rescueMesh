package com.rescuemesh.app.model

import kotlinx.serialization.Serializable

/**
 * Tipos de mensajes soportados en RescueMesh
 */
@Serializable
enum class MessageType {
    SOS,
    IM_OK,
    RESOURCE_REQUEST,
    DANGER_REPORT,
    CHAT
}

/**
 * Categorías de SOS
 */
@Serializable
enum class SosCategory {
    MEDICAL,          // Emergencia médica
    FIRE,             // Incendio
    TRAPPED,          // Atrapado/a
    CHILDREN,         // Niños en peligro
    ELDERLY,          // Personas mayores
    INJURED,          // Herido/a
    OTHER             // Otro
}

/**
 * Tipos de recursos que se pueden solicitar
 */
@Serializable
enum class ResourceType {
    WATER,            // Agua
    FOOD,             // Comida
    FIRST_AID,        // Botiquín/primeros auxilios
    TRANSPORT,        // Transporte
    SHELTER,          // Refugio
    BLANKETS,         // Mantas
    FLASHLIGHT,       // Linterna
    BATTERY,          // Baterías
    MEDICINE,         // Medicinas
    OTHER             // Otro
}

/**
 * Tipos de peligros reportables
 */
@Serializable
enum class DangerType {
    FIRE,             // Fuego
    COLLAPSE,         // Derrumbe
    FLOOD,            // Inundación
    GAS_LEAK,         // Fuga de gas
    BLOCKED_ROAD,     // Camino bloqueado
    UNSAFE_BUILDING,  // Edificio inseguro
    ELECTRICAL,       // Peligro eléctrico
    OTHER             // Otro
}

/**
 * Prioridad del mensaje (para ordenar)
 */
@Serializable
enum class MessagePriority(val value: Int) {
    CRITICAL(0),      // SOS con heridos, niños, fuego
    HIGH(1),          // SOS general
    MEDIUM(2),        // Reportes de peligro
    LOW(3),           // Solicitudes de recursos
    INFO(4)           // I'm OK, chat
}
