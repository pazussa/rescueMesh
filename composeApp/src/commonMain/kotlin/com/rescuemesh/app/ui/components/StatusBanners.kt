package com.rescuemesh.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.ui.theme.RescueMeshColors

/**
 * Banner de advertencia cuando Bluetooth está desactivado
 */
@Composable
fun BluetoothWarningBanner(
    isBluetoothEnabled: Boolean,
    onEnableBluetooth: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !isBluetoothEnabled,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEnableBluetooth() },
            colors = CardDefaults.cardColors(
                containerColor = RescueMeshColors.Danger
            ),
            shape = RoundedCornerShape(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono con animación de pulso
                val infiniteTransition = rememberInfiniteTransition()
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                
                Text(
                    text = "📵",
                    fontSize = 24.sp,
                    modifier = Modifier.scale(alpha)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bluetooth Desactivado",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "La red mesh requiere Bluetooth. Toca para activar.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
                
                Text(
                    text = "ACTIVAR →",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Banner de estado de la red mesh
 */
@Composable
fun NetworkStatusBanner(
    isAdvertising: Boolean,
    isDiscovering: Boolean,
    connectedPeers: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = isAdvertising || isDiscovering
    
    val bgColor = when {
        connectedPeers > 0 -> RescueMeshColors.Success.copy(alpha = 0.15f)
        isActive -> RescueMeshColors.Warning.copy(alpha = 0.15f)
        else -> RescueMeshColors.Danger.copy(alpha = 0.15f)
    }
    
    val statusColor = when {
        connectedPeers > 0 -> RescueMeshColors.Success
        isActive -> RescueMeshColors.Warning
        else -> RescueMeshColors.Danger
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de estado con animación
            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isActive) 1.3f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor)
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        connectedPeers > 0 -> "📡 Red Mesh Activa"
                        isActive -> "📡 Buscando dispositivos..."
                        else -> "⚠️ Red no disponible"
                    },
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontSize = 13.sp
                )
                Text(
                    text = buildString {
                        if (connectedPeers > 0) {
                            append("$connectedPeers conectado${if (connectedPeers > 1) "s" else ""}")
                        }
                        if (isAdvertising) append(" • Visible")
                        if (isDiscovering) append(" • Buscando")
                        if (!isActive) append("Verifica permisos y Bluetooth")
                    },
                    color = RescueMeshColors.OnBackground.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
            
            Text(
                text = "Ver detalles →",
                color = statusColor,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Botón flotante para acceder al resumen de IA
 */
@Composable
fun AISummaryButton(
    criticalCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (criticalCount > 0) RescueMeshColors.Danger else RescueMeshColors.Primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🤖", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Resumen IA",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                if (criticalCount > 0) {
                    Text(
                        text = "$criticalCount críticos",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
