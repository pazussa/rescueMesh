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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.model.SosCategory
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSosScreen(
    onSend: (SosCategory, Int, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SosCategory.MEDICAL) }
    var peopleCount by remember { mutableStateOf(1) }
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
                color = RescueMeshColors.Danger.copy(alpha = 0.95f)
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
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = "🆘 ${strings.sendSos}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isEnglish) "Request emergency help" else "Solicitar ayuda de emergencia",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f)
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
                // Section: Tipo de emergencia
                SectionTitle(
                    title = if (isEnglish) "Emergency type" else "Tipo de emergencia"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Grid de categorías
                val categories = listOf(
                    Triple(SosCategory.MEDICAL, "🏥", if (isEnglish) "Medical" else "Médico"),
                    Triple(SosCategory.FIRE, "🔥", if (isEnglish) "Fire" else "Fuego"),
                    Triple(SosCategory.TRAPPED, "🪤", if (isEnglish) "Trapped" else "Atrapado"),
                    Triple(SosCategory.CHILDREN, "👶", if (isEnglish) "Children" else "Niños"),
                    Triple(SosCategory.ELDERLY, "👴", if (isEnglish) "Elderly" else "Adulto mayor"),
                    Triple(SosCategory.INJURED, "🩹", if (isEnglish) "Injured" else "Herido")
                )
                
                // Grid 2x3
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { (cat, emoji, label) ->
                                CategoryChip(
                                    modifier = Modifier.weight(1f),
                                    emoji = emoji,
                                    label = label,
                                    isSelected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat }
                                )
                            }
                            // Fill empty space if row has less than 3 items
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Section: Número de personas
                SectionTitle(
                    title = if (isEnglish) "People affected" else "Personas afectadas"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PeopleCounter(
                    count = peopleCount,
                    onCountChange = { peopleCount = it.coerceIn(1, 99) },
                    isEnglish = isEnglish
                )
                
                Spacer(modifier = Modifier.height(28.dp))
                
                // Section: Info adicional (opcional)
                SectionTitle(
                    title = if (isEnglish) "Additional info (optional)" else "Info adicional (opcional)"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = additionalInfo,
                    onValueChange = { additionalInfo = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isEnglish) "Describe your situation..." else "Describe tu situación...",
                            color = RescueMeshColors.TextHint
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
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
                
                Spacer(modifier = Modifier.height(24.dp))
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
                        onClick = { onSend(selectedCategory, peopleCount, additionalInfo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RescueMeshColors.Danger
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "🆘",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEnglish) "SEND SOS" else "ENVIAR SOS",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = RescueMeshColors.OnSurfaceVariant,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun CategoryChip(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) RescueMeshColors.Danger.copy(alpha = 0.15f) else RescueMeshColors.Surface
    )
    val borderColor by animateColorAsState(
        if (isSelected) RescueMeshColors.Danger else RescueMeshColors.Divider
    )
    
    Surface(
        modifier = modifier
            .height(80.dp)
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
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) RescueMeshColors.Danger else RescueMeshColors.OnSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PeopleCounter(
    count: Int,
    onCountChange: (Int) -> Unit,
    isEnglish: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = RescueMeshColors.Surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (count == 1) 
                        (if (isEnglish) "1 person" else "1 persona")
                    else 
                        "$count ${if (isEnglish) "people" else "personas"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RescueMeshColors.OnSurface
                )
                Text(
                    text = if (isEnglish) "including yourself" else "incluyéndote",
                    fontSize = 13.sp,
                    color = RescueMeshColors.OnSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CounterButton(
                    text = "−",
                    enabled = count > 1,
                    onClick = { onCountChange(count - 1) }
                )
                
                Text(
                    text = count.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnSurface,
                    modifier = Modifier.widthIn(min = 40.dp),
                    textAlign = TextAlign.Center
                )
                
                CounterButton(
                    text = "+",
                    enabled = count < 99,
                    onClick = { onCountChange(count + 1) }
                )
            }
        }
    }
}

@Composable
private fun CounterButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
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
