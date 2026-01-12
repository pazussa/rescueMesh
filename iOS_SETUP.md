# iOS Setup Guide

## Estado del Proyecto iOS

La implementación de RescueMesh para iOS está actualmente **en desarrollo**. El proyecto incluye la estructura base de Kotlin Multiplatform para iOS, pero requiere configuración adicional para completar la instalación.

## Requisitos Previos

- **macOS** con Xcode 14.0 o superior
- **Xcode Command Line Tools** instalados
- **CocoaPods** (opcional, dependiendo de las dependencias)
- Cuenta de Apple Developer (para probar en dispositivos físicos)

## Estructura del Proyecto iOS

```
iosApp/
├── iosApp/
│   ├── AppDelegate.swift
│   ├── Info.plist
│   ├── MultipeerService.swift
│   └── Base.lproj/
└── iosApp.xcodeproj/
    └── project.pbxproj
```

## Pasos para Configurar el Proyecto iOS

### 1. Generar el Framework de Kotlin

Primero, necesitas compilar el código compartido de Kotlin para iOS:

```bash
cd rescuemesh
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

Para el simulador iOS:
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### 2. Abrir el Proyecto en Xcode

```bash
open iosApp/iosApp.xcodeproj
```

### 3. Configurar el Bundle Identifier

1. Selecciona el proyecto en el navegador de Xcode
2. En la pestaña "Signing & Capabilities"
3. Cambia el **Bundle Identifier** a uno único (ejemplo: `com.tuorganizacion.rescuemesh`)
4. Selecciona tu **Team** (cuenta de Apple Developer)

### 4. Verificar las Dependencias

Asegúrate de que el framework de Kotlin esté correctamente enlazado:

1. En Xcode, ve a **Build Phases**
2. Verifica que en "Link Binary With Libraries" esté incluido el framework compilado
3. El framework debería estar en: `composeApp/build/bin/iosArm64/debugFramework/`

### 5. Configurar Permisos en Info.plist

RescueMesh requiere ciertos permisos para funcionar. Agrega las siguientes claves a tu `Info.plist`:

```xml
<key>NSBluetoothAlwaysUsageDescription</key>
<string>RescueMesh necesita Bluetooth para comunicación de emergencia offline</string>

<key>NSBluetoothPeripheralUsageDescription</key>
<string>RescueMesh usa Bluetooth para crear redes mesh de emergencia</string>

<key>NSLocalNetworkUsageDescription</key>
<string>RescueMesh necesita acceso a la red local para comunicación peer-to-peer</string>

<key>NSLocationWhenInUseUsageDescription</key>
<string>RescueMesh usa tu ubicación para compartir tu posición en emergencias</string>

<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>RescueMesh necesita tu ubicación para funciones de emergencia</string>
```

### 6. Implementar el Protocolo Mesh para iOS

La implementación de iOS requiere usar **MultipeerConnectivity** o **Network.framework** para replicar la funcionalidad de mesh networking. El archivo `MultipeerService.swift` contiene el esqueleto para esto.

**Pendientes de implementación:**
- Configuración de MultipeerConnectivity para descubrimiento de peers
- Protocolo de sincronización de mensajes
- Integración con el código compartido de Kotlin
- Manejo de estados de conexión y desconexión

### 7. Compilar y Ejecutar

Una vez configurado:

1. Selecciona un dispositivo o simulador iOS en Xcode
2. Presiona **Cmd + R** para compilar y ejecutar
3. Acepta los permisos cuando se soliciten

## Problemas Comunes

### Error: "Framework not found"

Si obtienes un error de framework no encontrado:
```bash
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

Luego, en Xcode, limpia el build:
- **Product > Clean Build Folder** (Shift + Cmd + K)

### Error de Firma de Código

Si ves errores de firma:
1. Ve a **Signing & Capabilities** en Xcode
2. Desactiva y reactiva "Automatically manage signing"
3. Asegúrate de tener un perfil de aprovisionamiento válido

### El Framework de Kotlin no se Actualiza

Después de cambios en el código compartido:
```bash
./gradlew clean
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

Luego en Xcode: **Product > Clean Build Folder**

## Estado de Características iOS

| Característica | Estado |
|---|---|
| UI Compartida (Compose Multiplatform) | ✅ Implementado |
| Lógica de Negocio Compartida | ✅ Implementado |
| Mesh Networking (MultipeerConnectivity) | ⚠️ En desarrollo |
| Almacenamiento Local | ⚠️ Pendiente |
| Notificaciones | ⚠️ Pendiente |
| Compartir APK/IPA | ❌ Pendiente |

## Próximos Pasos

Para completar la implementación iOS:

1. **Implementar MultipeerConnectivity**
   - Crear un servicio de descubrimiento de peers
   - Implementar protocolo de comunicación
   - Sincronizar con el modelo de datos compartido

2. **Almacenamiento Persistente**
   - Usar UserDefaults o CoreData para mensajes
   - Implementar sincronización offline

3. **Notificaciones Push Locales**
   - Alertas para mensajes SOS
   - Notificaciones de nuevos mensajes

4. **Testing en Dispositivos Reales**
   - La comunicación mesh requiere dispositivos físicos
   - El simulador no soporta Bluetooth/MultipeerConnectivity completamente

## Recursos Adicionales

- [Kotlin Multiplatform for iOS](https://kotlinlang.org/docs/multiplatform-mobile-getting-started.html)
- [MultipeerConnectivity Framework](https://developer.apple.com/documentation/multipeerconnectivity)
- [Compose Multiplatform for iOS](https://www.jetbrains.com/lp/compose-multiplatform/)

## Contacto

Para preguntas sobre la implementación iOS:
- Email: juanpa1@unicaucax.edu.co
- Repository: https://github.com/pazussa/rescueMesh

---

**Nota**: La plataforma principal completamente funcional es **Android**. La versión iOS está en desarrollo activo y contribuciones son bienvenidas.
