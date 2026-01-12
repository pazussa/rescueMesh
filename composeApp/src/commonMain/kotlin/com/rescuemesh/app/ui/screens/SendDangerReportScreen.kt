package com.rescuemesh.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.rescuemesh.app.model.DangerType
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendDangerReportScreen(
    onSend: (DangerType, Boolean, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(DangerType.FIRE) }
    var isBlocking by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
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
                color = RescueMeshColors.Warning.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = "⚠️ ${strings.reportDanger}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = if (isEnglish) "Alert others about hazards" else "Alerta a otros sobre peligros",
                                fontSize = 13.sp,
                                color = Color.Black.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Section: Tipo de peligro
                SectionLabel(if (isEnglish) "Danger type" else "Tipo de peligro")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val dangers = listOf(
                    Triple(DangerType.FIRE, "��", if (isEnglish) "Fire" else "Fuego"),
                    Triple(DangerType.COLLAPSE, "🏚️", if (isEnglish) "Collapse" else "Derrumbe"),
                    Triple(DangerType.FLOOD, "🌊", if (isEnglish) "Flood" else "Inundación"),
                    Triple(DangerType.GAS_LEAK, "💨", if (isEnglish) "Gas Leak" else "Fuga de gas"),
                    Triple(DangerType.BLOCKED_ROAD, "🚧", if (isEnglish) "Blocked Road" else "Vía bloqueada"),
                    Triple(DangerType.UNSAFE_BUILDING, "🏗️", if (isEnglish) "Unsafe Building" else "Edificio inseguro"),
                    Triple(DangerType.ELECTRICAL, "⚡", if (isEnglish) "Electrical" else "Eléctrico"),
                    Triple(DangerType.OTHER, "⚠️", if (isEnglish) "Other" else "Otro")
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    dangers.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { (type, emoji, label) ->
                                DangerChip(
                                    modifier = Modifier.weight(1f),
                                    emoji = emoji,
                                    label = label,
                                    isSelected = selectedType == type,
                                    onClick = { selectedType = type }
                                )
                            }
                            repeat(4 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Blocking toggle
                BlockingToggle(
                    isBlocking = isBlocking,
                    onToggle = { isBlocking = !isBlocking },
                    isEnglish = isEnglish
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Descripción
                SectionLabel(if (isEnglish) "Description (optional)" else "Descripción (opcional)")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isEnglish) "Describe the danger zone..." else "Describe la zona de peligro...",
                            color = RescueMeshColors.TextHint
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RescueMeshColors.Warning,
                        unfocusedBorderColor = RescueMeshColors.Divider,
                        focusedContainerColor = RescueMeshColors.Surface,
                        unfocusedContainerColor = RescueMeshColors.Surface,
                        focusedTextColor = RescueMeshColors.OnSurface,
                        unfocusedTextColor = RescueMeshColors.OnSurface,
                        cursorColor = RescueMeshColors.Warning
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
                        onClick = { onSend(selectedType, isBlocking, description) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RescueMeshColors.Warning
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEnglish) "SEND ALERT" else "ENVIAR ALERTA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
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
private fun DangerChip(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) RescueMeshColors.Warning.copy(alpha = 0.2f) else RescueMeshColors.Surface
    )
    val borderColor by animateColorAsState(
        if (isSelected) RescueMeshColors.Warning else RescueMeshColors.Divider
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
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) RescueMeshColors.Warning else RescueMeshColors.OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun BlockingToggle(
    isBlocking: Boolean,
    onToggle: () -> Unit,
    isEnglish: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() },
        color = if (isBlocking) RescueMeshColors.Danger.copy(alpha = 0.15f) else RescueMeshColors.Surface,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🚫", fontSize = 24.sp)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEnglish) "Blocks access" else "Bloquea el acceso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBlocking) RescueMeshColors.Danger else RescueMeshColors.OnSurface
                )
                Text(
                    text = if (isEnglish) "Cannot pass through this area" else "No se puede pasar por esta zona",
                    fontSize = 12.sp,
                    color = RescueMeshColors.OnSurfaceVariant
                )
            }
            
            Switch(
                checked = isBlocking,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = RescueMeshColors.Danger,
                    uncheckedThumbColor = RescueMeshColors.OnSurfaceVariant,
                    uncheckedTrackColor = RescueMeshColors.SurfaceVariant
                )
            )
        }
    }
}
