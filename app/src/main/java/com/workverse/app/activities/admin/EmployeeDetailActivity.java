package com.workverse.app.activities.admin;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.workverse.app.R;
import com.workverse.app.utils.FirebaseHelper;
import java.util.HashMap;
import java.util.Map;

public class EmployeeDetailActivity extends AppCompatActivity {
    TextView tvName,tvDesignation,tvEmail,tvPhone,tvDepartment,tvStatus;
    Button btnEdit,btnDelete;
    String employeeId;

    String currentName="", currentDesignation="", currentEmail="", currentPhone="";

    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_employee_detail);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        tvName=findViewById(R.id.tvName);tvDesignation=findViewById(R.id.tvDesignation);
        tvEmail=findViewById(R.id.tvEmail);tvPhone=findViewById(R.id.tvPhone);
        tvDepartment=findViewById(R.id.tvDepartment);tvStatus=findViewById(R.id.tvStatus);
        btnEdit=findViewById(R.id.btnEdit);btnDelete=findViewById(R.id.btnDelete);
        employeeId=getIntent().getStringExtra("id");
        if(employeeId!=null) loadEmployee(employeeId);

        btnEdit.setOnClickListener(v -> showEditDialog());

        btnDelete.setOnClickListener(v->{
            if(employeeId!=null){
                FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).document(employeeId).delete()
                        .addOnSuccessListener(r->{Toast.makeText(this,"Employee deleted",Toast.LENGTH_SHORT).show();finish();})
                        .addOnFailureListener(e->Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadEmployee(String id){
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).document(id).get()
                .addOnSuccessListener(d->{
                    if(d.exists()){
                        currentName = d.getString("name") != null ? d.getString("name") : "";
                        currentDesignation = d.getString("designation") != null ? d.getString("designation") : "";
                        currentEmail = d.getString("email") != null ? d.getString("email") : "";
                        currentPhone = d.getString("phone") != null ? d.getString("phone") : "";

                        tvName.setText(!currentName.isEmpty() ? currentName : "—");
                        tvDesignation.setText(!currentDesignation.isEmpty() ? currentDesignation : "—");
                        tvEmail.setText("Email: "+(!currentEmail.isEmpty() ? currentEmail : "—"));
                        tvPhone.setText("Phone: "+(!currentPhone.isEmpty() ? currentPhone : "—"));
                        tvDepartment.setText("Department: "+(d.getString("department")!=null?d.getString("department"):"—"));
                        tvStatus.setText("Status: "+(d.getString("status")!=null?d.getString("status"):"active"));
                    }
                });
    }

    private void showEditDialog() {
        if (employeeId == null) return;

        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setText(currentName);

        EditText etDesignation = new EditText(this);
        etDesignation.setHint("Designation");
        etDesignation.setText(currentDesignation);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(currentEmail);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Phone");
        etPhone.setText(currentPhone);
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);

        ll.addView(etName);
        ll.addView(etDesignation);
        ll.addView(etEmail);
        ll.addView(etPhone);

        new AlertDialog.Builder(this).setTitle("Edit Employee").setView(ll)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String designation = etDesignation.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> upd = new HashMap<>();
                    upd.put("name", name);
                    upd.put("designation", designation);
                    upd.put("email", email);
                    upd.put("phone", phone);

                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES)
                            .document(employeeId).update(upd)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Employee updated!", Toast.LENGTH_SHORT).show();
                                loadEmployee(employeeId);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }
}