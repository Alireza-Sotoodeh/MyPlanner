@echo off
setlocal

echo ============================================
echo   MyPlanner - Build Release APK
echo ============================================
echo.

set KEYSTORE_PATH=%~dp0my-release-key.jks

if not exist "%KEYSTORE_PATH%" (
    echo ERROR: Keystore not found at %KEYSTORE_PATH%
    echo Generate one with:
    echo   keytool -genkey -v -keystore my-release-key.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
    pause
    exit /b 1
)

set /p STORE_PASSWORD="Keystore password: "
set /p KEY_PASSWORD="Key password: "

echo.
echo Building release APK...
echo.

set KEYSTORE_PATH=%KEYSTORE_PATH%
call gradlew.bat assembleRelease

if %ERRORLEVEL% neq 0 (
    echo.
    echo BUILD FAILED
    pause
    exit /b 1
)

echo.
echo ============================================
echo   BUILD SUCCESSFUL
echo   APK: app\build\outputs\apk\release\app-release.apk
echo ============================================
echo.
pause
