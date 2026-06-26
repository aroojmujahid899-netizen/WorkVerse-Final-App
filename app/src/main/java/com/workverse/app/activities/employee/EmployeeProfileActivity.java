package com.workverse.app.activities.employee;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
public class EmployeeProfileActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_employee_profile);
        SharedPrefManager spm=SharedPrefManager.getInstance(this);
        ((TextView)findViewById(R.id.tvName)).setText(spm.getFullName()!=null?spm.getFullName():"Employee");
        ((TextView)findViewById(R.id.tvEmail)).setText("Email: "+(spm.getEmail()!=null?spm.getEmail():"—"));
        ((TextView)findViewById(R.id.tvRole)).setText("Employee");
        Button btnEdit=findViewById(R.id.btnEditProfile);
        Button btnLogout=findViewById(R.id.btnLogout);
        if(btnEdit!=null) btnEdit.setOnClickListener(v->startActivity(new Intent(this,EditProfileActivity.class)));
        btnLogout.setOnClickListener(v->{
            FirebaseHelper.getAuth().signOut();spm.clear();
            Intent i=new Intent(this,LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}