package com.rescuemesh.app

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/**
 * Desktop entry point for RescueMesh
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RescueMesh - Emergency Communication",
        state = rememberWindowState(
            size = DpSize(900.dp, 700.dp)
        )
    ) {
        DesktopApp()
    }
}
