package com.workverse.app.activities.ceo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.workverse.app.R;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.HashMap;
import java.util.Map;

public class CEOProfileActivity extends AppCompatActivity {

    TextView tvEmail, tvRole;
    TextInputEditText etName, etPhone;
    Button btnSave;
    ProgressBar pb;
    SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_profile);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("My Profile");
        tb.setNavigationOnClickListener(v -> finish());

        spm     = SharedPrefManager.getInstance(this);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole  = findViewById(R.id.tvRole);
        etName  = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        btnSave = findViewById(R.id.btnSave);
        pb      = findViewById(R.id.progressBar);

        if (etName != null)
            etName.setText(spm.getFullName() != null
                    ? spm.getFullName() : "");
        if (tvEmail != null)
            tvEmail.setText(spm.getEmail() != null
                    ? spm.getEmail() : "—");
        if (tvRole != null)
            tvRole.setText("Chief Executive Officer");

        loadProfile();
        if (btnSave != null)
            btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        String uid = spm.getUid();
        if (uid == null) return;
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_USERS)
                .document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String phone = doc.getString("phone");
                        if (etPhone != null && phone != null)
                            etPhone.setText(phone);
                    }
                });
    }

    private void saveProfile() {
        String name = etName != null
                ? etName.getText().toString().trim() : "";
        String phone = etPhone != null
                ? etPhone.getText().toString().trim() : "";
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this,
                    "Name cannot be empty",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = spm.getUid();
        if (uid == null) return;
        pb.setVisibility(View.VISIBLE);
        if (btnSave != null) btnSave.setEnabled(false);
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", name);
        updates.put("phone", phone);
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_USERS)
                .document(uid).update(updates)
                .addOnSuccessListener(r -> {
                    pb.setVisibility(View.GONE);
                    if (btnSave != null)
                        btnSave.setEnabled(true);
                    spm.saveUser(uid, spm.getUsername(),
                            spm.getRole(), name, spm.getEmail());
                    Toast.makeText(this,
                            "Profile updated!",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    if (btnSave != null)
                        btnSave.setEnabled(true);
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}