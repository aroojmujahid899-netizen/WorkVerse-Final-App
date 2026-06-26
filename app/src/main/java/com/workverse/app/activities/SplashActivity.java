package com.workverse.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.workverse.app.R;
import com.workverse.app.activities.admin.AdminDashboardActivity;
import com.workverse.app.activities.ceo.CEODashboardActivity;
import com.workverse.app.activities.employee.EmployeeDashboardActivity;
import com.workverse.app.activities.manager.ManagerDashboardActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            SharedPrefManager spm = SharedPrefManager.getInstance(this);
            if (spm.isLoggedIn()) {
                String role = spm.getRole();
                Intent i;
                if      (FirebaseHelper.ROLE_ADMIN.equals(role))    i = new Intent(this, AdminDashboardActivity.class);
                else if (FirebaseHelper.ROLE_CEO.equals(role))       i = new Intent(this, CEODashboardActivity.class);
                else if (FirebaseHelper.ROLE_MANAGER.equals(role))   i = new Intent(this, ManagerDashboardActivity.class);
                else                                                   i = new Intent(this, EmployeeDashboardActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 2000);
    }
}
