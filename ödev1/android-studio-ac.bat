@echo off
setlocal
set "STUDIO=C:\Program Files\Android\Android Studio\bin\studio64.exe"
if not exist "%STUDIO%" (
  echo Android Studio bulunamadi: %STUDIO%
  pause
  exit /b 1
)
start "" "%STUDIO%" "%~dp0"
