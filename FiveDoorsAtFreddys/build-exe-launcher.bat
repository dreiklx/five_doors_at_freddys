@echo off
setlocal enabledelayedexpansion
REM Genera FiveDoorsAtFreddys.exe (launcher nativo real) a partir del jar +
REM runtime ya generados por build-portable.bat, usando launch4j
REM (net.sf.launch4j, Maven Central) en modo "launching" (dontWrapJar) --
REM el .exe es un stub nativo pequeno (~60KB) que referencia
REM FiveDoorsAtFreddys.jar externo (nunca lo embebe), y termina lanzando
REM exactamente el mismo "runtime\bin\javaw.exe -jar FiveDoorsAtFreddys.jar"
REM que ya usa FiveDoorsAtFreddys.bat -- ver CLAUDE.md #1.23 para la
REM investigacion completa (por que jpackage NO sirve para esto, por que
REM launch4j si, y la verificacion real de todo el flujo a traves del .exe).
REM
REM Uso: build-exe-launcher.bat "<carpeta destino con jar+runtime>" "<JDK8>"
REM Llamado automaticamente por build-portable.bat -- no hace falta correrlo
REM a mano. Si algo falla aqui, build-portable.bat sigue dejando
REM FiveDoorsAtFreddys.bat como lanzador alternativo completamente funcional
REM -- este script nunca debe hacer que la distribucion entera falle.

set DIST_DIR=%~1
set JDK8=%~2
set SCRIPT_DIR=%~dp0
set L4J_CACHE=%USERPROFILE%\.launch4j-cache\3.50
set L4J_BASE_URL=https://repo1.maven.org/maven2/net/sf/launch4j/launch4j/3.50
set L4J_CORE_SHA1=9448dd71640657ca17b9bd20a447f5e3032bbad2
set L4J_WORKDIR_SHA1=2b176aff1bce73b8495688eb9bcb156ab7d9624c

if "%DIST_DIR%"=="" (
    echo ERROR: falta la carpeta destino.
    exit /b 1
)
if not exist "%DIST_DIR%\FiveDoorsAtFreddys.jar" (
    echo ERROR: no existe "%DIST_DIR%\FiveDoorsAtFreddys.jar" -- corre build-portable.bat primero.
    exit /b 1
)

if not exist "%L4J_CACHE%\ready.flag" (
    echo Descargando launch4j 3.50 de Maven Central ^(una sola vez, se cachea en %L4J_CACHE%^)...
    if exist "%L4J_CACHE%" rmdir /s /q "%L4J_CACHE%"
    mkdir "%L4J_CACHE%" >nul 2>nul

    powershell -NoProfile -Command "$ErrorActionPreference='Stop'; try { Invoke-WebRequest -Uri '%L4J_BASE_URL%/launch4j-3.50.jar' -OutFile '%L4J_CACHE%\launch4j-3.50.jar' -UseBasicParsing; Invoke-WebRequest -Uri '%L4J_BASE_URL%/launch4j-3.50-workdir-win32.jar' -OutFile '%L4J_CACHE%\launch4j-3.50-workdir-win32.jar' -UseBasicParsing; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/thoughtworks/xstream/xstream/1.4.15/xstream-1.4.15.jar' -OutFile '%L4J_CACHE%\xstream-1.4.15.jar' -UseBasicParsing; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar' -OutFile '%L4J_CACHE%\xmlpull-1.1.3.1.jar' -UseBasicParsing; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar' -OutFile '%L4J_CACHE%\xpp3_min-1.1.4c.jar' -UseBasicParsing } catch { Write-Output ('DESCARGA_FALLO: ' + $_.Exception.Message); exit 1 }"
    if errorlevel 1 (
        echo ADVERTENCIA: no se pudo descargar launch4j ^(sin internet?^) -- se omite el .exe.
        exit /b 1
    )

    echo Verificando integridad ^(SHA1 contra Maven Central^)...
    powershell -NoProfile -Command "$ErrorActionPreference='Stop'; $h1=(Get-FileHash '%L4J_CACHE%\launch4j-3.50.jar' -Algorithm SHA1).Hash.ToLower(); $h2=(Get-FileHash '%L4J_CACHE%\launch4j-3.50-workdir-win32.jar' -Algorithm SHA1).Hash.ToLower(); if ($h1 -ne '%L4J_CORE_SHA1%') { Write-Output 'CHECKSUM_INVALIDO_CORE'; exit 1 }; if ($h2 -ne '%L4J_WORKDIR_SHA1%') { Write-Output 'CHECKSUM_INVALIDO_WORKDIR'; exit 1 }"
    if errorlevel 1 (
        echo ADVERTENCIA: checksum de launch4j invalido -- se borra el cache y se omite el .exe por seguridad.
        rmdir /s /q "%L4J_CACHE%"
        exit /b 1
    )

    echo Extrayendo recursos nativos de launch4j...
    pushd "%L4J_CACHE%"
    "%JDK8%\bin\jar.exe" xf launch4j-3.50-workdir-win32.jar
    move "launch4j-3.50-workdir-win32" "launch4j" >nul
    copy /y "launch4j-3.50.jar" "launch4j\launch4j.jar" >nul
    popd

    REM Marca de cache lista -- si algo de lo anterior fallo, este archivo nunca se crea,
    REM asi que la proxima corrida vuelve a intentar desde cero en vez de usar un cache a medias.
    echo ok > "%L4J_CACHE%\ready.flag"
)

