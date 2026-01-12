#!/bin/bash
# RescueMesh Desktop - Linux
# Script de ejecución para Linux

# Colores para la salida
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # Sin color

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}  RescueMesh Desktop - Linux${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# Verificar si Java está instalado
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java no está instalado.${NC}"
    echo -e "${YELLOW}Por favor, instala Java JDK 11 o superior:${NC}"
    echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
    echo "  Fedora: sudo dnf install java-17-openjdk"
    echo "  Arch: sudo pacman -S jdk-openjdk"
    exit 1
fi

# Mostrar versión de Java
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo -e "Java versión: ${GREEN}$JAVA_VERSION${NC}"
echo ""

# Ejecutar la aplicación
echo -e "${YELLOW}Iniciando RescueMesh...${NC}"
echo ""

java -jar RescueMesh-linux-x64-1.0.0.jar

# Verificar el código de salida
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}RescueMesh cerrado correctamente.${NC}"
else
    echo ""
    echo -e "${RED}RescueMesh terminó con errores.${NC}"
    exit 1
fi
