package com.workverse.app.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.workverse.app.R;
import com.workverse.app.utils.FirebaseHelper;

import java.util.HashMap;
import java.util.Map;

public class AddEmployeeActivity extends AppCompatActivity {

    TextInputLayout tilFullName, tilUsername, tilEmail, tilPhone, tilDesignation, tilCampaign, tilPassword;
    TextInputEditText etFullName, etUsername, etEmail, etPhone, etPassword;
    AutoCompleteTextView actDesignation, actCampaign;
    Button btnSubmit;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_add_employee);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        btnSubmit = findViewById(R.id.btnSubmit);
        pb = findViewById(R.id.progressBar);

        tilFullName = findViewById(R.id.tilFullName);
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilDesignation = findViewById(R.id.tilDesignation);
        tilCampaign = findViewById(R.id.tilCampaign);
        tilPassword = findViewById(R.id.tilPassword);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        actDesignation = findViewById(R.id.actDesignation);
        actCampaign = findViewById(R.id.actCampaign);

        String[] designationList = {"Fronters", "Verifiers", "Closers"};
        actDesignation.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, designationList));

        String[] campaignList = {"MEDICARE", "FE", "Home Warranty"};
        actCampaign.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, campaignList));

        setupRealTimeValidation();

        btnSubmit.setOnClickListener(v -> {
            if (validateInputs()) {
                addEmployee();
            }
        });
    }

    // ---------- Live validation ----------
    private interface Checker { String check(String v); }

    private void watch(TextInputEditText et, TextInputLayout til, Checker c) {
        et.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int d) {}
            public void onTextChanged(CharSequence s, int a, int b, int d) {}
            public void afterTextChanged(Editable s) {
                til.setError(c.check(s.toString().trim()));
            }
        });
    }

    private void setupRealTimeValidation() {
        watch(etFullName, tilFullName, this::checkName);
        watch(etUsername, tilUsername, this::checkUsername);
        watch(etEmail, tilEmail, this::checkEmail);
        watch(etPhone, tilPhone, this::checkPhone);
        watch(etPassword, tilPassword, this::checkPassword);

        actDesignation.setOnItemClickListener((p, v, pos, id) -> tilDesignation.setError(null));
        actCampaign.setOnItemClickListener((p, v, pos, id) -> tilCampaign.setError(null));
    }

    // ---------- Shared checks (identical in Add Manager) ----------
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

    private boolean validateInputs() {
        boolean ok = true;
        ok &= show(tilFullName, checkName(etFullName.getText().toString().trim()));
        ok &= show(tilUsername, checkUsername(etUsername.getText().toString().trim()));
        ok &= show(tilEmail, checkEmail(etEmail.getText().toString().trim()));
        ok &= show(tilPhone, checkPhone(etPhone.getText().toString().trim()));
        ok &= show(tilDesignation, TextUtils.isEmpty(actDesignation.getText().toString().trim())
                ? "Please select Designation" : null);
        ok &= show(tilCampaign, TextUtils.isEmpty(actCampaign.getText().toString().trim())
                ? "Please select Campaign" : null);
        ok &= show(tilPassword, checkPassword(etPassword.getText().toString().trim()));
        return ok;
    }

    // ---------- Save ----------
    private void addEmployee() {
        String name = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String desig = actDesignation.getText().toString().trim();
        String campaign = actCampaign.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        pb.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(res -> {
                    String uid = res.getUser().getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", uid);
                    userMap.put("username", username);
                    userMap.put("email", email);
                    userMap.put("fullName", name);
                    userMap.put("phone", phone);
                    userMap.put("role", "Employee");
                    userMap.put("designation", desig);
                    userMap.put("campaign", campaign);
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).set(userMap);

                    Map<String, Object> emp = new HashMap<>();
                    emp.put("name", name);
                    emp.put("email", email);
                    emp.put("phone", phone);
                    emp.put("designation", desig);
                    emp.put("campaign", campaign);
                    emp.put("userId", uid);
                    emp.put("status", "active");
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).document(uid).set(emp)
                            .addOnSuccessListener(r -> {
                                pb.setVisibility(View.GONE);
                                Toast.makeText(this, "Employee added!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                pb.setVisibility(View.GONE);
                                btnSubmit.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Auth error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}