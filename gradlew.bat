@echo off
setlocal
set "GRADLE_VERSION=8.13"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "BOOTSTRAP_DIR=%GRADLE_USER_HOME%\wim100-bootstrap"
set "GRADLE_DIR=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%"
set "ZIP_FILE=%BOOTSTRAP_DIR%\gradle-%GRADLE_VERSION%-bin.zip"
if not exist "%GRADLE_DIR%\bin\gradle.bat" (
  if not exist "%BOOTSTRAP_DIR%" mkdir "%BOOTSTRAP_DIR%"
  if not exist "%ZIP_FILE%" (
    echo Descargando Gradle %GRADLE_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%ZIP_FILE%'"
    if errorlevel 1 exit /b 1
  )
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP_FILE%' -DestinationPath '%BOOTSTRAP_DIR%' -Force"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_DIR%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
