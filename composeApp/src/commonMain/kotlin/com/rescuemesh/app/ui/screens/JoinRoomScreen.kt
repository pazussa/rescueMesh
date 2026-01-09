package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomScreen(
    onBack: () -> Unit,
    onJoin: (roomId: String, pin: String, roomName: String) -> Unit
) {
    var roomId by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var roomName by remember { mutableStateOf("") }
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.joinRoom) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Surface,
                    titleContentColor = RescueMeshColors.OnSurface,
                    navigationIconContentColor = RescueMeshColors.OnSurface
                )
            )
        },
        containerColor = RescueMeshColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon and title
            Text(
                text = "🔗",
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isEnglish) "Enter room details" else "Ingresa los datos de la sala",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnBackground
            )
            
            Text(
                text = if (isEnglish) "Request the code and PIN from the room creator" else "Solicita el código y PIN al creador de la sala",
                fontSize = 14.sp,
                color = RescueMeshColors.OnBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Manual entry fields
            OutlinedTextField(
                value = roomId,
                onValueChange = { roomId = it.uppercase().take(8) },
                label = { Text(strings.roomCode, color = RescueMeshColors.OnBackground) },
                placeholder = { Text("ABCDEFGH", color = RescueMeshColors.OnBackground.copy(alpha = 0.5f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RescueMeshColors.Primary,
                    unfocusedBorderColor = RescueMeshColors.OnBackground.copy(alpha = 0.5f),
                    focusedTextColor = RescueMeshColors.OnBackground,
                    unfocusedTextColor = RescueMeshColors.OnBackground,
                    cursorColor = RescueMeshColors.Primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                label = { Text(strings.pin, color = RescueMeshColors.OnBackground) },
                placeholder = { Text("123456", color = RescueMeshColors.OnBackground.copy(alpha = 0.5f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RescueMeshColors.Primary,
                    unfocusedBorderColor = RescueMeshColors.OnBackground.copy(alpha = 0.5f),
                    focusedTextColor = RescueMeshColors.OnBackground,
                    unfocusedTextColor = RescueMeshColors.OnBackground,
                    cursorColor = RescueMeshColors.Primary
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text(if (isEnglish) "Room name (optional)" else "Nombre de la sala (opcional)", color = RescueMeshColors.OnBackground) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RescueMeshColors.Primary,
                    unfocusedBorderColor = RescueMeshColors.OnBackground.copy(alpha = 0.5f),
                    focusedTextColor = RescueMeshColors.OnBackground,
                    unfocusedTextColor = RescueMeshColors.OnBackground,
                    cursorColor = RescueMeshColors.Primary
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { onJoin(roomId, pin, roomName.ifBlank { "${if (isEnglish) "Room" else "Sala"} $roomId" }) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Primary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = roomId.length == 8 && pin.length == 6
            ) {
                Text(
                    text = strings.join,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
