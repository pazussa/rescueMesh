# RescueMesh Desktop - Windows

## Requisitos

- **Java JDK 11 o superior**
- Windows 10 o superior

## Instalación de Java

### Descargar Java
Descarga e instala Java desde una de estas opciones:

1. **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/
2. **Eclipse Temurin (recomendado)**: https://adoptium.net/

### Verificar instalación
Abre CMD o PowerShell y ejecuta:
```cmd
java -version
```

Deberías ver algo como:
```
openjdk version "17.0.x" ...
```

## Ejecución

### Método 1: Doble clic
Simplemente haz **doble clic** en `run.bat`

### Método 2: Desde CMD
```cmd
run.bat
```

### Método 3: Ejecución directa
```cmd
java -jar RescueMesh.jar
```

## Características

- **Centro de operaciones de emergencia** - Monitorea y coordina la actividad de la red mesh
- **Testing y desarrollo** - Prueba protocolos de red mesh sin dispositivos móviles
- **Monitoreo de mensajes** - Ve y gestiona comunicaciones de emergencia en pantalla grande
- **Multiplataforma Kotlin** - Código compartido con versiones móviles

## Solución de Problemas

### Error: "java no se reconoce como un comando"
Java no está instalado o no está en el PATH del sistema.

**Solución:**
1. Instala Java desde los enlaces anteriores
2. Durante la instalación, asegúrate de marcar "Add to PATH"
3. Reinicia tu computadora
4. Verifica con `java -version` en CMD

### Configurar Java en PATH manualmente
1. Busca "Variables de entorno" en el menú inicio
2. Click en "Variables de entorno"
3. En "Variables del sistema", busca "Path"
4. Agrega la ruta de instalación de Java (ej: `C:\Program Files\Java\jdk-17\bin`)
5. Reinicia CMD y prueba

### La aplicación no inicia
Verifica que tienes Java 11 o superior:
```cmd
java -version
```

### Problemas de red
RescueMesh Desktop usa multicast UDP para comunicación. 

**Configurar Firewall de Windows:**
1. Abre "Firewall de Windows Defender"
2. Click en "Configuración avanzada"
3. Click en "Reglas de entrada"
4. Click en "Nueva regla"
5. Tipo: Puerto
6. Protocolo: UDP, Puerto: 5555
7. Acción: Permitir la conexión
8. Nombre: RescueMesh Desktop

### Antivirus bloquea la aplicación
Algunos antivirus pueden bloquear archivos JAR. Agrega una excepción para:
- `RescueMesh.jar`
- Carpeta completa de RescueMesh Desktop

## Características de Red Desktop

La versión desktop usa **MulticastSocket** para comunicación P2P:
- Alcance en la red local (LAN)
- No requiere conexión a Internet
- Compatible con múltiples instancias en la misma red
- Protocolo de sincronización automática

## Ejecutar múltiples instancias

Para probar la comunicación entre múltiples instancias:
1. Abre varias ventanas de CMD
2. En cada una ejecuta `run.bat`
3. Las instancias se detectarán automáticamente en la misma red

## Contacto

- Email: juanpa1@unicaucax.edu.co
- Repository: https://github.com/pazussa/rescueMesh

---

**Nota**: La versión desktop está diseñada principalmente para testing y como centro de coordinación. La funcionalidad completa de mesh networking está en la versión Android.
