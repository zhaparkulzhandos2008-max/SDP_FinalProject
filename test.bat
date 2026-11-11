@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
where java >nul 2>nul || (echo Java is not installed or not on PATH.& pause & exit /b 1)
if not exist out mkdir out
set "SOURCES="
for /r src %%f in (*.java) do set "SOURCES=!SOURCES! %%f"
for /r test %%f in (*.java) do set "SOURCES=!SOURCES! %%f"
echo Compiling tests...
javac -encoding UTF-8 -d out !SOURCES!
if errorlevel 1 (echo Compilation failed.& pause & exit /b 1)
echo Running automated tests...
java -cp out setup.TestRunner
pause
