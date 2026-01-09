package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
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
fun CreateRoomScreen(
    onBack: () -> Unit,
    onCreate: (name: String, description: String) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.createIncidentRoom) },
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
            Text(
                text = "🚨",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isEnglish) "New Incident Room" else "Nueva sala de incidente",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnBackground
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text(strings.roomName, color = RescueMeshColors.OnBackground) },
                placeholder = { Text(strings.roomNameHint, color = RescueMeshColors.OnBackground.copy(alpha = 0.5f)) },
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
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.descriptionHint, color = RescueMeshColors.OnBackground) },
                placeholder = { Text(if (isEnglish) "Describe the situation" else "Describe la situación", color = RescueMeshColors.OnBackground.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RescueMeshColors.Primary,
                    unfocusedBorderColor = RescueMeshColors.OnBackground.copy(alpha = 0.5f),
                    focusedTextColor = RescueMeshColors.OnBackground,
                    unfocusedTextColor = RescueMeshColors.OnBackground,
                    cursorColor = RescueMeshColors.Primary
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RescueMeshColors.SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (isEnglish) "📱 When creating the room:" else "📱 Al crear la sala:",
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.OnSurface,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isEnglish) 
                            "• A PIN will be generated to share\n• You can display a QR code\n• Nearby devices can join"
                        else
                            "• Se generará un PIN para compartir\n• Podrás mostrar un código QR\n• Otros dispositivos cercanos podrán unirse",
                        color = RescueMeshColors.OnSurface.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { onCreate(roomName, description) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Primary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = roomName.isNotBlank()
            ) {
                Text(
                    text = strings.create,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
