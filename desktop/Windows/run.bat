@echo off
REM RescueMesh Desktop - Windows
REM Script de ejecución para Windows

title RescueMesh Desktop - Windows
color 0A

echo ================================
echo   RescueMesh Desktop - Windows
echo ================================
echo.

REM Verificar si Java está instalado
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    color 0C
    echo Error: Java no esta instalado.
    echo.
    echo Por favor, instala Java JDK 11 o superior desde:
    echo https://www.oracle.com/java/technologies/downloads/
    echo o
    echo https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Mostrar versión de Java
echo Verificando version de Java...
java -version
echo.

REM Ejecutar la aplicación
echo Iniciando RescueMesh...
echo.

java -jar RescueMesh.jar

REM Verificar el código de salida
if %ERRORLEVEL% EQU 0 (
    echo.
    echo RescueMesh cerrado correctamente.
) else (
    color 0C
    echo.
    echo RescueMesh termino con errores.
    pause
    exit /b 1
)

pause
