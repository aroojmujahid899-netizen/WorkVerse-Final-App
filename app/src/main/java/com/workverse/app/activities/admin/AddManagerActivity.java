package com.workverse.app.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.workverse.app.R;
import com.workverse.app.utils.FirebaseHelper;
import java.util.HashMap;
import java.util.Map;

public class AddManagerActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilUsername, tilEmail, tilPhone, tilDesignation, tilCampaign, tilPassword;
    private TextInputEditText etFullName, etUsername, etEmail, etPhone, etPassword;
    private AutoCompleteTextView spinnerDesignation, spinnerCampaign;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_manager);

        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
            tb.setNavigationOnClickListener(v -> finish());
        }

        // Layouts
        tilFullName = findViewById(R.id.tilFullName);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilDesignation = findViewById(R.id.tilDesignation);
        tilCampaign = findViewById(R.id.tilCampaign);
        tilPassword = findViewById(R.id.tilPassword);

        // Inputs
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        spinnerDesignation = findViewById(R.id.spinnerDesignation);
        spinnerCampaign = findViewById(R.id.spinnerCampaign);
        btnSave = findViewById(R.id.btnSave);

        // Dropdowns Setup
        String[] designations = new String[]{"Fronters", "Verifiers", "Closers"};
        spinnerDesignation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, designations));

        String[] campaigns = new String[]{"MEDICARE", "FE", "Home Warranty"};
        spinnerCampaign.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campaigns));

        // Real-Time Validation Watchers
        setupRealtimeValidation();

        btnSave.setOnClickListener(v -> validateAndSaveManager());
    }

    private void setupRealtimeValidation() {
        // Full Name Live Watcher
        etFullName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString().trim();
                if (!val.isEmpty() && !val.matches("^[a-zA-Z\\s]+$")) {
                    tilFullName.setError("Full Name can only contain letters (no numbers)");
                } else {
                    tilFullName.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Username Live Watcher
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString().trim();
                if (val.contains(" ")) {
                    tilUsername.setError("Username cannot contain spaces");
                } else {
                    tilUsername.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Email Live Watcher
        etEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString().trim();
                if (!val.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(val).matches()) {
                    tilEmail.setError("Enter a valid email address");
                } else {
                    tilEmail.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Phone Live Watcher
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String val = s.toString().trim();
                if (!val.isEmpty() && !val.matches("^03\\d{9}$")) {
                    tilPhone.setError("Enter valid 11-digit number starting with 03");
                } else {
                    tilPhone.setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void validateAndSaveManager() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String designation = spinnerDesignation.getText().toString().trim();
        String campaign = spinnerCampaign.getText().toString().trim();
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean isValid = true;

        // 1. Full Name
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Full Name is required");
            isValid = false;
        } else if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            tilFullName.setError("Full Name can only contain letters");
            isValid = false;
        } else {
            tilFullName.setError(null);
        }

        // 2. Username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required");
            isValid = false;
        } else if (username.contains(" ")) {
            tilUsername.setError("Username cannot contain spaces");
            isValid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        // 3. Email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // 4. Phone
        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Phone number is required");
            isValid = false;
        } else if (!phone.matches("^03\\d{9}$")) {
            tilPhone.setError("Enter valid 11-digit number (e.g. 03001234567)");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        // 5. Designation
        if (TextUtils.isEmpty(designation)) {
            tilDesignation.setError("Please select Designation");
            isValid = false;
        } else {
            tilDesignation.setError(null);
        }

        // 6. Campaign
        if (TextUtils.isEmpty(campaign)) {
            tilCampaign.setError("Please select Campaign");
            isValid = false;
        } else {
            tilCampaign.setError(null);
        }

        // 7. Password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!isValid) return;

        btnSave.setEnabled(false);

        Map<String, Object> managerMap = new HashMap<>();
        managerMap.put("fullName", fullName);
        managerMap.put("name", fullName);
        managerMap.put("username", username);
        managerMap.put("email", email);
        managerMap.put("phone", phone);
        managerMap.put("designation", designation);
        managerMap.put("campaign", campaign);
        managerMap.put("role", "Manager");
        managerMap.put("status", "active");

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_MANAGERS)
                .add(managerMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddManagerActivity.this, "Manager added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(AddManagerActivity.this, "Failed to add manager: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}