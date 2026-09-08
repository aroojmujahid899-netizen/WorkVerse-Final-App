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

    private void setupRealTimeValidation() {

        etFullName.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                if (TextUtils.isEmpty(name)) {
                    tilFullName.setError("Full name is required");
                } else if (!name.matches("^[a-zA-Z ]+$")) {
                    tilFullName.setError("Name should contain only letters");
                } else {
                    tilFullName.setError(null);
                }
            }
        });

        etUsername.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                String username = s.toString().trim();
                if (TextUtils.isEmpty(username)) {
                    tilUsername.setError("Username is required");
                } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
                    tilUsername.setError("Only letters, numbers and underscore allowed");
                } else {
                    tilUsername.setError(null);
                }
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                String email = s.toString().trim();
                if (TextUtils.isEmpty(email)) {
                    tilEmail.setError("Email is required");
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.setError("Enter a valid email address");
                } else {
                    tilEmail.setError(null);
                }
            }
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                String phone = s.toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    tilPhone.setError("Phone number is required");
                } else if (!phone.matches("^[0-9]{10,11}$")) {
                    tilPhone.setError("Enter a valid phone number (digits only)");
                } else {
                    tilPhone.setError(null);
                }
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                String pass = s.toString().trim();
                if (TextUtils.isEmpty(pass)) {
                    tilPassword.setError("Password is required");
                } else if (pass.length() < 6) {
                    tilPassword.setError("Password must be at least 6 characters");
                } else {
                    tilPassword.setError(null);
                }
            }
        });

        actDesignation.setOnItemClickListener((parent, view, position, id) -> tilDesignation.setError(null));
        actCampaign.setOnItemClickListener((parent, view, position, id) -> tilCampaign.setError(null));
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String desig = actDesignation.getText().toString().trim();
        String campaign = actCampaign.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            tilFullName.setError("Full name is required"); isValid = false;
        } else if (!name.matches("^[a-zA-Z ]+$")) {
            tilFullName.setError("Name should contain only letters"); isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError("Username is required"); isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            tilUsername.setError("Only letters, numbers and underscore allowed"); isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required"); isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address"); isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Phone number is required"); isValid = false;
        } else if (!phone.matches("^[0-9]{10,11}$")) {
            tilPhone.setError("Enter a valid phone number (digits only)"); isValid = false;
        }

        if (TextUtils.isEmpty(desig)) {
            tilDesignation.setError("Select a designation"); isValid = false;
        }

        if (TextUtils.isEmpty(campaign)) {
            tilCampaign.setError("Select a campaign"); isValid = false;
        }

        if (TextUtils.isEmpty(pass)) {
            tilPassword.setError("Password is required"); isValid = false;
        } else if (pass.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters"); isValid = false;
        }

        return isValid;
    }

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