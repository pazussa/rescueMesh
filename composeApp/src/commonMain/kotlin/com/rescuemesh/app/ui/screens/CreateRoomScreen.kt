package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    onCreateRoom: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var roomDescription by remember { mutableStateOf("") }
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RescueMeshColors.GradientStart,
                        RescueMeshColors.Background
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
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
                        tint = RescueMeshColors.OnBackground
                    )
                }
                
                Text(
                    text = strings.createIncidentRoom,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RescueMeshColors.OnBackground
                )
            }
            
            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            RescueMeshColors.Primary.copy(alpha = 0.1f),
                            RoundedCornerShape(20.dp)
                        )
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📡", fontSize = 40.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Room name field
                Text(
                    text = if (isEnglish) "Room name" else "Nombre de la sala",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = RescueMeshColors.OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isEnglish) "e.g., Building A - Fire" else "ej., Edificio A - Incendio",
                            color = RescueMeshColors.TextHint
                        )
                    },
                    singleLine = true,
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Description field
                Text(
                    text = if (isEnglish) "Description (optional)" else "Descripción (opcional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = RescueMeshColors.OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = roomDescription,
                    onValueChange = { roomDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (isEnglish) "Describe the incident..." else "Describe el incidente...",
                            color = RescueMeshColors.TextHint
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
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
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Info card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = RescueMeshColors.SurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isEnglish) "How it works" else "Cómo funciona",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RescueMeshColors.OnSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (isEnglish)
                                "A unique code will be generated that others can use to join your room."
                            else
                                "Se generará un código único que otros podrán usar para unirse.",
                            fontSize = 13.sp,
                            color = RescueMeshColors.OnSurfaceVariant,
                            lineHeight = 19.sp
                        )
                    }
                }
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
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = { onCreateRoom(roomName, roomDescription) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = roomName.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RescueMeshColors.Primary,
                            disabledContainerColor = RescueMeshColors.Primary.copy(alpha = 0.3f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.createRoom,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
