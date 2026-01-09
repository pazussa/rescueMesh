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
import com.rescuemesh.app.model.ResourceType
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendResourceRequestScreen(
    onBack: () -> Unit,
    onSend: (ResourceType, Int, Boolean, String) -> Unit
) {
    var selectedResource by remember { mutableStateOf<ResourceType?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var isUrgent by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Recursos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Info,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
                text = "¿Qué necesitas?",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnBackground,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(230.dp)
            ) {
                items(ResourceType.entries) { resource ->
                    ResourceCard(
                        resource = resource,
                        isSelected = selectedResource == resource,
                        onClick = { selectedResource = resource }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() }.take(3) },
                    label = { Text("Cantidad", color = RescueMeshColors.OnBackground) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RescueMeshColors.Info,
                        unfocusedBorderColor = RescueMeshColors.OnBackground.copy(alpha = 0.5f),
                        focusedTextColor = RescueMeshColors.OnBackground,
                        unfocusedTextColor = RescueMeshColors.OnBackground,
                        cursorColor = RescueMeshColors.Info
                    )
                )
                
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { isUrgent = !isUrgent },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUrgent) RescueMeshColors.Warning else RescueMeshColors.Surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            checked = isUrgent,
                            onCheckedChange = { isUrgent = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RescueMeshColors.Danger
                            )
                        )
                        Text(
                            "Urgente",
                            color = if (isUrgent) Color.Black else RescueMeshColors.OnSurface,
                            fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    selectedResource?.let { res ->
                        onSend(res, quantity.toIntOrNull() ?: 1, isUrgent, "")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Info
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedResource != null
            ) {
                Text(
                    text = "📦 Enviar Solicitud",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ResourceCard(
    resource: ResourceType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (emoji, label) = when (resource) {
        ResourceType.WATER -> "💧" to "Agua"
        ResourceType.FOOD -> "🍞" to "Comida"
        ResourceType.FIRST_AID -> "🩹" to "Botiquín"
        ResourceType.TRANSPORT -> "🚗" to "Transporte"
        ResourceType.SHELTER -> "🏠" to "Refugio"
        ResourceType.BLANKETS -> "🛏️" to "Mantas"
        ResourceType.FLASHLIGHT -> "🔦" to "Linterna"
        ResourceType.BATTERY -> "🔋" to "Baterías"
        ResourceType.MEDICINE -> "💊" to "Medicinas"
        ResourceType.OTHER -> "📦" to "Otro"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RescueMeshColors.Info else RescueMeshColors.Surface
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
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else RescueMeshColors.OnSurface
            )
        }
    }
}
