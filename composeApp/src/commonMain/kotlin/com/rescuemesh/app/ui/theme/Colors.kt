package com.rescuemesh.app.ui.theme

import androidx.compose.ui.graphics.Color

// Colores principales de RescueMesh
object RescueMeshColors {
    // Primarios
    val Primary = Color(0xFFE65100)  // Naranja emergencia
    val PrimaryDark = Color(0xFFBF360C)
    val PrimaryLight = Color(0xFFFF9800)
    
    // Secundarios
    val Secondary = Color(0xFF1565C0)  // Azul confianza
    val SecondaryDark = Color(0xFF0D47A1)
    
    // Estados
    val Success = Color(0xFF2E7D32)  // Verde OK
    val Warning = Color(0xFFFFC107)  // Amarillo advertencia (más visible)
    val Danger = Color(0xFFC62828)   // Rojo peligro
    val Info = Color(0xFF1976D2)     // Azul info
    val Emergency = Color(0xFFD32F2F) // Rojo emergencia para headers críticos
    
    // Backgrounds
    val Background = Color(0xFF121212)
    val Surface = Color(0xFF1E1E1E)
    val SurfaceVariant = Color(0xFF2D2D2D)
    
    // Texto
    val OnPrimary = Color.White
    val OnBackground = Color(0xFFE0E0E0)
    val OnSurface = Color(0xFFE0E0E0)
    
    // Prioridad de mensajes
    val PriorityCritical = Color(0xFFD32F2F)
    val PriorityHigh = Color(0xFFE64A19)
    val PriorityMedium = Color(0xFFFFA000)
    val PriorityLow = Color(0xFF388E3C)
    val PriorityInfo = Color(0xFF1976D2)
}
