package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomScreen(
    onJoinRoom: (String) -> Unit,
    onBack: () -> Unit
) {
    var roomCode by remember { mutableStateOf("") }
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
                    text = strings.joinIncidentRoom,
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                
                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            RescueMeshColors.Secondary.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔗", fontSize = 40.sp)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = if (isEnglish) "Enter room code" else "Ingresa el código de sala",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isEnglish)
                        "Ask the room creator for the code"
                    else
                        "Pide el código al creador de la sala",
                    fontSize = 14.sp,
                    color = RescueMeshColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Code input
                OutlinedTextField(
                    value = roomCode,
                    onValueChange = { roomCode = it.uppercase().take(8) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "XXXXXXXX",
                            color = RescueMeshColors.TextHint,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 4.sp
                    ),
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
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Info card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = RescueMeshColors.SurfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📶", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isEnglish) "Offline connection" else "Conexión sin internet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RescueMeshColors.OnSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (isEnglish)
                                "You'll connect via Bluetooth and WiFi Direct with nearby devices."
                            else
                                "Te conectarás vía Bluetooth y WiFi Direct con dispositivos cercanos.",
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
                        onClick = { onJoinRoom(roomCode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = roomCode.length >= 4,
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
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.joinRoom,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
