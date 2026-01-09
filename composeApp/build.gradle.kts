import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Export all public APIs
            export(project)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.play.services.nearby)
            implementation(libs.play.services.location)
            implementation(libs.zxing.core)
        }
        
        iosMain.dependencies {
            // iOS-specific dependencies (if any)
        }
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
        }
    }
}

// Task to build iOS framework for Xcode
tasks.register("assembleXCFramework") {
    dependsOn("linkDebugFrameworkIosArm64", "linkDebugFrameworkIosX64", "linkDebugFrameworkIosSimulatorArm64")
    dependsOn("linkReleaseFrameworkIosArm64", "linkReleaseFrameworkIosX64", "linkReleaseFrameworkIosSimulatorArm64")
}

// Sync framework to Xcode build directory
tasks.register<Sync>("syncFrameworkToXcode") {
    val configuration = System.getenv("CONFIGURATION") ?: "Debug"
    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
    
    val targetName = when {
        sdkName.startsWith("iphoneos") -> "iosArm64"
        sdkName.startsWith("iphonesimulator") -> {
            val arch = System.getenv("NATIVE_ARCH") ?: System.getProperty("os.arch")
            if (arch == "arm64") "iosSimulatorArm64" else "iosX64"
        }
        else -> "iosSimulatorArm64"
    }
    
    val frameworkDir = layout.buildDirectory.dir("bin/$targetName/${configuration.lowercase()}Framework")
    val outputDir = layout.buildDirectory.dir("xcode-frameworks/$configuration/$sdkName")
    
    from(frameworkDir)
    into(outputDir)
}

android {
    namespace = "com.rescuemesh.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.rescuemesh.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