set L4J_CP=%L4J_CACHE%\launch4j\launch4j.jar;%L4J_CACHE%\xstream-1.4.15.jar;%L4J_CACHE%\xmlpull-1.1.3.1.jar;%L4J_CACHE%\xpp3_min-1.1.4c.jar

set CONFIG_XML=%TEMP%\fdaf-launch4j-config-%RANDOM%.xml
(
echo ^<launch4jConfig^>
echo   ^<headerType^>gui^</headerType^>
echo   ^<dontWrapJar^>true^</dontWrapJar^>
echo   ^<outfile^>%DIST_DIR%\FiveDoorsAtFreddys.exe^</outfile^>
echo   ^<jar^>FiveDoorsAtFreddys.jar^</jar^>
echo   ^<errTitle^>Five Doors At Freddy's^</errTitle^>
echo   ^<chdir^>.^</chdir^>
echo   ^<priority^>normal^</priority^>
echo   ^<stayAlive^>false^</stayAlive^>
echo   ^<restartOnCrash^>false^</restartOnCrash^>
echo   ^<manifest^>^</manifest^>
echo   ^<icon^>^</icon^>
echo   ^<jre^>
echo     ^<path^>runtime^</path^>
echo     ^<bundledJre64Bit^>true^</bundledJre64Bit^>
echo     ^<bundledJreAsFallback^>false^</bundledJreAsFallback^>
echo     ^<minVersion^>1.8.0^</minVersion^>
echo     ^<maxVersion^>1.8.0_999^</maxVersion^>
echo     ^<jdkPreference^>preferJre^</jdkPreference^>
echo     ^<runtimeBits^>64/32^</runtimeBits^>
echo   ^</jre^>
echo ^</launch4jConfig^>
) > "%CONFIG_XML%"

"%JDK8%\bin\java.exe" -cp "%L4J_CP%" net.sf.launch4j.Main "%CONFIG_XML%"
set L4J_RESULT=%ERRORLEVEL%
del /q "%CONFIG_XML%" 2>nul

if not "%L4J_RESULT%"=="0" (
    echo ADVERTENCIA: launch4j fallo al generar el .exe.
    exit /b 1
)
if not exist "%DIST_DIR%\FiveDoorsAtFreddys.exe" (
    echo ADVERTENCIA: launch4j no reporto error pero el .exe no aparecio.
    exit /b 1
)

echo FiveDoorsAtFreddys.exe generado correctamente.
endlocal
