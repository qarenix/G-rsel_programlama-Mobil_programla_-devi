@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "GRADLE=%~dp0gradlew.bat"
if not exist "%GRADLE%" (
  echo gradlew.bat bulunamadi. Projeyi Android Studio ile acip Sync calistirin.
  pause
  exit /b 1
)
"%GRADLE%" assembleDebug
pause
