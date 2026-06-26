package com.workverse.app.activities.admin;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.workverse.app.R;
import com.workverse.app.models.Employee;
import com.workverse.app.models.User;
import com.workverse.app.utils.FirebaseHelper;
import java.util.HashMap;
import java.util.Map;
public class AddEmployeeActivity extends AppCompatActivity {
    TextInputEditText etName,etUsername,etEmail,etPhone,etDesig,etDept,etPass;
    Button btnSubmit; ProgressBar pb;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_add_employee);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        btnSubmit=findViewById(R.id.btnSubmit); pb=findViewById(R.id.progressBar);
        // get all TextInputEditTexts from layouts
        android.view.ViewGroup root=(android.view.ViewGroup)((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0);
        java.util.List<TextInputEditText> eds = new java.util.ArrayList<>();
        findEditTexts(root, eds);
        if(eds.size()>=7){etName=eds.get(0);etUsername=eds.get(1);etEmail=eds.get(2);etPhone=eds.get(3);etDesig=eds.get(4);etDept=eds.get(5);etPass=eds.get(6);}
        btnSubmit.setOnClickListener(v->addEmployee());
    }
    private void findEditTexts(android.view.ViewGroup vg, java.util.List<TextInputEditText> list){
        for(int i=0;i<vg.getChildCount();i++){
            View c=vg.getChildAt(i);
            if(c instanceof TextInputEditText) list.add((TextInputEditText)c);
            else if(c instanceof android.view.ViewGroup) findEditTexts((android.view.ViewGroup)c,list);
        }
    }
    private void addEmployee(){
        String name=etName!=null?etName.getText().toString().trim():"";
        String username=etUsername!=null?etUsername.getText().toString().trim():"";
        String email=etEmail!=null?etEmail.getText().toString().trim():"";
        String phone=etPhone!=null?etPhone.getText().toString().trim():"";
        String desig=etDesig!=null?etDesig.getText().toString().trim():"";
        String dept=etDept!=null?etDept.getText().toString().trim():"";
        String pass=etPass!=null?etPass.getText().toString().trim():"";
        if(TextUtils.isEmpty(name)||TextUtils.isEmpty(email)||TextUtils.isEmpty(pass)){
            Toast.makeText(this,"Name, Email and Password are required",Toast.LENGTH_SHORT).show();return;}
        pb.setVisibility(View.VISIBLE); btnSubmit.setEnabled(false);
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(email,pass)
            .addOnSuccessListener(res->{
                String uid=res.getUser().getUid();
                Map<String,Object> userMap=new HashMap<>();
                userMap.put("uid",uid);userMap.put("username",username);userMap.put("email",email);
                userMap.put("fullName",name);userMap.put("phone",phone);userMap.put("role","Employee");
                userMap.put("department",dept);userMap.put("designation",desig);
                FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).set(userMap);
                Map<String,Object> emp=new HashMap<>();
                emp.put("name",name);emp.put("email",email);emp.put("phone",phone);
                emp.put("designation",desig);emp.put("department",dept);
                emp.put("userId",uid);emp.put("status","active");
                FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).document(uid).set(emp)
                    .addOnSuccessListener(r->{pb.setVisibility(View.GONE);
                        Toast.makeText(this,"Employee added!",Toast.LENGTH_SHORT).show();finish();})
                    .addOnFailureListener(e->{pb.setVisibility(View.GONE);btnSubmit.setEnabled(true);
                        Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();});
            })
            .addOnFailureListener(e->{pb.setVisibility(View.GONE);btnSubmit.setEnabled(true);
                Toast.makeText(this,"Auth error: "+e.getMessage(),Toast.LENGTH_SHORT).show();});
    }
}