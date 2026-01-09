package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.ui.theme.RescueMeshColors

@Composable
fun WelcomeScreen(
    userName: String,
    onUserNameChange: (String) -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit
) {
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RescueMeshColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Language toggle at top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { LanguageManager.toggleLanguage() }
            ) {
                Text(
                    text = if (currentLanguage == LanguageManager.Language.ENGLISH) "🇪🇸 ES" else "🇬🇧 EN",
                    color = RescueMeshColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(0.1f))
        
        // Logo/Title
        Text(
            text = "🆘",
            fontSize = 72.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = strings.appName,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = RescueMeshColors.Primary
        )
        
        Text(
            text = strings.appTagline,
            fontSize = 16.sp,
            color = RescueMeshColors.OnBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Name field
        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChange,
            label = { Text(strings.yourName, color = RescueMeshColors.OnBackground) },
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Create Room Button
        Button(
            onClick = onCreateRoom,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RescueMeshColors.Primary
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = userName.isNotBlank()
        ) {
            Text(
                text = strings.createIncidentRoom,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Join Button
        OutlinedButton(
            onClick = onJoinRoom,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RescueMeshColors.Primary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(RescueMeshColors.Primary)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = userName.isNotBlank()
        ) {
            Text(
                text = strings.joinIncidentRoom,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Info Card
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
                    text = if (currentLanguage == LanguageManager.Language.ENGLISH) 
                        "ℹ️ How it works" else "ℹ️ Cómo funciona",
                    fontWeight = FontWeight.Bold,
                    color = RescueMeshColors.OnSurface,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (currentLanguage == LanguageManager.Language.ENGLISH)
                        "RescueMesh creates a network between nearby phones without internet. Emergency messages propagate automatically through other devices."
                    else
                        "RescueMesh crea una red entre teléfonos cercanos sin necesidad de internet. Los mensajes de emergencia se propagan automáticamente a través de otros dispositivos.",
                    color = RescueMeshColors.OnSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(0.1f))
    }
}
