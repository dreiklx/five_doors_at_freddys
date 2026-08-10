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
REM para un JDK 8 completo copiado tal cual, combinacion no soportada. El
REM MISMO runtime, invocado directamente via runtime\bin\javaw.exe -jar
REM (lo que hace este script), SI funciona -- verificado con una corrida
REM real, ventana real del juego. Por eso este script usa un .bat lanzador
REM en vez de intentar generar un .exe nativo.

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
echo Listo. Carpeta portable generada en: %DIST_DIR%
echo No necesita Java instalado en la maquina destino -- el runtime va incluido.
echo.
echo IMPORTANTE para que la transicion Noche 5 -^> Escape funcione: esta carpeta
echo debe quedar en la misma posicion relativa que el checkout normal, es decir
echo   ^<algun directorio^>\five_doors_at_freddys\FiveDoorsAtFreddys\   ^(esta carpeta, ya generada^)
echo   ^<algun directorio^>\five_doors_escape\                         ^(carpeta hermana^)
echo Ver SETUP.md para el detalle completo.
endlocal
