@echo off
if not exist "lib" mkdir lib

echo Compiling...
javac -d bin -sourcepath src src/com/scholarship/Main.java

if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)

echo Running...
java -cp "bin;lib/*" com.scholarship.Main
pause
