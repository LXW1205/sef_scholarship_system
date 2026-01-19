@echo off
echo Finding processes using port 8080...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080') do (
    echo Killing PID %%a
    taskkill /PID %%a /F >nul 2>&1
)

echo Done.
pause
