package com.rescuemesh.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.model.SosCategory
import com.rescuemesh.app.ui.theme.RescueMeshColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendSosScreen(
    onBack: () -> Unit,
    onSend: (SosCategory, String, Int) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<SosCategory?>(null) }
    var peopleCount by remember { mutableStateOf("1") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar SOS") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Danger,
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
                text = "Tipo de emergencia",
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.OnBackground,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Grid de categorías
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(280.dp)
            ) {
                items(SosCategory.entries) { category ->
                    SosCategoryCard(
                        category = category,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Número de personas
            OutlinedTextField(
                value = peopleCount,
                onValueChange = { peopleCount = it.filter { c -> c.isDigit() }.take(3) },
                label = { Text("Personas afectadas", color = RescueMeshColors.OnBackground) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RescueMeshColors.Danger,
                    unfocusedBorderColor = RescueMeshColors.OnBackground.copy(alpha = 0.5f),
                    focusedTextColor = RescueMeshColors.OnBackground,
                    unfocusedTextColor = RescueMeshColors.OnBackground,
                    cursorColor = RescueMeshColors.Danger
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    selectedCategory?.let { cat ->
                        onSend(cat, "", peopleCount.toIntOrNull() ?: 1)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Danger
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedCategory != null
            ) {
                Text(
                    text = " ENVIAR SOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SosCategoryCard(
    category: SosCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (emoji, label) = when (category) {
        SosCategory.MEDICAL -> "Medical:" to "Médico"
        SosCategory.FIRE -> "Fire:" to "Fuego"
        SosCategory.TRAPPED -> "Trapped:" to "Atrapado"
        SosCategory.CHILDREN -> "Children:" to "Niños"
        SosCategory.ELDERLY -> "Elderly:" to "Adulto mayor"
        SosCategory.INJURED -> "Injured:" to "Herido"
        SosCategory.OTHER -> "Other:" to "Otro"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) RescueMeshColors.Danger else RescueMeshColors.Surface
        ),
        border = if (isSelected) null else CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else RescueMeshColors.OnSurface
            )
        }
    }
}
