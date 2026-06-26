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
import com.workverse.app.R;
import com.workverse.app.utils.FirebaseHelper;
import java.util.HashMap;
import java.util.Map;
public class AddManagerActivity extends AppCompatActivity {
    Button btnSubmit; ProgressBar pb;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_add_manager);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        btnSubmit=findViewById(R.id.btnSubmit); pb=findViewById(R.id.progressBar);
        java.util.List<TextInputEditText> eds=new java.util.ArrayList<>();
        findEditTexts((android.view.ViewGroup)((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0),eds);
        btnSubmit.setOnClickListener(v->{
            if(eds.size()<6){Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();return;}
            String name=eds.get(0).getText().toString().trim();
            String username=eds.get(1).getText().toString().trim();
            String email=eds.get(2).getText().toString().trim();
            String phone=eds.get(3).getText().toString().trim();
            String dept=eds.get(4).getText().toString().trim();
            String pass=eds.get(5).getText().toString().trim();
            if(TextUtils.isEmpty(name)||TextUtils.isEmpty(email)||TextUtils.isEmpty(pass)){
                Toast.makeText(this,"Required fields missing",Toast.LENGTH_SHORT).show();return;}
            pb.setVisibility(View.VISIBLE);btnSubmit.setEnabled(false);
            FirebaseHelper.getAuth().createUserWithEmailAndPassword(email,pass)
                .addOnSuccessListener(res->{
                    String uid=res.getUser().getUid();
                    Map<String,Object> um=new HashMap<>();
                    um.put("uid",uid);um.put("username",username);um.put("email",email);
                    um.put("fullName",name);um.put("phone",phone);um.put("role","Manager");um.put("department",dept);
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).set(um);
                    Map<String,Object> mm=new HashMap<>();
                    mm.put("name",name);mm.put("email",email);mm.put("phone",phone);mm.put("department",dept);mm.put("userId",uid);mm.put("status","active");
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_MANAGERS).document(uid).set(mm)
                        .addOnSuccessListener(r->{pb.setVisibility(View.GONE);Toast.makeText(this,"Manager added!",Toast.LENGTH_SHORT).show();finish();})
                        .addOnFailureListener(e->{pb.setVisibility(View.GONE);btnSubmit.setEnabled(true);Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();});
                })
                .addOnFailureListener(e->{pb.setVisibility(View.GONE);btnSubmit.setEnabled(true);Toast.makeText(this,"Auth error: "+e.getMessage(),Toast.LENGTH_SHORT).show();});
        });
    }
    private void findEditTexts(android.view.ViewGroup vg,java.util.List<TextInputEditText> list){
        for(int i=0;i<vg.getChildCount();i++){View c=vg.getChildAt(i);
            if(c instanceof TextInputEditText)list.add((TextInputEditText)c);
            else if(c instanceof android.view.ViewGroup)findEditTexts((android.view.ViewGroup)c,list);}
    }
}