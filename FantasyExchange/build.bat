@echo off
cd /d "%~dp0"
if not exist build\app mkdir build\app
javac -Xlint:all -cp acm.jar -d build\app *.java
if errorlevel 1 exit /b 1
(echo Manifest-Version: 1.0& echo Main-Class: FantasyExchangeGUI& echo Class-Path: acm.jar& echo.) > build\MANIFEST.MF
jar cfm FantasyExchange.jar build\MANIFEST.MF -C build\app .
