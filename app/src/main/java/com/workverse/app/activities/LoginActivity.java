package com.workverse.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.workverse.app.R;
import com.workverse.app.activities.admin.AdminDashboardActivity;
import com.workverse.app.activities.ceo.CEODashboardActivity;
import com.workverse.app.activities.employee.EmployeeDashboardActivity;
import com.workverse.app.activities.manager.ManagerDashboardActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText etUsername, etPassword;
    Button btnLogin;
    ProgressBar progressBar;
    TextView tvForgot;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_login);
        etUsername  = findViewById(R.id.etUsername);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvForgot    = findViewById(R.id.tvForgotPassword);
        auth = FirebaseHelper.getAuth();
        db   = FirebaseHelper.getDb();
        btnLogin.setOnClickListener(v -> doLogin());
        tvForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        db.collection(FirebaseHelper.COL_USERS)
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener(snap -> {
                if (snap.isEmpty()) {
                    setLoading(false);
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                String email = snap.getDocuments().get(0).getString("email");
                String role  = snap.getDocuments().get(0).getString("role");
                String name  = snap.getDocuments().get(0).getString("fullName");
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(res -> {
                        String uid = res.getUser().getUid();
                        SharedPrefManager.getInstance(this).saveUser(uid, username, role, name, email);
                        setLoading(false);
                        Intent i;
                        switch (role != null ? role : "") {
                            case FirebaseHelper.ROLE_ADMIN:
                                i = new Intent(this, AdminDashboardActivity.class); break;
                            case FirebaseHelper.ROLE_CEO:
                                i = new Intent(this, CEODashboardActivity.class); break;
                            case FirebaseHelper.ROLE_MANAGER:
                                i = new Intent(this, ManagerDashboardActivity.class); break;
                            default:
                                i = new Intent(this, EmployeeDashboardActivity.class);
                        }
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                setLoading(false);
                Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setLoading(boolean l) {
        progressBar.setVisibility(l ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!l);
    }
}
