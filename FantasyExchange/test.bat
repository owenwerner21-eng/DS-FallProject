@echo off
cd /d "%~dp0"
if not exist build mkdir build
javac -Xlint:all -cp acm.jar -d build *.java tests\ExchangeTest.java
if errorlevel 1 exit /b 1
java -cp "build;acm.jar" ExchangeTest
