@echo off
setlocal
title Pantera Fitness - servidor
cd /d "%~dp0"
set PUERTO=8090

echo ==========================================================
echo                     PANTERA FITNESS
echo ==========================================================
echo.
echo  Iniciando el sistema. Puede tardar entre 15 y 40 segundos.
echo  El navegador se abrira solo en http://localhost:%PUERTO%
echo.
echo  Usuarios de prueba (contrasena 123456):
echo    cliente@panterfitness.com    - Cliente
echo    profesor@panterfitness.com   - Profesor
echo    admin@panterfitness.com      - Administrador
echo    vencido@panterfitness.com    - Cliente con membresia vencida
echo    sancionado@panterfitness.com - Cliente con penalizacion
echo.
echo  Para CERRAR el sistema, cerra esta ventana.
echo ==========================================================
echo.

if not exist "%~dp0jre\bin\java.exe" (
  echo [ERROR] No se encontro la carpeta jre. Descomprimi el .zip completo antes de abrirlo.
  pause
  exit /b 1
)

rem Abre el navegador cuando el servidor responde (sin bloquear esta ventana).
start "" /min powershell -NoProfile -WindowStyle Hidden -Command "for($i=0;$i -lt 120;$i++){try{Invoke-WebRequest -UseBasicParsing -TimeoutSec 2 'http://localhost:%PUERTO%/api/health' | Out-Null; Start-Process 'http://localhost:%PUERTO%/login'; break}catch{Start-Sleep -Seconds 2}}"

"%~dp0jre\bin\java.exe" -Xmx512m -jar "%~dp0app\pantera-fitness.jar" --spring.profiles.active=h2 --server.port=%PUERTO% --spring.h2.console.enabled=false

echo.
echo El servidor se detuvo. Si aparecio un error de puerto en uso, cerra otras ventanas de Pantera Fitness.
pause
