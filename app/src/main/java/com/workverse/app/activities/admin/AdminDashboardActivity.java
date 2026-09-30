package com.workverse.app.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.NotificationBadgeHelper;
import com.workverse.app.utils.SharedPrefManager;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView tvAdminName, tvProfileInitial, tvTotalEmployees, tvPresentToday, tvPendingLeaves, tvTotalManagers;
    LinearLayout qaRoles, qaViewEmp, qaAddMgr, qaAttendance, qaLeave, qaPerformance, qaFeedback, qaSales, qaNotifications;
    CardView cardEmployees, cardPresentToday, cardPendingLeaves, cardManagers;
    ImageView btnMenu;
    View headerProfile;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    FirebaseFirestore db;
    SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_dashboard);

        spm = SharedPrefManager.getInstance(this);
        db = FirebaseHelper.getDb();

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        tvAdminName = findViewById(R.id.tvAdminName);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        headerProfile = findViewById(R.id.headerProfile);
        btnMenu = findViewById(R.id.btnMenu);

        String fullName = spm.getFullName();
        tvAdminName.setText(fullName != null ? fullName : "Admin");

        if (tvProfileInitial != null) {
            String initial = (fullName != null && !fullName.trim().isEmpty())
                    ? String.valueOf(fullName.trim().charAt(0)).toUpperCase()
                    : "A";
            tvProfileInitial.setText(initial);
        }

        if (headerProfile != null) headerProfile.setOnClickListener(v -> go(AdminProfileActivity.class));

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

        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                } else if (id == R.id.nav_alerts) {
                    go(AdminNotificationsActivity.class);
                } else if (id == R.id.nav_profile) {
                    go(AdminProfileActivity.class);
                } else if (id == R.id.nav_logout) {
                    logout();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }

        tvTotalEmployees = findViewById(R.id.tvTotalEmployees);
        tvPresentToday = findViewById(R.id.tvPresentToday);
        tvPendingLeaves = findViewById(R.id.tvPendingLeaves);
        tvTotalManagers = findViewById(R.id.tvTotalManagers);

        cardEmployees = findViewById(R.id.cardEmployees);
        cardPresentToday = findViewById(R.id.cardPresentToday);
        cardPendingLeaves = findViewById(R.id.cardPendingLeaves);
        cardManagers = findViewById(R.id.cardManagers);

        if (cardEmployees != null) cardEmployees.setOnClickListener(v -> go(ViewEmployeesActivity.class));
        if (cardPresentToday != null) cardPresentToday.setOnClickListener(v -> go(AdminAttendanceActivity.class));
        if (cardPendingLeaves != null) cardPendingLeaves.setOnClickListener(v -> go(AdminLeaveManagementActivity.class));
        if (cardManagers != null) cardManagers.setOnClickListener(v -> go(ViewManagersActivity.class));

        qaRoles = findViewById(R.id.qaRoles);
        qaViewEmp = findViewById(R.id.qaViewEmp);
        qaAddMgr = findViewById(R.id.qaAddMgr);
        qaAttendance = findViewById(R.id.qaAttendance);
        qaLeave = findViewById(R.id.qaLeave);
        qaPerformance = findViewById(R.id.qaPerformance);
        qaFeedback = findViewById(R.id.qaFeedback);
        qaSales = findViewById(R.id.qaSales);
        qaNotifications = findViewById(R.id.qaNotifications);

        qaRoles.setOnClickListener(v -> go(ManageRolesActivity.class));
        qaViewEmp.setOnClickListener(v -> go(AddEmployeeActivity.class));
        qaAddMgr.setOnClickListener(v -> go(AddManagerActivity.class));
        qaAttendance.setOnClickListener(v -> go(AdminAttendanceActivity.class));
        qaLeave.setOnClickListener(v -> go(AdminLeaveManagementActivity.class));
        qaPerformance.setOnClickListener(v -> go(AdminPerformanceActivity.class));
        qaFeedback.setOnClickListener(v -> go(AdminFeedbackActivity.class));
        qaSales.setOnClickListener(v -> go(AdminSalesReportActivity.class));
        qaNotifications.setOnClickListener(v -> go(AdminNotificationsActivity.class));

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
        updateNotifBadge();
    }

    private void updateNotifBadge() {
        TextView tvNotifBadge = findViewById(R.id.tvNotifBadge);
        if (tvNotifBadge == null) return;
        NotificationBadgeHelper.fetchUnreadCount(this, count -> {
            if (count > 0) {
                tvNotifBadge.setVisibility(View.VISIBLE);
                tvNotifBadge.setText(count > 9 ? "9+" : String.valueOf(count));
            } else {
                tvNotifBadge.setVisibility(View.GONE);
            }
        });
    }

    private void loadStats() {
        db.collection(FirebaseHelper.COL_EMPLOYEES).get()
                .addOnSuccessListener(s -> tvTotalEmployees.setText(String.valueOf(s.size())));
        db.collection(FirebaseHelper.COL_MANAGERS).get()
                .addOnSuccessListener(s -> tvTotalManagers.setText(String.valueOf(s.size())));
        db.collection(FirebaseHelper.COL_LEAVES).whereEqualTo("status", "Pending").get()
                .addOnSuccessListener(s -> tvPendingLeaves.setText(String.valueOf(s.size())));
        db.collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("date", com.workverse.app.utils.DateTimeUtils.getCurrentDate())
                .whereEqualTo("status", "Present").get()
                .addOnSuccessListener(s -> tvPresentToday.setText(String.valueOf(s.size())));
    }

    private void go(Class<?> c) {
        startActivity(new Intent(this, c));
    }

    private void logout() {
        FirebaseHelper.getAuth().signOut();
        spm.clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}