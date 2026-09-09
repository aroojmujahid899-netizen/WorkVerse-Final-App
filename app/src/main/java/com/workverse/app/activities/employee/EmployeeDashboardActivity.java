package com.workverse.app.activities.employee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class EmployeeDashboardActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView btnMenu, ivNotif;
    CardView cardPresentDays, cardLeaveCount;
    SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        spm = SharedPrefManager.getInstance(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenu = findViewById(R.id.btnMenu);
        ivNotif = findViewById(R.id.ivNotif);

        TextView tvName = findViewById(R.id.tvUserName);
        if (tvName != null) tvName.setText(spm.getFullName() != null ? spm.getFullName() : "Employee");

        // Hamburger menu click
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
            });
        }

        // Side drawer menu clicks
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    // Current Screen
                } else if (id == R.id.nav_alerts) {
                    startActivity(new Intent(this, EmployeeNotificationsActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, EmployeeProfileActivity.class));
                } else if (id == R.id.nav_logout) {
                    logout();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }

        // Clickable stat cards
        cardPresentDays = findViewById(R.id.cardPresentDays);
        cardLeaveCount = findViewById(R.id.cardLeaveCount);

        if (cardPresentDays != null)
            cardPresentDays.setOnClickListener(v -> startActivity(new Intent(this, ViewAttendanceActivity.class)));
        if (cardLeaveCount != null)
            cardLeaveCount.setOnClickListener(v -> startActivity(new Intent(this, ViewLeaveStatusActivity.class)));

        // Quick Actions
        LinearLayout qaAttendance = findViewById(R.id.qaAttendance);
        LinearLayout qaLeave = findViewById(R.id.qaLeave);
        LinearLayout qaPerformance = findViewById(R.id.qaPerformance);
        LinearLayout qaFeedback = findViewById(R.id.qaFeedback);
        LinearLayout qaSales = findViewById(R.id.qaSales);
        LinearLayout qaNotifications = findViewById(R.id.qaNotifications);
        LinearLayout qaFaq = findViewById(R.id.qaFaq);

        if (qaAttendance != null) qaAttendance.setOnClickListener(v -> startActivity(new Intent(this, MarkAttendanceActivity.class)));
        if (qaLeave != null) qaLeave.setOnClickListener(v -> startActivity(new Intent(this, ViewLeaveStatusActivity.class)));
        if (qaPerformance != null) qaPerformance.setOnClickListener(v -> startActivity(new Intent(this, EmployeePerformanceActivity.class)));
        if (qaFeedback != null) qaFeedback.setOnClickListener(v -> startActivity(new Intent(this, SubmitFeedbackActivity.class)));
        if (qaSales != null) qaSales.setOnClickListener(v -> startActivity(new Intent(this, EmployeeSalesReportActivity.class)));
        if (qaNotifications != null) qaNotifications.setOnClickListener(v -> startActivity(new Intent(this, EmployeeNotificationsActivity.class)));
        if (qaFaq != null) qaFaq.setOnClickListener(v -> startActivity(new Intent(this, ViewFAQActivity.class)));

        if (ivNotif != null) ivNotif.setOnClickListener(v -> startActivity(new Intent(this, EmployeeNotificationsActivity.class)));

        loadStats();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        String uid = spm.getUid();
        TextView tvPresent = findViewById(R.id.tvPresentDays);
        TextView tvLeave = findViewById(R.id.tvLeaveCount);

        if (uid != null) {
            FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                    .whereEqualTo("userId", uid).whereEqualTo("status", "Present").get()
                    .addOnSuccessListener(s -> { if (tvPresent != null) tvPresent.setText(String.valueOf(s.size())); });

            FirebaseHelper.getDb().collection(FirebaseHelper.COL_LEAVES)
                    .whereEqualTo("userId", uid).get()
                    .addOnSuccessListener(s -> { if (tvLeave != null) tvLeave.setText(String.valueOf(s.size())); });
        }
    }

    private void logout() {
        FirebaseHelper.getAuth().signOut();
        spm.clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}