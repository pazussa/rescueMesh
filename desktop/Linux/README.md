# RescueMesh Desktop - Linux

## Requisitos

- **Java JDK 11 o superior**
- Sistema operativo Linux (cualquier distribución)

## Instalación de Java

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Fedora
```bash
sudo dnf install java-17-openjdk
```

### Arch Linux
```bash
sudo pacman -S jdk-openjdk
```

### Verificar instalación
```bash
java -version
```

## Ejecución

### Método 1: Script de ejecución
```bash
chmod +x run.sh
./run.sh
```

### Método 2: Ejecución directa
```bash
java -jar RescueMesh-linux-x64-1.0.0.jar
```

## Características

- **Centro de operaciones de emergencia** - Monitorea y coordina la actividad de la red mesh
- **Testing y desarrollo** - Prueba protocolos de red mesh sin dispositivos móviles
- **Monitoreo de mensajes** - Ve y gestiona comunicaciones de emergencia en pantalla grande
- **Multiplataforma Kotlin** - Código compartido con versiones móviles

## Solución de Problemas

### Error: "java: command not found"
Java no está instalado. Instala OpenJDK siguiendo las instrucciones anteriores.

### Error: "Could not find or load main class"
Asegúrate de estar en el directorio correcto donde está el archivo JAR.

### La aplicación no inicia
Verifica que tienes Java 11 o superior:
```bash
java -version
```

### Problemas de red
RescueMesh Desktop usa multicast UDP para comunicación. Asegúrate de que tu firewall permita:
- Puerto UDP: 5555
- Multicast: 230.0.0.1

Configurar firewall (Ubuntu):
```bash
sudo ufw allow 5555/udp
```

## Características de Red Desktop

La versión desktop usa **MulticastSocket** para comunicación P2P:
- Alcance en la red local (LAN)
- No requiere conexión a Internet
- Compatible con múltiples instancias en la misma red
- Protocolo de sincronización automática

## Contacto

- Email: juanpa1@unicaucax.edu.co
- Repository: https://github.com/pazussa/rescueMesh

---

**Nota**: La versión desktop está diseñada principalmente para testing y como centro de coordinación. La funcionalidad completa de mesh networking está en la versión Android.
