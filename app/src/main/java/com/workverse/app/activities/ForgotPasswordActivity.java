package com.workverse.app.activities;
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
public class ForgotPasswordActivity extends AppCompatActivity {
    TextInputEditText etEmail; Button btnReset; ProgressBar progressBar;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_forgot_password);
        Toolbar tb=findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v->finish());
        etEmail=findViewById(R.id.etEmail);
        btnReset=findViewById(R.id.btnReset);
        progressBar=findViewById(R.id.progressBar);
        btnReset.setOnClickListener(v->{
            String email=etEmail.getText().toString().trim();
            if(TextUtils.isEmpty(email)){Toast.makeText(this,"Enter email",Toast.LENGTH_SHORT).show();return;}
            progressBar.setVisibility(View.VISIBLE); btnReset.setEnabled(false);
            FirebaseHelper.getAuth().sendPasswordResetEmail(email)
                .addOnSuccessListener(r->{progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,"Reset link sent to email",Toast.LENGTH_LONG).show();finish();})
                .addOnFailureListener(e->{progressBar.setVisibility(View.GONE);btnReset.setEnabled(true);
                    Toast.makeText(this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();});
        });
    }
}