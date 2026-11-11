@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
where java >nul 2>nul || (echo Java is not installed or not on PATH. Install JDK 17+ and retry.& pause & exit /b 1)
if not exist out mkdir out
set "SOURCES="
for /r src %%f in (*.java) do set "SOURCES=!SOURCES! %%f"
echo Compiling Build Your Setup...
javac -encoding UTF-8 -d out !SOURCES!
if errorlevel 1 (echo Compilation failed.& pause & exit /b 1)
echo.
echo Starting server at http://localhost:8080
echo Press Ctrl+C to stop.
java -cp out setup.web.WebServer 8080
pause
