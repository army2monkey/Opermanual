@echo off
setlocal
set VER=8.13
if "%GRADLE_USER_HOME%"=="" (
  set BASE=%USERPROFILE%\.gradle\jims-field-playbook
) else (
  set BASE=%GRADLE_USER_HOME%\jims-field-playbook
)
set HOME_DIR=%BASE%\gradle-%VER%
set ZIP=%BASE%\gradle-%VER%-bin.zip
if not exist "%HOME_DIR%\bin\gradle.bat" (
  if not exist "%BASE%" mkdir "%BASE%"
  if not exist "%ZIP%" (
    echo Downloading Gradle %VER%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-%VER%-bin.zip' -OutFile '%ZIP%'"
    if errorlevel 1 exit /b 1
  )
  echo Extracting Gradle %VER%...
  if exist "%HOME_DIR%" rmdir /s /q "%HOME_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%ZIP%' -DestinationPath '%BASE%' -Force"
  if errorlevel 1 exit /b 1
)
call "%HOME_DIR%\bin\gradle.bat" %*
endlocal
