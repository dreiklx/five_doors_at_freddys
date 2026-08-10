@echo off
setlocal enabledelayedexpansion
REM Construye una distribucion PORTABLE de Five Doors At Freddy's:
REM runtime JDK8+JavaFX embebido + jar ejecutable + lanzador .bat, todo en
REM una sola carpeta. La maquina que EJECUTA la carpeta resultante NO
REM necesita tener Java instalado -- el runtime va incluido. Solo la
REM maquina donde se CORRE este script necesita un JDK 8 con JavaFX (ver
REM SETUP.md, mismo requisito que ya existia para desarrollo).
REM
REM Uso:  build-portable.bat  ["ruta a un JDK 8 con JavaFX"]
REM Si no se pasa el argumento, intenta detectarlo solo (JAVA8_HOME o las
REM ubicaciones tipicas de instalacion en Windows).
REM
REM Investigado y probado 2026-08-09/10 (ronda de distribucion portable):
REM jpackage (--type app-image) SI logra empaquetar el runtime JDK8+JavaFX,
REM pero el .exe nativo que genera falla en Windows con un error clasico de
REM registro ("Software\JavaSoft\Java Runtime Environment...") -- causa
REM real: jpackage esta pensado para runtimes modulares (jlink, JDK 9+), no
REM para un JDK 8 completo copiado tal cual, combinacion no soportada.
REM
REM [ACTUALIZADO 2026-08-10] launch4j (github.com/lukaszlenart/launch4j,
REM via Maven Central) SI genera un .exe nativo que funciona de verdad --
REM investigado y probado con una corrida real (ventana real del juego,
REM ciclo completo ganar->reinicio->Noche 5->Escape confirmado). Se usa en
REM modo "launching" (dontWrapJar=true, NO en modo "wrapping") a proposito:
REM el exe queda como un stub nativo de ~60KB que referencia el .jar externo
REM ya generado (nunca lo embebe) -- evita el falso positivo de antivirus
REM que launch4j mismo advierte para el modo wrapping, y produce EXACTAMENTE
REM el mismo proceso final (runtime\bin\javaw.exe -jar FiveDoorsAtFreddys.jar)
REM que ya genera el .bat de mas abajo -- launch4j reemplaza solo COMO se
REM invoca ese comando (.exe nativo en vez de .bat), nunca QUE se invoca.
REM El .bat se conserva ademas del .exe (alternativa siempre disponible si
REM launch4j no esta disponible para construir, ver mas abajo).

set SCRIPT_DIR=%~dp0
set DIST_DIR=%SCRIPT_DIR%dist\FiveDoorsAtFreddys
set JDK8=%~1

if "%JDK8%"=="" if defined JAVA8_HOME set JDK8=%JAVA8_HOME%

if "%JDK8%"=="" (
    for /d %%D in ("C:\Program Files\Java\jdk1.8*") do (
        if exist "%%D\jre\lib\jfxswt.jar" set JDK8=%%D
    )
)
if "%JDK8%"=="" (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-8*") do (
        if exist "%%D\jre\lib\jfxswt.jar" set JDK8=%%D
    )
)

if "%JDK8%"=="" (
    echo ERROR: no se encontro automaticamente un JDK 8 con JavaFX instalado.
    echo Instala uno con JavaFX incluido ^(ver SETUP.md^) o pasa la ruta como
    echo argumento:   build-portable.bat "C:\ruta\a\tu\jdk1.8"
    exit /b 1
)

if not exist "%JDK8%\bin\javac.exe" (
    echo ERROR: "%JDK8%" no parece un JDK valido ^(falta bin\javac.exe^).
    exit /b 1
)
if not exist "%JDK8%\jre\lib\jfxswt.jar" (
    echo ERROR: "%JDK8%" no incluye JavaFX ^(falta jre\lib\jfxswt.jar^).
    echo PnlWin.reproducirVideo necesita JavaFX real -- ver SETUP.md.
    exit /b 1
)

echo Usando JDK 8: %JDK8%

if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%DIST_DIR%" >nul

set TMP_DIR=%SCRIPT_DIR%build-portable-tmp
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%"
mkdir "%TMP_DIR%\classes" >nul

echo Compilando...
"%JDK8%\bin\javac.exe" -encoding UTF-8 -d "%TMP_DIR%\classes" -sourcepath "%SCRIPT_DIR%src" "%SCRIPT_DIR%src\com\fdaf\init\Main.java"
if errorlevel 1 (
    echo ERROR: fallo la compilacion.
    rmdir /s /q "%TMP_DIR%"
    exit /b 1
)

echo Copiando recursos...
xcopy /e /i /q "%SCRIPT_DIR%src\com\fdaf\resources\*" "%TMP_DIR%\classes\" >nul

echo Empaquetando jar...
pushd "%TMP_DIR%\classes"
"%JDK8%\bin\jar.exe" cfe "%DIST_DIR%\FiveDoorsAtFreddys.jar" com.fdaf.init.Main .
popd
if not exist "%DIST_DIR%\FiveDoorsAtFreddys.jar" (
    echo ERROR: no se genero el jar.
    rmdir /s /q "%TMP_DIR%"
    exit /b 1
)

echo Copiando runtime JDK8+JavaFX completo ^(varios cientos de MB, puede tardar^)...
xcopy /e /i /q "%JDK8%\*" "%DIST_DIR%\runtime\" >nul

(
echo @echo off
echo cd /d "%%~dp0"
echo start "" "%%~dp0runtime\bin\javaw.exe" -jar "%%~dp0FiveDoorsAtFreddys.jar"
) > "%DIST_DIR%\FiveDoorsAtFreddys.bat"

rmdir /s /q "%TMP_DIR%"

echo.
echo === Generando FiveDoorsAtFreddys.exe (launch4j) ===
call "%SCRIPT_DIR%build-exe-launcher.bat" "%DIST_DIR%" "%JDK8%"
if errorlevel 1 (
    echo ADVERTENCIA: no se pudo generar el .exe nativo -- FiveDoorsAtFreddys.bat
    echo sigue siendo un lanzador completamente funcional, usa ese en su lugar.
)

echo.
echo Listo. Carpeta portable generada en: %DIST_DIR%
echo No necesita Java instalado en la maquina destino -- el runtime va incluido.
if exist "%DIST_DIR%\FiveDoorsAtFreddys.exe" (
    echo El usuario final solo necesita abrir FiveDoorsAtFreddys.exe
) else (
    echo El usuario final solo necesita abrir FiveDoorsAtFreddys.bat
)
echo.
echo IMPORTANTE para que la transicion Noche 5 -^> Escape funcione: esta carpeta
echo debe quedar en la misma posicion relativa que el checkout normal, es decir
echo   ^<algun directorio^>\five_doors_at_freddys\FiveDoorsAtFreddys\   ^(esta carpeta, ya generada^)
echo   ^<algun directorio^>\five_doors_escape\                         ^(carpeta hermana^)
echo Ver SETUP.md para el detalle completo.
endlocal
