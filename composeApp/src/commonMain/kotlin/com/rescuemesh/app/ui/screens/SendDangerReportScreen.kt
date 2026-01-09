package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.model.DangerType
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendDangerReportScreen(
    onBack: () -> Unit,
    onSend: (DangerType, Int, String, Boolean) -> Unit
) {
    var selectedDanger by remember { mutableStateOf<DangerType?>(null) }
    var isBlocking by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Peligro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Warning,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = RescueMeshColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Tipo de peligro",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnBackground,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(DangerType.entries) { danger ->
                    DangerCard(
                        danger = danger,
                        isSelected = selectedDanger == danger,
                        onClick = { selectedDanger = danger }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bloquea el paso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isBlocking = !isBlocking }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isBlocking,
                    onCheckedChange = { isBlocking = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = RescueMeshColors.Warning
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Blocked: Bloquea el paso / acceso",
                    color = RescueMeshColors.OnBackground
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    selectedDanger?.let { danger ->
                        // Severity 5 by default, empty description
                        onSend(danger, 5, "", isBlocking)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Warning
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedDanger != null
            ) {
                Text(
                    text = "WARNING: Reportar Peligro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun DangerCard(
    danger: DangerType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (emoji, label) = when (danger) {
        DangerType.FIRE -> "Fire:" to "Fuego"
        DangerType.COLLAPSE -> "Collapse:" to "Derrumbe"
        DangerType.FLOOD -> "Flood:" to "Inundación"
        DangerType.GAS_LEAK -> "Gas:" to "Fuga de gas"
        DangerType.BLOCKED_ROAD -> "Blocked:" to "Camino bloqueado"
        DangerType.UNSAFE_BUILDING -> "" to "Edificio inseguro"
        DangerType.ELECTRICAL -> "Electrical:" to "Eléctrico"
        DangerType.OTHER -> "WARNING:" to "Otro"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RescueMeshColors.Warning else RescueMeshColors.Surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.Black else RescueMeshColors.OnSurface
            )
        }
    }
}
