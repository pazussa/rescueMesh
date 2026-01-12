package com.rescuemesh.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Adaptive dimensions
data class AppDimens(
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 12.dp,
    val itemSpacing: Dp = 10.dp,
    val sectionSpacing: Dp = 20.dp,
    val iconSize: Dp = 22.dp,
    val iconSizeSmall: Dp = 18.dp,
    val buttonHeight: Dp = 48.dp,
    val buttonHeightSmall: Dp = 40.dp,
    val cardRadius: Dp = 12.dp,
    val buttonRadius: Dp = 10.dp,
    val inputRadius: Dp = 10.dp,
    val avatarSize: Dp = 44.dp,
    val avatarSizeSmall: Dp = 36.dp,
    val statusIndicator: Dp = 10.dp
)

val LocalAppDimens = compositionLocalOf { AppDimens() }

// Compact typography
object AppTypography {
    val displayLarge = TextStyle(
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    )
    val displayMedium = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.sp
    )
    val headlineLarge = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp
    )
    val headlineMedium = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp
    )
    val titleLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp
    )
    val titleMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp
    )
    val bodyLarge = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp
    )
    val bodyMedium = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp
    )
    val bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp
    )
    val labelLarge = TextStyle(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp
    )
    val labelMedium = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp
    )
    val labelSmall = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 12.sp
    )
}

// Modern light theme colors
object AppColors {
    // Brand - Blue primary
    val Primary = Color(0xFF2196F3)
    val PrimaryContainer = Color(0xFFE3F2FD)
    val OnPrimary = Color.White
    val OnPrimaryContainer = Color(0xFF0D47A1)
    
    // Secondary - Teal
    val Secondary = Color(0xFF26A69A)
    val SecondaryContainer = Color(0xFFE0F2F1)
    val OnSecondary = Color.White
    val OnSecondaryContainer = Color(0xFF00695C)
    
    // Background & Surface (Light theme)
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)
    val SurfaceElevated = Color(0xFFFFFFFF)
    
    // Text
    val OnBackground = Color(0xFF212121)
    val OnSurface = Color(0xFF212121)
    val OnSurfaceVariant = Color(0xFF757575)
    val TextMuted = Color(0xFF9E9E9E)
    
    // Status
    val Success = Color(0xFF4CAF50)
    val SuccessContainer = Color(0xFFE8F5E9)
    val Warning = Color(0xFFFF9800)
    val WarningContainer = Color(0xFFFFF3E0)
    val Error = Color(0xFFF44336)
    val ErrorContainer = Color(0xFFFFEBEE)
    val Info = Color(0xFF2196F3)
    val InfoContainer = Color(0xFFE3F2FD)
    
    // Priority colors
    val PriorityCritical = Color(0xFFD32F2F)
    val PriorityHigh = Color(0xFFE64A19)
    val PriorityMedium = Color(0xFFF57C00)
    val PriorityLow = Color(0xFF388E3C)
    val PriorityInfo = Color(0xFF1976D2)
    
    // Overlay
    val Scrim = Color(0x40000000)
    val Divider = Color(0xFFE0E0E0)
}

// Design shapes
object AppShapes {
    val small = RoundedCornerShape(6.dp)
    val medium = RoundedCornerShape(10.dp)
    val large = RoundedCornerShape(14.dp)
    val extraLarge = RoundedCornerShape(20.dp)
    val full = RoundedCornerShape(50)
}

@Composable
fun RescueMeshTheme(
    content: @Composable () -> Unit
) {
    val dimens = AppDimens()
    
    // Light theme
    val colorScheme = lightColorScheme(
        primary = AppColors.Primary,
        onPrimary = AppColors.OnPrimary,
        primaryContainer = AppColors.PrimaryContainer,
        onPrimaryContainer = AppColors.OnPrimaryContainer,
        secondary = AppColors.Secondary,
        onSecondary = AppColors.OnSecondary,
        secondaryContainer = AppColors.SecondaryContainer,
        onSecondaryContainer = AppColors.OnSecondaryContainer,
        background = AppColors.Background,
        onBackground = AppColors.OnBackground,
        surface = AppColors.Surface,
        onSurface = AppColors.OnSurface,
        surfaceVariant = AppColors.SurfaceVariant,
        onSurfaceVariant = AppColors.OnSurfaceVariant,
        error = AppColors.Error,
        onError = Color.White,
        errorContainer = AppColors.ErrorContainer
    )
    
    CompositionLocalProvider(
        LocalAppDimens provides dimens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
