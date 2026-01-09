package com.rescuemesh.app.platform

/**
 * iOS Application Context holder
 */
object IOSAppContext {
    private var initialized = false
    
    fun initialize() {
        if (!initialized) {
            initialized = true
            // Any iOS-specific initialization can go here
        }
    }
    
    fun isInitialized(): Boolean = initialized
}
