# طاولتي (Tawlati) - تطبيق إدارة طاولات الكافيه

## 🚀 خطوات فتح المشروع في Android Studio

### الخطوة 1: تحميل Android Studio
https://developer.android.com/studio

### الخطوة 2: فتح المشروع
1. استخرج ZIP على سطح المكتب
2. افتح Android Studio
3. اضغط **Open**
4. اختر مجلد **Tawlati**

### الخطوة 3: انتظر Gradle Sync
Android Studio سيحمّل كل شيء تلقائياً (3-5 دقائق مع الإنترنت)

### الخطوة 4: بناء APK
```
Build → Build Bundle(s)/APK(s) → Build APK(s)
```

## 🔑 بيانات الدخول
- كلمة مرور المدير: **1234**
- طاولات افتراضية: 20 داخلية + 15 خارجية
- مدة الحجز الافتراضية: 60 دقيقة
- تنبيه قبل: 10 دقائق

## ✅ المشاكل المُصلحة
1. TableAdapter - إصلاح حساب الوقت المتبقي
2. SettingsActivity - إصلاح ترتيب setContentView و ViewModel
3. gradle-wrapper - إصلاح ملفات الإعداد

## 📦 هيكل المشروع
```
Tawlati/
├── app/src/main/java/com/tawlati/app/
│   ├── models/          ← TableModel, WaitingCustomer, AppSettings
│   ├── database/        ← Room DB, DAOs, Repository
│   ├── viewmodels/      ← MainViewModel
│   ├── adapters/        ← TableAdapter ✅, WaitingListAdapter
│   ├── ui/              ← MainActivity, BookingActivity
│   │                       SettingsActivity ✅, WaitingListActivity
│   └── utils/           ← NotificationHelper, AlarmHelper
└── app/src/main/res/
    ├── layout/          ← XML layouts
    ├── values/          ← colors, strings, styles
    └── drawable/        ← icons
```
