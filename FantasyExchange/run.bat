@echo off
cd /d "%~dp0"
if not exist build mkdir build
javac -cp acm.jar -d build *.java
if errorlevel 1 exit /b 1
java -cp "build;acm.jar" FantasyExchangeGUI
