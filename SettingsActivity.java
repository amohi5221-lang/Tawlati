package com.tawlati.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.tawlati.app.R;
import com.tawlati.app.models.AppSettings;
import com.tawlati.app.viewmodels.MainViewModel;

public class SettingsActivity extends AppCompatActivity {

    private MainViewModel viewModel;
    private AppSettings currentSettings;

    private EditText etCafeName, etIndoorCount, etOutdoorCount,
                     etDefaultDuration, etAlertBefore, etNewPassword;
    private Switch switchDarkMode;
    private Button btnSave, btnResetTables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        findViewById(R.id.settingsContent).setVisibility(android.view.View.GONE);
        showPasswordDialog();
    }

    private void showPasswordDialog() {
        EditText etPassword = new EditText(this);
        etPassword.setHint("كلمة مرور المدير");
        etPassword.setGravity(android.view.Gravity.CENTER);
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this)
            .setTitle("🔒 الدخول للإعدادات")
            .setMessage("يرجى إدخال كلمة مرور المدير")
            .setView(etPassword)
            .setPositiveButton("دخول", (dialog, which) ->
                verifyPassword(etPassword.getText().toString()))
            .setNegativeButton("إلغاء", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    private void verifyPassword(String input) {
        new Thread(() -> {
            currentSettings = viewModel.getSettingsSync();
            String correct = (currentSettings != null) ?
                currentSettings.adminPassword : "1234";
            runOnUiThread(() -> {
                if (correct.equals(input)) {
                    findViewById(R.id.settingsContent)
                        .setVisibility(android.view.View.VISIBLE);
                    initViews();
                    populateSettings();
                } else {
                    Toast.makeText(this, "❌ كلمة المرور غير صحيحة",
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    private void initViews() {
        etCafeName        = findViewById(R.id.etCafeName);
        etIndoorCount     = findViewById(R.id.etIndoorCount);
        etOutdoorCount    = findViewById(R.id.etOutdoorCount);
        etDefaultDuration = findViewById(R.id.etDefaultDuration);
        etAlertBefore     = findViewById(R.id.etAlertBefore);
        etNewPassword     = findViewById(R.id.etNewPassword);
        switchDarkMode    = findViewById(R.id.switchDarkMode);
        btnSave           = findViewById(R.id.btnSaveSettings);
        btnResetTables    = findViewById(R.id.btnResetTables);

        btnSave.setOnClickListener(v -> saveSettings());
        btnResetTables.setOnClickListener(v -> confirmResetTables());
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void populateSettings() {
        if (currentSettings == null) return;
        etCafeName.setText(currentSettings.cafeName);
        etIndoorCount.setText(String.valueOf(currentSettings.indoorTablesCount));
        etOutdoorCount.setText(String.valueOf(currentSettings.outdoorTablesCount));
        etDefaultDuration.setText(String.valueOf(currentSettings.defaultBookingDurationMin));
        etAlertBefore.setText(String.valueOf(currentSettings.alertBeforeMinutes));
        switchDarkMode.setChecked(currentSettings.darkMode);
    }

    // ✅ دالة مساعدة آمنة لتحويل النص لرقم
    private int safeParseInt(String value, int defaultVal) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private void saveSettings() {
        if (currentSettings == null) currentSettings = new AppSettings();

        String cafeName   = etCafeName.getText().toString().trim();
        String indoorStr  = etIndoorCount.getText().toString().trim();
        String outdoorStr = etOutdoorCount.getText().toString().trim();
        String durationStr= etDefaultDuration.getText().toString().trim();
        String alertStr   = etAlertBefore.getText().toString().trim();
        String newPass    = etNewPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(cafeName))    currentSettings.cafeName = cafeName;

        // ✅ استخدام safeParseInt بدلاً من parseInt المباشر
        if (!TextUtils.isEmpty(indoorStr))
            currentSettings.indoorTablesCount = safeParseInt(indoorStr, 20);
        if (!TextUtils.isEmpty(outdoorStr))
            currentSettings.outdoorTablesCount = safeParseInt(outdoorStr, 15);
        if (!TextUtils.isEmpty(durationStr))
            currentSettings.defaultBookingDurationMin = safeParseInt(durationStr, 60);
        if (!TextUtils.isEmpty(alertStr))
            currentSettings.alertBeforeMinutes = safeParseInt(alertStr, 10);
        if (!TextUtils.isEmpty(newPass))
            currentSettings.adminPassword = newPass;
        currentSettings.darkMode = switchDarkMode.isChecked();

        viewModel.updateSettings(currentSettings);
        Toast.makeText(this, "✅ تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show();
    }

    private void confirmResetTables() {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ تحديث الطاولات")
            .setMessage("سيتم مسح جميع الحجوزات وإعادة توليد الطاولات. هل أنت متأكد؟")
            .setPositiveButton("نعم، تحديث", (dialog, which) -> {
                saveSettings();
                int indoor  = safeParseInt(
                    etIndoorCount.getText().toString().trim(), 20);
                int outdoor = safeParseInt(
                    etOutdoorCount.getText().toString().trim(), 15);
                viewModel.regenerateTables(indoor, outdoor);
                Toast.makeText(this, "✅ تم تحديث الطاولات", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("إلغاء", null)
            .show();
    }
}
