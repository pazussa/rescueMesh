package com.rescuemesh.app.sharing

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * AppSharing - Allows sharing the APK file with nearby devices
 * 
 * In emergency situations, most people won't have the app installed.
 * This feature enables:
 * - Sharing the APK via Bluetooth
 * - Sharing via WiFi Direct
 * - Sharing via any available sharing method (AirDrop-like)
 * 
 * This is critical for rapid deployment in disaster scenarios where
 * internet connectivity may not be available.
 */
object AppSharing {
    
    private const val TAG = "AppSharing"
    private const val APK_FILENAME = "RescueMesh.apk"
    
    /**
     * Gets the APK file path of the current application
     */
    private fun getApkFile(context: Context): File? {
        return try {
            val appInfo: ApplicationInfo = context.applicationInfo
            File(appInfo.sourceDir)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Copies the APK to a shareable location and returns a content URI
     */
    private fun getShareableApkUri(context: Context): Uri? {
        return try {
            val apkFile = getApkFile(context) ?: return null
            
            // Copy APK to cache directory for sharing
            val shareDir = File(context.cacheDir, "share")
            shareDir.mkdirs()
            
            val shareFile = File(shareDir, APK_FILENAME)
            
            // Copy the APK file
            apkFile.inputStream().use { input ->
                FileOutputStream(shareFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Get content URI using FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                shareFile
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Creates an intent to share the APK file
     * Users can choose Bluetooth, WiFi Direct, or any other sharing method
     */
    fun createShareIntent(context: Context): Intent? {
        val apkUri = getShareableApkUri(context) ?: return null
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            putExtra(Intent.EXTRA_SUBJECT, "RescueMesh - Emergency Network App")
            putExtra(
                Intent.EXTRA_TEXT,
                """
                🆘 RescueMesh - Offline Emergency Network
                
                Install this app to join the emergency mesh network.
                No internet required - works via Bluetooth/WiFi.
                
                Instructions:
                1. Install the APK
                2. Grant permissions when asked
                3. Create or join an Incident Room
                
                Stay safe! 🙏
                """.trimIndent()
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Launches the share dialog for the APK
     */
    fun shareApp(context: Context): Boolean {
        val shareIntent = createShareIntent(context) ?: return false
        
        val chooserIntent = Intent.createChooser(
            shareIntent,
            "Share RescueMesh via..."
        )
        
        return try {
            context.startActivity(chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Creates an intent specifically for Bluetooth sharing
     */
    fun createBluetoothShareIntent(context: Context): Intent? {
        val apkUri = getShareableApkUri(context) ?: return null
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, apkUri)
            setPackage("com.android.bluetooth")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Gets app version info for display
     */
    fun getAppVersionInfo(context: Context): AppVersionInfo {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
            
            val apkFile = getApkFile(context)
            val apkSizeBytes = apkFile?.length() ?: 0L
            val apkSizeMB = apkSizeBytes / (1024.0 * 1024.0)
            
            AppVersionInfo(
                versionName = packageInfo.versionName ?: "1.0",
                versionCode = versionCode.toInt(),
                fileSizeMb = String.format("%.1f", apkSizeMB)
            )
        } catch (e: Exception) {
            AppVersionInfo("1.0", 1, "~10")
        }
    }
}

/**
 * Data class containing app version information
 */
data class AppVersionInfo(
    val versionName: String,
    val versionCode: Int,
    val fileSizeMb: String
)
