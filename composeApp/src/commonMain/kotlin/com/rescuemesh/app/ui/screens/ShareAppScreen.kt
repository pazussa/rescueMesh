package com.rescuemesh.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rescuemesh.app.localization.LanguageManager
import com.rescuemesh.app.localization.rememberStrings
import com.rescuemesh.app.ui.theme.RescueMeshColors

/**
 * Screen for sharing the app with other users in emergency situations.
 * 
 * CRITICAL FEATURE: In emergencies, most people won't have the app installed.
 * This screen:
 * 1. Clearly explains that automatic notifications are NOT possible
 * 2. HACK: Broadcasts emergency via Bluetooth name (visible to everyone!)
 * 3. Provides easy ways to share the APK manually
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAppScreen(
    appVersionName: String,
    appVersionCode: Int,
    appSizeMb: String,
    isEmergencyBroadcastActive: Boolean = false,
    emergencyBroadcastStatus: String = "",
    onBack: () -> Unit,
    onShareBluetooth: () -> Unit,
    onShareGeneral: () -> Unit,
    onStartEmergencyBroadcast: () -> Unit = {},
    onStopEmergencyBroadcast: () -> Unit = {}
) {
    val strings = rememberStrings()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isEnglish = currentLanguage == LanguageManager.Language.ENGLISH
    val scrollState = rememberScrollState()
    
    // Pulsating animation for emergency broadcast button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isEnglish) " Share App to Save Lives" else " Comparte la App para Salvar Vidas")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = strings.back
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RescueMeshColors.Emergency,
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ================================================================
            // HACK: EMERGENCY BROADCAST - Makes device visible to everyone!
            // ================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isEmergencyBroadcastActive) {
                            Modifier.alpha(pulseAlpha)
                        } else {
                            Modifier
                        }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isEmergencyBroadcastActive) 
                        RescueMeshColors.Success 
                    else 
                        RescueMeshColors.Emergency
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isEmergencyBroadcastActive) "Signal: BROADCASTING..." else "Signal: HACK: EMERGENCY SIGNAL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isEnglish)
                            if (isEmergencyBroadcastActive)
                                "Your device is now visible as 'SOS-RESCUEMESH' to EVERYONE scanning Bluetooth!"
                            else
                                "Make your device name visible to ALL nearby phones (no app needed!)"
                        else
                            if (isEmergencyBroadcastActive)
                                "¡Tu dispositivo ahora es visible como 'SOS-RESCUEMESH' para TODOS los que busquen Bluetooth!"
                            else
                                "Haz visible el nombre de tu dispositivo a TODOS los teléfonos cercanos (¡sin app necesaria!)",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            if (isEmergencyBroadcastActive) {
                                onStopEmergencyBroadcast()
                            } else {
                                onStartEmergencyBroadcast()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEmergencyBroadcastActive) 
                                Color.White.copy(alpha = 0.2f) 
                            else 
                                Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isEmergencyBroadcastActive) 
                                (if (isEnglish) "STOP STOP BROADCAST" else "STOP DETENER TRANSMISIÓN")
                            else 
                                (if (isEnglish) "Broadcast: START EMERGENCY BROADCAST" else "Broadcast: INICIAR TRANSMISIÓN DE EMERGENCIA"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEmergencyBroadcastActive) Color.White else RescueMeshColors.Emergency
                        )
                    }
                    
                    if (emergencyBroadcastStatus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = emergencyBroadcastStatus,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    if (isEmergencyBroadcastActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isEnglish)
                                    " Tell people: \"Look for 'SOS-RESCUEMESH' in your Bluetooth devices!\""
                                else
                                    " Dile a la gente: \"¡Busca 'SOS-RESCUEMESH' en tus dispositivos Bluetooth!\"",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // App Info Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "RescueMesh",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = RescueMeshColors.OnSurface
                        )
                        Text(
                            text = "v$appVersionName • $appSizeMb MB",
                            fontSize = 14.sp,
                            color = RescueMeshColors.OnSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (isEnglish) "Offline Emergency Network" else "Red de Emergencia Offline",
                            fontSize = 12.sp,
                            color = RescueMeshColors.Primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Share Buttons
            Text(
                text = if (isEnglish) " SHARE NOW TO NEARBY DEVICES" else " COMPARTE AHORA A DISPOSITIVOS CERCANOS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = RescueMeshColors.Emergency,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Primary: Bluetooth Share
            Button(
                onClick = onShareBluetooth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Signal: ${if (isEnglish) "Share via Bluetooth" else "Compartir por Bluetooth"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Secondary: General Share (Nearby Share, etc.)
            Button(
                onClick = onShareGeneral,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RescueMeshColors.Secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = " ${if (isEnglish) "Nearby Share / Quick Share" else "Nearby Share / Quick Share"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // How it works
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RescueMeshColors.Info.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = " ${if (isEnglish) "HOW TO SHARE:" else "CÓMO COMPARTIR:"}",
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.Info,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val steps = if (isEnglish) listOf(
                        "1️⃣ Press a share button above",
                        "2️⃣ Select the recipient device",
                        "3️⃣ Wait for transfer to complete",
                        "4️⃣ REPEAT for each person nearby!",
                        "5️⃣ Tell them to install and join your room"
                    ) else listOf(
                        "1️⃣ Presiona un botón de compartir arriba",
                        "2️⃣ Selecciona el dispositivo receptor",
                        "3️⃣ Espera que complete la transferencia",
                        "4️⃣ ¡REPITE para cada persona cercana!",
                        "5️⃣ Diles que instalen y se unan a tu sala"
                    )
                    
                    steps.forEach { step ->
                        Text(
                            text = step,
                            color = RescueMeshColors.OnBackground,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instructions for recipient
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
                        text = if (isEnglish) 
                            " TELL THE RECIPIENT:" 
                        else 
                            " DILE AL RECEPTOR:",
                        fontWeight = FontWeight.Bold,
                        color = RescueMeshColors.OnSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isEnglish)
                            """
                            OK: Accept the file transfer
                            OK: Tap the downloaded APK file
                            OK: Allow "Install from unknown sources"
                            OK: Open RescueMesh after install
                            OK: Tap "Join Incident Room"
                            OK: Enter the room code I give you
                            """.trimIndent()
                        else
                            """
                            OK: Acepta la transferencia del archivo
                            OK: Toca el archivo APK descargado
                            OK: Permite "Instalar de fuentes desconocidas"
                            OK: Abre RescueMesh después de instalar
                            OK: Toca "Unirse a Sala de Incidente"
                            OK: Ingresa el código de sala que te doy
                            """.trimIndent(),
                        color = RescueMeshColors.OnSurface.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 22.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Reminder text
            Text(
                text = if (isEnglish) 
                    " Every phone you share with can help save more lives!" 
                else 
                    " ¡Cada teléfono con el que compartas puede ayudar a salvar más vidas!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RescueMeshColors.Success,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
