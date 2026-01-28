@echo off
if not exist "lib" mkdir lib

echo Compiling...
dir /s /b src\*.java > sources.txt
javac -d bin -cp "lib/*" @sources.txt
del sources.txt

if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)

echo Running Tests...
java -jar lib/junit-platform-console-standalone-1.10.1.jar -cp "bin" --scan-class-path
pause
