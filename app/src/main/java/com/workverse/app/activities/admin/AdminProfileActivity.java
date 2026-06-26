package com.workverse.app.activities.admin;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.HashMap;
import java.util.Map;
public class AdminProfileActivity extends AppCompatActivity {
    TextView tvName, tvEmail, tvRole; Button btnEdit, btnLogout;
    SharedPrefManager spm;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_profile);
        spm = SharedPrefManager.getInstance(this);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvRole = findViewById(R.id.tvRole);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        refreshUI();
        if (btnEdit != null) btnEdit.setOnClickListener(v -> showEditDialog());
        btnLogout.setOnClickListener(v -> {
            FirebaseHelper.getAuth().signOut(); spm.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
    private void refreshUI() {
        tvName.setText(spm.getFullName() != null ? spm.getFullName() : "Admin");
        tvEmail.setText("Email: " + (spm.getEmail() != null ? spm.getEmail() : "—"));
        tvRole.setText("Role: Admin");
    }
    private void showEditDialog() {
        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);
        EditText etName = new EditText(this);
        etName.setHint("Full Name");
        etName.setText(spm.getFullName());
        ll.addView(etName);
        new AlertDialog.Builder(this).setTitle("Edit Profile").setView(ll)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) { Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show(); return; }
                    Map<String, Object> upd = new HashMap<>();
                    upd.put("fullName", name);
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS)
                            .document(spm.getUid()).update(upd)
                            .addOnSuccessListener(r -> {
                                spm.saveUser(spm.getUid(), spm.getUsername(), "Admin", name, spm.getEmail());
                                refreshUI();
                                Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }
}