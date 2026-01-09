package com.rescuemesh.app

import androidx.compose.ui.window.ComposeUIViewController
import com.rescuemesh.app.platform.IOSAppContext

/**
 * Main entry point for iOS - creates the Compose UI view controller
 */
fun MainViewController() = ComposeUIViewController {
    // Initialize iOS context
    IOSAppContext.initialize()
    
    // Create and display the shared App composable
    IOSApp()
}
