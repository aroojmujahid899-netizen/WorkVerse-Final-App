package com.workverse.app.activities.ceo;

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
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.NotificationBadgeHelper;
import com.workverse.app.utils.SharedPrefManager;

public class CEODashboardActivity extends AppCompatActivity {

    TextView tvCEOName, tvTotalEmployees, tvPresentToday,
            tvTotalManagers, tvAvgKPI;
    LinearLayout qaPerformance, qaAttendance, qaFeedback,
            qaSales, qaNotifications, qaOverallPerformance, headerTextArea;
    CardView cardEmployees, cardPresentToday, cardManagers, cardAvgKPI;
    CardView ivProfilePic;
    TextView tvProfileInitial;
    ImageView ivNotif, btnMenu;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_dashboard);

        spm = SharedPrefManager.getInstance(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        tvCEOName        = findViewById(R.id.tvCEOName);
        tvTotalEmployees = findViewById(R.id.tvTotalEmployees);
        tvPresentToday   = findViewById(R.id.tvPresentToday);
        tvTotalManagers  = findViewById(R.id.tvTotalManagers);
        tvAvgKPI         = findViewById(R.id.tvAvgKPI);
        ivNotif          = findViewById(R.id.ivNotif);
        btnMenu          = findViewById(R.id.btnMenu);
        ivProfilePic     = findViewById(R.id.ivProfilePic);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        headerTextArea   = findViewById(R.id.headerTextArea);

        String fullName = spm.getFullName();
        if (tvCEOName != null)
            tvCEOName.setText(fullName != null ? fullName : "CEO");

        if (tvProfileInitial != null && fullName != null && !fullName.trim().isEmpty())
            tvProfileInitial.setText(fullName.trim().substring(0, 1).toUpperCase());

        if (ivProfilePic != null)
            ivProfilePic.setOnClickListener(v -> go(CEOProfileActivity.class));

        if (headerTextArea != null)
            headerTextArea.setOnClickListener(v -> go(CEOProfileActivity.class));

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
                    go(CEONotificationsActivity.class);
                } else if (id == R.id.nav_profile) {
                    go(CEOProfileActivity.class);
                } else if (id == R.id.nav_logout) {
                    logout();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }

        cardEmployees    = findViewById(R.id.cardEmployees);
        cardPresentToday = findViewById(R.id.cardPresentToday);
        cardManagers     = findViewById(R.id.cardManagers);
        cardAvgKPI       = findViewById(R.id.cardAvgKPI);

        if (cardEmployees != null)
            cardEmployees.setOnClickListener(v -> go(com.workverse.app.activities.admin.ViewEmployeesActivity.class));
        if (cardPresentToday != null)
            cardPresentToday.setOnClickListener(v -> go(CEOAttendanceActivity.class));
        if (cardManagers != null)
            cardManagers.setOnClickListener(v -> go(com.workverse.app.activities.admin.ViewManagersActivity.class));
        if (cardAvgKPI != null)
            cardAvgKPI.setOnClickListener(v -> go(CEOPerformanceActivity.class));

        qaPerformance        = findViewById(R.id.qaPerformance);
        qaAttendance         = findViewById(R.id.qaAttendance);
        qaFeedback           = findViewById(R.id.qaFeedback);
        qaSales              = findViewById(R.id.qaSales);
        qaNotifications      = findViewById(R.id.qaNotifications);
        qaOverallPerformance = findViewById(R.id.qaReports);

        if (qaPerformance != null)
            qaPerformance.setOnClickListener(v -> go(CEOPerformanceActivity.class));
        if (qaAttendance != null)
            qaAttendance.setOnClickListener(v -> go(CEOAttendanceActivity.class));
        if (qaFeedback != null)
            qaFeedback.setOnClickListener(v -> go(CEOFeedbackActivity.class));
        if (qaSales != null)
            qaSales.setOnClickListener(v -> go(CEOSalesReportActivity.class));
        if (qaNotifications != null)
            qaNotifications.setOnClickListener(v -> go(CEONotificationsActivity.class));
        if (qaOverallPerformance != null)
            qaOverallPerformance.setOnClickListener(v -> go(CEOAllReportsActivity.class));

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

    private void go(Class<?> c) {
        startActivity(new Intent(this, c));
    }

    private void loadStats() {
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_EMPLOYEES).get()
                .addOnSuccessListener(s -> {
                    if (tvTotalEmployees != null)
                        tvTotalEmployees.setText(String.valueOf(s.size())); });
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_MANAGERS).get()
                .addOnSuccessListener(s -> {
                    if (tvTotalManagers != null)
                        tvTotalManagers.setText(String.valueOf(s.size())); });
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("date", DateTimeUtils.getCurrentDate())
                .whereEqualTo("status", "Present").get()
                .addOnSuccessListener(s -> {
                    if (tvPresentToday != null)
                        tvPresentToday.setText(String.valueOf(s.size())); });
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_PERFORMANCE).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        if (tvAvgKPI != null) tvAvgKPI.setText("N/A");
                        return;
                    }
                    double total = 0; int count = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot d : snap) {
                        Double kpi = d.getDouble("kpiScore");
                        if (kpi != null) { total += kpi; count++; }
                    }
                    double avg = count > 0 ? total / count : 0;
                    if (tvAvgKPI != null) tvAvgKPI.setText(String.format("%.0f%%", avg));
                });
    }

    private void logout() {
        FirebaseHelper.getAuth().signOut();
        spm.clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}