@echo off
title Student Event-Management System
echo ==================================================================
echo   Compiling and Launching Student Event-Management System...
echo ==================================================================
cd /d "%~dp0"

if not exist mysql-connector-j-8.3.0.jar (
    echo Downloading MySQL Connector/J JDBC Driver...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar' -OutFile 'mysql-connector-j-8.3.0.jar'"
)

echo Compiling Java source files...
javac -cp ".;mysql-connector-j-8.3.0.jar;*" java/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please ensure JDK javac is installed.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Compilation Successful! Starting Java Web Server and Console CLI...
echo ==================================================================
java -cp ".;java;mysql-connector-j-8.3.0.jar;*" Main

pause
