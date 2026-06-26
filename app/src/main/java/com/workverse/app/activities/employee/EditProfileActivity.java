package com.workverse.app.activities.employee;
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
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class EditProfileActivity extends AppCompatActivity {
    Button btnSave; ProgressBar pb;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_edit_profile);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        btnSave=findViewById(R.id.btnSubmit);pb=findViewById(R.id.progressBar);
        List<TextInputEditText> eds=new ArrayList<>();
        findEditTexts((android.view.ViewGroup)((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0),eds);
        btnSave.setOnClickListener(v->{
            if(eds.size()<2){Toast.makeText(this,"Fill fields",Toast.LENGTH_SHORT).show();return;}
            String name=eds.get(0).getText().toString().trim();
            String phone=eds.get(1).getText().toString().trim();
            if(TextUtils.isEmpty(name)){Toast.makeText(this,"Name required",Toast.LENGTH_SHORT).show();return;}
            pb.setVisibility(View.VISIBLE);btnSave.setEnabled(false);
            String uid=SharedPrefManager.getInstance(this).getUid();
            Map<String,Object> updates=new HashMap<>();
            updates.put("fullName",name);updates.put("phone",phone);
            FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).update(updates)
                .addOnSuccessListener(r->{pb.setVisibility(View.GONE);
                    SharedPrefManager.getInstance(this).saveUser(uid,SharedPrefManager.getInstance(this).getUsername(),"Employee",name,SharedPrefManager.getInstance(this).getEmail());
                    Toast.makeText(this,"Profile updated!",Toast.LENGTH_SHORT).show();finish();})
                .addOnFailureListener(e->{pb.setVisibility(View.GONE);btnSave.setEnabled(true);Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show();});
        });
    }
    private void findEditTexts(android.view.ViewGroup vg,List<TextInputEditText> list){
        for(int i=0;i<vg.getChildCount();i++){View c=vg.getChildAt(i);
            if(c instanceof TextInputEditText)list.add((TextInputEditText)c);
            else if(c instanceof android.view.ViewGroup)findEditTexts((android.view.ViewGroup)c,list);}
    }
}