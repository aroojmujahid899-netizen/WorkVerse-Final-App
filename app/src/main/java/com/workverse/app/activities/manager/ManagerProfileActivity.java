package com.workverse.app.activities.manager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.HashMap;
import java.util.Map;

public class ManagerProfileActivity extends AppCompatActivity {
    TextView tvName, tvEmail, tvRole, tvPhone, tvDepartment;
    Button btnEdit, btnLogout;
    SharedPrefManager spm;

    String currentEmail = "", currentPhone = "", currentDepartment = "";

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_profile);
        spm = SharedPrefManager.getInstance(this);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        tvPhone = findViewById(R.id.tvPhone);
        tvDepartment = findViewById(R.id.tvDepartment);
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
        tvName.setText(spm.getFullName() != null ? spm.getFullName() : "Manager");
        currentEmail = spm.getEmail() != null ? spm.getEmail() : "";
        tvEmail.setText("Email: " + (currentEmail.isEmpty() ? "—" : currentEmail));
        if (tvRole != null) tvRole.setText("Role: Manager");
    }

    private void loadFullProfileFromFirestore() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS)
                .document(spm.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String department = doc.getString("department");

                        currentEmail = email != null ? email : "";
                        currentPhone = phone != null ? phone : "";
                        currentDepartment = department != null ? department : "";

                        tvEmail.setText("Email: " + (currentEmail.isEmpty() ? "—" : currentEmail));
                        tvPhone.setText("Phone: " + (currentPhone.isEmpty() ? "—" : currentPhone));
                        tvDepartment.setText("Department: " + (currentDepartment.isEmpty() ? "—" : currentDepartment));
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

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(currentEmail);
        etEmail.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone");
        etPhone.setText(currentPhone);
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        EditText etDepartment = new EditText(this);
        etDepartment.setHint("Department");
        etDepartment.setText(currentDepartment);

        ll.addView(etName);
        ll.addView(etEmail);
        ll.addView(etPhone);
        ll.addView(etDepartment);

        new AlertDialog.Builder(this).setTitle("Edit Profile").setView(ll)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String department = etDepartment.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(this, "Email required", Toast.LENGTH_SHORT).show(); return;
                    }

                    Map<String, Object> upd = new HashMap<>();
                    upd.put("fullName", name);
                    upd.put("email", email);
                    upd.put("phone", phone);
                    upd.put("department", department);

                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS)
                            .document(spm.getUid()).update(upd)
                            .addOnSuccessListener(r -> {
                                spm.saveUser(spm.getUid(), spm.getUsername(), "Manager", name, email);
                                currentEmail = email;
                                currentPhone = phone;
                                currentDepartment = department;
                                refreshUIFromCache();
                                tvEmail.setText("Email: " + (email.isEmpty() ? "—" : email));
                                tvPhone.setText("Phone: " + (phone.isEmpty() ? "—" : phone));
                                tvDepartment.setText("Department: " + (department.isEmpty() ? "—" : department));
                                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }
}