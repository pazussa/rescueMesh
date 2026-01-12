package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RescueMeshColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(RescueMeshColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📡",
                    fontSize = 44.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Title
            Text(
                text = strings.appName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnBackground
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = strings.appTagline,
                fontSize = 14.sp,
                color = RescueMeshColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // Name input
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { 
                    Text(
                        "Your name",
                        fontSize = 14.sp
                    ) 
                },
                placeholder = {
                    Text(
                        "Enter your name",
                        color = RescueMeshColors.TextHint,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = RescueMeshColors.Primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RescueMeshColors.Primary,
                    unfocusedBorderColor = RescueMeshColors.Divider,
                    focusedContainerColor = RescueMeshColors.Surface,
                    unfocusedContainerColor = RescueMeshColors.Surface,
                    focusedTextColor = RescueMeshColors.OnSurface,
                    unfocusedTextColor = RescueMeshColors.OnSurface,
                    cursorColor = RescueMeshColors.Primary,
                    focusedLabelColor = RescueMeshColors.Primary,
                    unfocusedLabelColor = RescueMeshColors.OnSurfaceVariant
                )
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Create Room button
            Button(
                onClick = onCreateRoom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = userName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Primary,
                    contentColor = Color.White,
                    disabledContainerColor = RescueMeshColors.Primary.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.createIncidentRoom,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Join Room button
            OutlinedButton(
                onClick = onJoinRoom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = userName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RescueMeshColors.Primary,
                    disabledContentColor = RescueMeshColors.Primary.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (userName.isNotBlank()) RescueMeshColors.Primary else RescueMeshColors.Primary.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.joinIncidentRoom,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // Info card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = RescueMeshColors.SurfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How it works",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RescueMeshColors.OnSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    InfoItem(
                        emoji = "📶",
                        text = "Works without internet or cellular"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InfoItem(
                        emoji = "🔗",
                        text = "Creates mesh network with nearby devices"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    InfoItem(
                        emoji = "📨",
                        text = "Messages propagate automatically"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoItem(
    emoji: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = emoji,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = RescueMeshColors.OnSurfaceVariant
        )
    }
}
