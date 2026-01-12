package com.rescuemesh.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Light theme color system for RescueMesh
 * Modern, clean design with proper semantic colors
 */
object RescueMeshColors {
    // Brand - Blue for primary actions
    val Primary = Color(0xFF2196F3)
    val PrimaryDark = Color(0xFF1976D2)
    val PrimaryLight = Color(0xFF64B5F6)
    val PrimaryContainer = Color(0xFFE3F2FD)
    
    // Secondary - Teal for secondary actions
    val Secondary = Color(0xFF00BCD4)
    val SecondaryDark = Color(0xFF0097A7)
    val SecondaryContainer = Color(0xFFE0F7FA)
    
    // Status colors - Semantic and clear
    val Success = Color(0xFF4CAF50)
    val SuccessLight = Color(0xFFC8E6C9)
    val SuccessContainer = Color(0xFFE8F5E9)
    
    val Warning = Color(0xFFFF9800)
    val WarningLight = Color(0xFFFFE0B2)
    val WarningContainer = Color(0xFFFFF3E0)
    
    val Danger = Color(0xFFF44336)  // Real red
    val DangerDark = Color(0xFFD32F2F)
    val DangerContainer = Color(0xFFFFEBEE)
    
    val Info = Color(0xFF2196F3)  // Real blue
    val InfoLight = Color(0xFFBBDEFB)
    val InfoContainer = Color(0xFFE3F2FD)
    
    val Emergency = Color(0xFFD32F2F)
    
    // Backgrounds - Light theme
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)
    val SurfaceElevated = Color(0xFFFFFFFF)
    val SurfaceCard = Color(0xFFFFFFFF)
    
    // Text colors - Dark on light
    val OnPrimary = Color.White
    val OnBackground = Color(0xFF212121)
    val OnSurface = Color(0xFF212121)
    val OnSurfaceVariant = Color(0xFF757575)
    val TextMuted = Color(0xFF9E9E9E)
    val TextHint = Color(0xFFBDBDBD)
    
    // Priority colors - Clear distinction
    val PriorityCritical = Color(0xFFD32F2F)
    val PriorityHigh = Color(0xFFF44336)
    val PriorityMedium = Color(0xFFFF9800)
    val PriorityLow = Color(0xFF4CAF50)
    val PriorityInfo = Color(0xFF2196F3)
    
    // Utilities
    val Divider = Color(0xFFE0E0E0)
    val Scrim = Color(0x52000000)
    val Ripple = Color(0x1F000000)
    
    // Gradients (subtle for light theme)
    val GradientStart = Color(0xFFFFFFFF)
    val GradientEnd = Color(0xFFFAFAFA)
}
