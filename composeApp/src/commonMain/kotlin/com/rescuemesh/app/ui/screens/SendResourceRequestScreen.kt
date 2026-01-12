package com.rescuemesh.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.model.ResourceType
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendResourceRequestScreen(
    onSend: (ResourceType, Int, Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(ResourceType.WATER) }
    var quantity by remember { mutableStateOf(1) }
    var isUrgent by remember { mutableStateOf(false) }
    var additionalInfo by remember { mutableStateOf("") }
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RescueMeshColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RescueMeshColors.Surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = RescueMeshColors.OnSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Column {
                        Text(
                            text = "📦 ${strings.requestResources}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RescueMeshColors.OnSurface
                        )
                        Text(
                            text = if (isEnglish) "Request supplies you need" else "Solicita suministros que necesitas",
                            fontSize = 12.sp,
                            color = RescueMeshColors.OnSurfaceVariant
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Section: Tipo de recurso
                SectionLabel(if (isEnglish) "Resource type" else "Tipo de recurso")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val resources = listOf(
                    Triple(ResourceType.WATER, "💧", if (isEnglish) "Water" else "Agua"),
                    Triple(ResourceType.FOOD, "🍞", if (isEnglish) "Food" else "Comida"),
                    Triple(ResourceType.FIRST_AID, "🩹", if (isEnglish) "First Aid" else "Botiquín"),
                    Triple(ResourceType.MEDICINE, "💊", if (isEnglish) "Medicine" else "Medicina"),
                    Triple(ResourceType.BLANKETS, "🛏️", if (isEnglish) "Blankets" else "Mantas"),
                    Triple(ResourceType.FLASHLIGHT, "🔦", if (isEnglish) "Flashlight" else "Linterna"),
                    Triple(ResourceType.BATTERY, "🔋", if (isEnglish) "Batteries" else "Baterías"),
                    Triple(ResourceType.TRANSPORT, "🚗", if (isEnglish) "Transport" else "Transporte"),
                    Triple(ResourceType.SHELTER, "🏠", if (isEnglish) "Shelter" else "Refugio")
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    resources.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { (type, emoji, label) ->
                                ResourceChip(
                                    modifier = Modifier.weight(1f),
                                    emoji = emoji,
                                    label = label,
                                    isSelected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                            }
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Section: Cantidad
                SectionLabel(if (isEnglish) "Quantity" else "Cantidad")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                QuantitySelector(
                    count = quantity,
                    onCountChange = { quantity = it.coerceIn(1, 99) }
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Urgente toggle
                UrgentToggle(
                    isUrgent = isUrgent,
                    onToggle = { isUrgent = !isUrgent },
                    isEnglish = isEnglish
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Info adicional
                SectionLabel(if (isEnglish) "Notes (optional)" else "Notas (opcional)")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { additionalInfo = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isEnglish) "Any additional details..." else "Detalles adicionales...",
                            color = RescueMeshColors.TextHint
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RescueMeshColors.Primary,
                        unfocusedBorderColor = RescueMeshColors.Divider,
                        focusedContainerColor = RescueMeshColors.Surface,
                        unfocusedContainerColor = RescueMeshColors.Surface,
                        focusedTextColor = RescueMeshColors.OnSurface,
                        unfocusedTextColor = RescueMeshColors.OnSurface,
                        cursorColor = RescueMeshColors.Primary
                    )
                )
            }
            
            // Bottom button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = RescueMeshColors.Surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(20.dp)
                ) {
                    Button(
                        onClick = { onSend(selectedType, quantity, isUrgent, additionalInfo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUrgent) RescueMeshColors.Warning else RescueMeshColors.Primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "📦",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEnglish) "SEND REQUEST" else "ENVIAR SOLICITUD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = RescueMeshColors.OnSurfaceVariant,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun ResourceChip(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) RescueMeshColors.Primary.copy(alpha = 0.15f) else RescueMeshColors.Surface
    )
    val borderColor by animateColorAsState(
        if (isSelected) RescueMeshColors.Primary else RescueMeshColors.Divider
    )
    
    Surface(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) RescueMeshColors.Primary else RescueMeshColors.OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuantitySelector(
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RescueMeshColors.Surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = count.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnSurface
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CountButton(text = "−", enabled = count > 1) { onCountChange(count - 1) }
                CountButton(text = "+", enabled = count < 99) { onCountChange(count + 1) }
            }
        }
    }
}

@Composable
private fun CountButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled) { onClick() },
        color = if (enabled) RescueMeshColors.Primary else RescueMeshColors.SurfaceVariant,
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color.White else RescueMeshColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun UrgentToggle(
    isUrgent: Boolean,
    onToggle: () -> Unit,
    isEnglish: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() },
        color = if (isUrgent) RescueMeshColors.Warning.copy(alpha = 0.15f) else RescueMeshColors.Surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⚡", fontSize = 24.sp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEnglish) "Urgent" else "Urgente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUrgent) RescueMeshColors.Warning else RescueMeshColors.OnSurface
                )
                Text(
                    text = if (isEnglish) "Mark as high priority" else "Marcar como alta prioridad",
                    fontSize = 12.sp,
                    color = RescueMeshColors.OnSurfaceVariant
                )
            }
            
            Switch(
                checked = isUrgent,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RescueMeshColors.Warning,
                    uncheckedThumbColor = RescueMeshColors.OnSurfaceVariant,
                    uncheckedTrackColor = RescueMeshColors.SurfaceVariant
                )
            )
        }
    }
}
