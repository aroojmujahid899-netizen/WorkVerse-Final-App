package com.workverse.app.activities.admin;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.workverse.app.R;
import com.workverse.app.utils.FirebaseHelper;
public class EmployeeDetailActivity extends AppCompatActivity {
    TextView tvName,tvDesignation,tvEmail,tvPhone,tvDepartment,tvStatus;
    Button btnEdit,btnDelete;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_employee_detail);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        tvName=findViewById(R.id.tvName);tvDesignation=findViewById(R.id.tvDesignation);
        tvEmail=findViewById(R.id.tvEmail);tvPhone=findViewById(R.id.tvPhone);
        tvDepartment=findViewById(R.id.tvDepartment);tvStatus=findViewById(R.id.tvStatus);
        btnEdit=findViewById(R.id.btnEdit);btnDelete=findViewById(R.id.btnDelete);
        String id=getIntent().getStringExtra("id");
        if(id!=null) loadEmployee(id);
        btnDelete.setOnClickListener(v->{
            if(id!=null){
                FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).document(id).delete()
                    .addOnSuccessListener(r->{Toast.makeText(this,"Employee deleted",Toast.LENGTH_SHORT).show();finish();})
                    .addOnFailureListener(e->Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show());
            }
        });
    }
    private void loadEmployee(String id){
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).document(id).get()
            .addOnSuccessListener(d->{
                if(d.exists()){
                    tvName.setText(d.getString("name")!=null?d.getString("name"):"—");
                    tvDesignation.setText(d.getString("designation")!=null?d.getString("designation"):"—");
                    tvEmail.setText("Email: "+(d.getString("email")!=null?d.getString("email"):"—"));
                    tvPhone.setText("Phone: "+(d.getString("phone")!=null?d.getString("phone"):"—"));
                    tvDepartment.setText("Department: "+(d.getString("department")!=null?d.getString("department"):"—"));
                    tvStatus.setText("Status: "+(d.getString("status")!=null?d.getString("status"):"active"));
                }
            });
    }
}