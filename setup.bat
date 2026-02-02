@echo off
setlocal

:: Set title
title SEF Scholarship System Setup

echo ========================================
echo    SEF Scholarship System Setup
echo ========================================

:: Check for Java
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Java not found. Please install JDK and add it to your PATH.
    pause
    exit /b 1
)

:: Create bin directory if it doesn't exist
if not exist bin mkdir bin

:: Compile DatabaseSetup
echo [1/2] Compiling DatabaseSetup...
javac -d bin -cp "lib/*;src" src/utils/DatabaseSetup.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b %errorlevel%
)
echo [SUCCESS] Compilation complete.

:: Run DatabaseSetup
echo [2/2] Running DatabaseSetup...
java -cp "bin;lib/*" utils.DatabaseSetup
if %errorlevel% neq 0 (
    echo [ERROR] Execution failed.
    pause
    exit /b %errorlevel%
)

echo.
echo Setup process finished.
pause
endlocal
