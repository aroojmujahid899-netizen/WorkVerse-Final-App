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

        setupRealtimeValidation();

        btnSave.setOnClickListener(v -> validateAndSaveManager());
    }

    // ---------- Live validation ----------
    private interface Checker { String check(String v); }

    private void watch(TextInputEditText et, TextInputLayout til, Checker c) {
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void afterTextChanged(Editable s) {
                til.setError(c.check(s.toString().trim()));
            }
        });
    }

    private void setupRealtimeValidation() {
        watch(etFullName, tilFullName, this::checkName);
        watch(etUsername, tilUsername, this::checkUsername);
        watch(etEmail, tilEmail, this::checkEmail);
        watch(etPhone, tilPhone, this::checkPhone);
        watch(etPassword, tilPassword, this::checkPassword);

        spinnerDesignation.setOnItemClickListener((p, v, pos, id) -> tilDesignation.setError(null));
        spinnerCampaign.setOnItemClickListener((p, v, pos, id) -> tilCampaign.setError(null));
    }

    // ---------- Shared checks (identical in Add Employee) ----------
    private String checkName(String v) {
        if (TextUtils.isEmpty(v)) return "Full name is required";
        if (!v.matches("^[a-zA-Z ]+$")) return "Name should contain only letters";
        return null;
    }

    private String checkUsername(String v) {
        if (TextUtils.isEmpty(v)) return "Username is required";
        if (!v.matches("^[a-zA-Z0-9_]+$")) return "Only letters, numbers and underscore allowed";
        if (v.length() < 3) return "Username must be at least 3 characters";
        return null;
    }

    private String checkEmail(String v) {
        if (TextUtils.isEmpty(v)) return "Email is required";
        if (!Patterns.EMAIL_ADDRESS.matcher(v).matches()) return "Enter a valid email address";
        return null;
    }

    private String checkPhone(String v) {
        if (TextUtils.isEmpty(v)) return "Phone number is required";
        if (!v.matches("^[0-9]+$")) return "Enter a valid phone number (digits only)";
        if (!v.matches("^03\\d{9}$")) return "Phone number must be 11 digits and start with 03";
        return null;
    }

    private String checkPassword(String v) {
        if (TextUtils.isEmpty(v)) return "Password is required";
        if (v.length() < 6) return "Password must be at least 6 characters";
        return null;
    }

    private boolean show(TextInputLayout til, String err) {
        til.setError(err);
        return err == null;
    }

    // ---------- Validate + Save ----------
    private void validateAndSaveManager() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String designation = spinnerDesignation.getText().toString().trim();
        String campaign = spinnerCampaign.getText().toString().trim();
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean ok = true;
        ok &= show(tilFullName, checkName(fullName));
        ok &= show(tilUsername, checkUsername(username));
        ok &= show(tilEmail, checkEmail(email));
        ok &= show(tilPhone, checkPhone(phone));
        ok &= show(tilDesignation, TextUtils.isEmpty(designation) ? "Please select Designation" : null);
        ok &= show(tilCampaign, TextUtils.isEmpty(campaign) ? "Please select Campaign" : null);
        ok &= show(tilPassword, checkPassword(password));
        if (!ok) return;

        btnSave.setEnabled(false);

        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser().getUid();

                    // users collection: login yahin se username -> email dhoondta hai
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", uid);
                    userMap.put("username", username);
                    userMap.put("email", email);
                    userMap.put("fullName", fullName);
                    userMap.put("phone", phone);
                    userMap.put("role", FirebaseHelper.ROLE_MANAGER);
                    userMap.put("designation", designation);
                    userMap.put("campaign", campaign);
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).set(userMap);

                    // managers collection: list/cards ke liye
                    Map<String, Object> managerMap = new HashMap<>();
                    managerMap.put("fullName", fullName);
                    managerMap.put("name", fullName);
                    managerMap.put("username", username);
                    managerMap.put("email", email);
                    managerMap.put("phone", phone);
                    managerMap.put("designation", designation);
                    managerMap.put("campaign", campaign);
                    managerMap.put("role", FirebaseHelper.ROLE_MANAGER);
                    managerMap.put("userId", uid);
                    managerMap.put("status", "active");

                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_MANAGERS).document(uid)
                            .set(managerMap)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(AddManagerActivity.this, "Manager added successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnSave.setEnabled(true);
                                Toast.makeText(AddManagerActivity.this, "Failed to add manager: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(AddManagerActivity.this, "Auth error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}