#!/bin/bash
echo "========================================="
echo "   طاولتي - إعداد تلقائي للمشروع"
echo "========================================="
echo ""
echo "جاري تحميل Gradle Wrapper..."

curl -L -o gradle/wrapper/gradle-wrapper.jar \
  "https://raw.githubusercontent.com/nicowillis/gradle-wrapper/main/gradle-wrapper.jar" \
  2>/dev/null

if [ ! -s "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "تحميل بديل..."
    wget -q -O gradle/wrapper/gradle-wrapper.jar \
      "https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
fi

echo ""
echo "✅ تم! افتح Android Studio واختر هذا المجلد"
