package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.model.IncidentRoom
import com.rescuemesh.app.ui.theme.RescueMeshColors
import com.rescuemesh.app.localization.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomInfoScreen(
    room: IncidentRoom,
    connectedPeers: Int,
    qrData: String?,
    onBack: () -> Unit,
    onShareRoom: () -> Unit,
    onShareApp: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Strings.get("room_info_title")) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareRoom) {
                        Icon(Icons.Default.Share, contentDescription = Strings.get("share"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Surface,
                    titleContentColor = RescueMeshColors.OnSurface,
                    navigationIconContentColor = RescueMeshColors.OnSurface,
                    actionIconContentColor = RescueMeshColors.OnSurface
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
            // Nombre de la sala
            Text(
                text = "",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = room.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnBackground,
                textAlign = TextAlign.Center
            )
            
            if (room.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = room.description,
                    fontSize = 14.sp,
                    color = RescueMeshColors.OnBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Código de sala
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RescueMeshColors.SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Strings.get("room_code"),
                        fontSize = 14.sp,
                        color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = room.id,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.Primary,
                        letterSpacing = 4.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // PIN
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RescueMeshColors.SurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PIN",
                        fontSize = 14.sp,
                        color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = room.pin,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.Secondary,
                        letterSpacing = 8.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Estado de conexión
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RescueMeshColors.Surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$connectedPeers",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = RescueMeshColors.Success
                        )
                        Text(
                            text = Strings.get("connected"),
                            fontSize = 12.sp,
                            color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Signal:",
                            fontSize = 28.sp
                        )
                        Text(
                            text = Strings.get("mesh_active"),
                            fontSize = 12.sp,
                            color = RescueMeshColors.OnSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Share App Button
            Button(
                onClick = onShareApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.get("share_app_button"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RescueMeshColors.Info.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = Strings.get("join_instructions_title"),
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.OnSurface,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Strings.get("join_instructions_body"),
                        color = RescueMeshColors.OnSurface.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
