@echo off
setlocal enabledelayedexpansion
REM Genera la distribucion portable COMPLETA de los dos juegos -- Five Doors
REM At Freddy's (Swing) + Five Doors Escape (LibGDX), cada uno con su propio
REM runtime Java embebido -- lista para copiarse entera a otra maquina y
REM funcionar sin instalar NADA ahi (ni JDK 8, ni JDK 21, ni Gradle).
REM
REM Investigado y probado 2026-08-10 (ronda de distribucion final). Reutiliza
REM build-portable.bat (Swing, ya existente) + gradlew lwjgl3:installDist
REM (Escape, ya soportado por el plugin 'application' sin cambios) -- este
REM script solo orquesta ambos y arma la carpeta final con la convencion de
REM nombres que LanzadorEscape.resolverDistribucionEmpaquetada() espera.
REM
REM Requisitos SOLO en esta maquina (la de destino final no necesita nada):
REM   - JDK 8 con JavaFX (mismo requisito que build-portable.bat, ver SETUP.md)
REM   - JDK 21
REM   - five_doors_escape clonado como carpeta hermana de este repo
REM   - Conexion a internet (para que Gradle descargue dependencias, una sola vez)
REM
REM Uso: build-distribucion.bat ["ruta JDK8"] ["ruta JDK21"]
REM Ambos argumentos son opcionales -- se autodetectan si no se pasan.

set SCRIPT_DIR=%~dp0
set JDK8_ARG=%~1
set JDK21=%~2
set DIST_ROOT=%SCRIPT_DIR%dist-completa

echo === 1/4: Construyendo Five Doors At Freddy's (build-portable.bat) ===
call "%SCRIPT_DIR%FiveDoorsAtFreddys\build-portable.bat" %JDK8_ARG%
if errorlevel 1 (
    echo ERROR: fallo el build de Five Doors At Freddy's.
    exit /b 1
)

echo === 2/4: Detectando JDK 21 ===
if "%JDK21%"=="" (
    if defined JAVA21_HOME set JDK21=%JAVA21_HOME%
)
if "%JDK21%"=="" (
    for /d %%D in ("C:\Program Files\Java\jdk-21*") do set JDK21=%%D
)
if "%JDK21%"=="" (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do set JDK21=%%D
)
if "%JDK21%"=="" (
    echo ERROR: no se encontro un JDK 21 instalado. Pasalo como segundo argumento:
    echo   build-distribucion.bat "" "C:\ruta\a\tu\jdk-21"
    exit /b 1
)
if not exist "%JDK21%\bin\java.exe" (
    echo ERROR: "%JDK21%" no parece un JDK valido ^(falta bin\java.exe^).
    exit /b 1
)
echo Usando JDK 21: %JDK21%

echo === 3/4: Construyendo Five Doors Escape ^(gradlew lwjgl3:installDist^) ===
set ESCAPE_DIR=%SCRIPT_DIR%..\five_doors_escape
if not exist "%ESCAPE_DIR%\gradlew.bat" (
    echo ERROR: no se encontro five_doors_escape como carpeta hermana de este repo
    echo ^(%ESCAPE_DIR%^) -- clonalo antes de correr este script ^(ver SETUP.md^).
    exit /b 1
)
pushd "%ESCAPE_DIR%"
set JAVA_HOME=%JDK21%
REM Ruta completa, NUNCA el nombre pelado "gradlew.bat" -- esta maquina no
REM busca el directorio actual implicitamente al resolver comandos
REM (confirmado real: "gradlew.bat" a secas falla con "no se reconoce...",
REM la misma ruta completa funciona sin problema).
call "%ESCAPE_DIR%\gradlew.bat" lwjgl3:installDist
if errorlevel 1 (
    echo ERROR: fallo "gradlew lwjgl3:installDist".
    popd
    exit /b 1
)
popd

echo === 4/4: Ensamblando la distribucion final en %DIST_ROOT% ===
if exist "%DIST_ROOT%" rmdir /s /q "%DIST_ROOT%"
mkdir "%DIST_ROOT%"

xcopy /e /i /q "%SCRIPT_DIR%FiveDoorsAtFreddys\dist\FiveDoorsAtFreddys" "%DIST_ROOT%\FiveDoorsAtFreddys\" >nul

mkdir "%DIST_ROOT%\FiveDoorsEscape" >nul
xcopy /e /i /q "%ESCAPE_DIR%\lwjgl3\build\install\lwjgl3" "%DIST_ROOT%\FiveDoorsEscape\lwjgl3\" >nul
echo Copiando runtime JDK 21 ^(puede tardar, son varios cientos de MB^)...
xcopy /e /i /q "%JDK21%\*" "%DIST_ROOT%\FiveDoorsEscape\runtime\" >nul

echo.
echo Listo. Distribucion completa en: %DIST_ROOT%
echo Copia esa carpeta ENTERA a otra maquina y ejecuta:
if exist "%DIST_ROOT%\FiveDoorsAtFreddys\FiveDoorsAtFreddys.exe" (
    echo   FiveDoorsAtFreddys\FiveDoorsAtFreddys.exe
) else (
    echo   FiveDoorsAtFreddys\FiveDoorsAtFreddys.bat  ^(no se pudo generar el .exe esta vez, ver arriba^)
)
echo No necesita Java/Gradle instalado en la maquina destino. La transicion
echo Noche 5 -^> Escape usa automaticamente FiveDoorsEscape\ ^(carpeta hermana^)
echo en vez de gradlew -- ver LanzadorEscape.resolverDistribucionEmpaquetada().
endlocal
