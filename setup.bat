@echo off
echo ========================================
echo    طاولتي - إعداد تلقائي للمشروع
echo ========================================
echo.
echo جاري تحميل Gradle Wrapper...

powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/nicowillis/gradle-wrapper/main/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"

IF NOT EXIST "gradle\wrapper\gradle-wrapper.jar" (
    echo تحميل بديل...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar', 'gradle\wrapper\gradle-wrapper.jar')"
)

echo.
echo ✅ تم! افتح Android Studio واختر هذا المجلد
pause
