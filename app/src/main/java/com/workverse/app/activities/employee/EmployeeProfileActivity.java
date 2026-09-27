package com.workverse.app.activities.employee;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.HashMap;
import java.util.Map;

public class EmployeeProfileActivity extends AppCompatActivity {
    TextView tvName, tvEmail, tvRole, tvPhone, tvDesignation, tvCampaign;
    Button btnEdit, btnLogout;
    SharedPrefManager spm;

    String currentEmail = "", currentPhone = "", currentDesignation = "", currentCampaign = "";

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_employee_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        spm = SharedPrefManager.getInstance(this);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvPhone = findViewById(R.id.tvPhone);
        tvDesignation = findViewById(R.id.tvDesignation);
        tvCampaign = findViewById(R.id.tvCampaign);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        refreshUIFromCache();
        loadFullProfileFromFirestore();

        if (btnEdit != null) btnEdit.setOnClickListener(v -> showEditDialog());
        btnLogout.setOnClickListener(v -> {
            FirebaseHelper.getAuth().signOut(); spm.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void refreshUIFromCache() {
        tvName.setText(spm.getFullName() != null ? spm.getFullName() : "Employee");
        currentEmail = spm.getEmail() != null ? spm.getEmail() : "";
        tvEmail.setText("Email: " + (currentEmail.isEmpty() ? "—" : currentEmail));
        if (tvRole != null) tvRole.setText("Employee");
    }

    private void loadFullProfileFromFirestore() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS)
                .document(spm.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String designation = doc.getString("designation");
                        String campaign = doc.getString("campaign");

                        currentEmail = email != null ? email : "";
                        currentPhone = phone != null ? phone : "";
                        currentDesignation = designation != null ? designation : "";
                        currentCampaign = campaign != null ? campaign : "";

                        tvEmail.setText("Email: " + (currentEmail.isEmpty() ? "—" : currentEmail));
                        tvPhone.setText("Phone: " + (currentPhone.isEmpty() ? "—" : currentPhone));
                        tvDesignation.setText("Designation: " + (currentDesignation.isEmpty() ? "—" : currentDesignation));
                        tvCampaign.setText("Campaign: " + (currentCampaign.isEmpty() ? "—" : currentCampaign));
                    }
                });
    }

    private void showEditDialog() {
        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        EditText etName = new EditText(this);
        etName.setHint("Full Name");
        etName.setText(spm.getFullName());

        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone");
        etPhone.setText(currentPhone);
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        ll.addView(etName);
        ll.addView(etPhone);

        new AlertDialog.Builder(this).setTitle("Edit Profile").setView(ll)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return;
                    }

                    Map<String, Object> upd = new HashMap<>();
                    upd.put("fullName", name);
                    upd.put("phone", phone);

                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS)
                            .document(spm.getUid()).update(upd)
                            .addOnSuccessListener(r -> {
                                spm.saveUser(spm.getUid(), spm.getUsername(), "Employee", name, spm.getEmail());
                                currentPhone = phone;
                                refreshUIFromCache();
                                tvPhone.setText("Phone: " + (phone.isEmpty() ? "—" : phone));
                                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }
}