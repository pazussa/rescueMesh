package com.rescuemesh.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.rescuemesh.app.ui.theme.RescueMeshColors
import com.rescuemesh.app.viewmodel.RescueMeshViewModel

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: RescueMeshViewModel
    
    // Permisos requeridos para Nearby Connections
    private val requiredPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }
    }.toTypedArray()
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            Toast.makeText(
                this,
                "Se requieren permisos para crear la red mesh",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = RescueMeshViewModel(applicationContext)
        
        // Solicitar permisos
        requestPermissionsIfNeeded()
        
        setContent {
            RescueMeshTheme {
                App(viewModel)
            }
        }
    }
    
    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // El viewModel limpiará las conexiones en onCleared
    }
}

@Composable
fun RescueMeshTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = RescueMeshColors.Primary,
        secondary = RescueMeshColors.Secondary,
        background = RescueMeshColors.Background,
        surface = RescueMeshColors.Surface,
        onPrimary = RescueMeshColors.OnPrimary,
        onBackground = RescueMeshColors.OnBackground,
        onSurface = RescueMeshColors.OnSurface,
        error = RescueMeshColors.Danger
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
