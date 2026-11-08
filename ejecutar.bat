@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Version de Java instalada:
java -version
echo.
if exist out rmdir /s /q out
javac -encoding UTF-8 -cp lib\postgresql-42.7.4.jar -d out -sourcepath src src\app\Main.java src\app\DemoConsola.java
if errorlevel 1 (
    echo.
    echo Hubo errores de compilacion. Copia el primer error y pasamelo.
    pause
    exit /b 1
)
java -Dfile.encoding=UTF-8 -cp "out;lib\postgresql-42.7.4.jar" app.Main
if errorlevel 1 pause
