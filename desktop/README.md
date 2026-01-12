# RescueMesh Desktop

Aplicación de escritorio multiplataforma para RescueMesh, diseñada como centro de operaciones y herramienta de testing para la red mesh de emergencias.

## 📁 Estructura

```
desktop/
├── Linux/              # Versión para Linux
│   ├── RescueMesh-linux-x64-1.0.0.jar
│   ├── run.sh         # Script de ejecución
│   └── README.md      # Instrucciones específicas de Linux
├── Windows/           # Versión para Windows
│   ├── RescueMesh.jar
│   ├── run.bat        # Script de ejecución
│   └── README.md      # Instrucciones específicas de Windows
└── README.md          # Este archivo
```

## 🚀 Inicio Rápido

### Linux
```bash
cd Linux
chmod +x run.sh
./run.sh
```

### Windows
1. Navega a la carpeta `Windows`
2. Haz doble clic en `run.bat`

## 📋 Requisitos

- **Java JDK 11 o superior** (requerido para ambas plataformas)
- Sistema operativo: Windows 10+ o cualquier distribución Linux moderna

## 🔧 Instalación de Java

### Linux
```bash
# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# Fedora
sudo dnf install java-17-openjdk

# Arch
sudo pacman -S jdk-openjdk
```

### Windows
Descarga desde:
- https://adoptium.net/ (recomendado)
- https://www.oracle.com/java/technologies/downloads/

## 🌟 Características

- **Centro de Operaciones** - Monitorea toda la actividad de la red mesh
- **Testing Multi-instancia** - Ejecuta múltiples instancias para probar comunicación
- **Visualización en tiempo real** - Ve mensajes SOS, recursos y alertas
- **Sincronización automática** - Detecta y conecta con otros nodos automáticamente
- **UI moderna** - Interfaz construida con Compose Multiplatform

## 🔌 Comunicación de Red

La versión desktop usa **MulticastSocket UDP** para comunicación P2P:

- **Puerto**: 5555 (UDP)
- **Grupo Multicast**: 230.0.0.1
- **Alcance**: Red local (LAN)
- **Sin Internet**: Funciona completamente offline

### Configurar Firewall

**Linux (UFW):**
```bash
sudo ufw allow 5555/udp
```

**Windows:**
Ver instrucciones detalladas en `Windows/README.md`

## 🧪 Testing Local

Para probar la comunicación entre múltiples nodos:

1. Abre múltiples terminales/ventanas CMD
2. Ejecuta la aplicación en cada una
3. Crea o únete a la misma sala de incidentes
4. Los nodos se detectarán automáticamente

## 📊 Diferencias con la Versión Móvil

| Característica | Android | Desktop |
|---|---|---|
| Mesh Networking | Google Nearby Connections | MulticastSocket UDP |
| Alcance | ~100m por salto | Red local (LAN) |
| Bluetooth | ✅ Sí | ❌ No |
| WiFi Direct | ✅ Sí | ❌ No |
| Ubicación GPS | ✅ Sí | ⚠️ Simulado |
| UI Táctil | ✅ Optimizada | ⚠️ Mouse/Teclado |
| Propósito | Uso en campo | Testing/Coordinación |

## 🛠️ Compilar desde Código Fuente

Si deseas compilar la aplicación desktop tú mismo:

```bash
# Desde la raíz del proyecto rescuemesh
./gradlew :composeApp:packageUberJarForCurrentOS
```

El JAR se generará en:
```
composeApp/build/compose/jars/
```

## 📖 Documentación Adicional

- **Linux**: Ver `Linux/README.md`
- **Windows**: Ver `Windows/README.md`
- **Proyecto Principal**: Ver `/README.md` en la raíz

## 🐛 Solución de Problemas

### Java no encontrado
Asegúrate de que Java está instalado y en el PATH:
```bash
java -version
```

### No se detectan otros nodos
1. Verifica que están en la misma red local
2. Comprueba la configuración del firewall
3. Asegúrate de que están usando el mismo código de sala

### Problemas de rendimiento
La aplicación desktop puede consumir más recursos que la móvil. Recomendado:
- 4GB RAM mínimo
- Procesador dual-core o superior

## 📝 Notas

- La versión desktop está optimizada para **testing y coordinación**
- Para uso en campo real, usa la **versión Android**
- El JAR es el mismo para Windows y Linux (Java multiplataforma)

## 📧 Contacto

- **Email**: juanpa1@unicaucax.edu.co
- **Repository**: https://github.com/pazussa/rescueMesh

---

Desarrollado para el Kotlin Multiplatform Contest 2025/2026 ❤️
