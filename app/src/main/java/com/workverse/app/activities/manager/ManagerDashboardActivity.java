package com.workverse.app.activities.manager;

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
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.NotificationBadgeHelper;
import com.workverse.app.utils.SharedPrefManager;

public class ManagerDashboardActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView btnMenu;
    View headerProfile;
    CardView ivProfilePic;
    TextView tvProfileInitial;
    CardView cardPresentDays, cardLeaveCount;
    SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        spm = SharedPrefManager.getInstance(this);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnMenu = findViewById(R.id.btnMenu);
        headerProfile = findViewById(R.id.headerProfile);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);

        TextView tvName = findViewById(R.id.tvUserName);
        if (tvName != null)
            tvName.setText(spm.getFullName() != null ? spm.getFullName() : "Manager");

        if (tvProfileInitial != null) {
            String name = spm.getFullName();
            String initial = (name != null && !name.trim().isEmpty())
                    ? String.valueOf(name.trim().charAt(0)).toUpperCase()
                    : "M";
            tvProfileInitial.setText(initial);
        }

        if (headerProfile != null)
            headerProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerProfileActivity.class)));

        if (ivProfilePic != null)
            ivProfilePic.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerProfileActivity.class)));

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
                    startActivity(new Intent(this, ManagerNotificationsActivity.class));
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, ManagerProfileActivity.class));
                } else if (id == R.id.nav_logout) {
                    logout();
                }

                if (drawerLayout != null) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            });
        }

        cardPresentDays = findViewById(R.id.cardPresentDays);
        cardLeaveCount = findViewById(R.id.cardLeaveCount);

        if (cardPresentDays != null)
            cardPresentDays.setOnClickListener(v -> {
                Intent i = new Intent(this, ManagerAttendanceActivity.class);
                i.putExtra(ManagerAttendanceActivity.EXTRA_MY_ATTENDANCE, true);
                startActivity(i);
            });
        if (cardLeaveCount != null)
            cardLeaveCount.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerLeaveStatusActivity.class)));

        LinearLayout qaMyAttendance  = findViewById(R.id.qaMyAttendance);
        LinearLayout qaAttendance    = findViewById(R.id.qaAttendance);
        LinearLayout qaMyLeave       = findViewById(R.id.qaMyLeave);
        LinearLayout qaTeamLeave     = findViewById(R.id.qaTeamLeave);
        LinearLayout qaPerformance   = findViewById(R.id.qaPerformance);
        LinearLayout qaFeedback      = findViewById(R.id.qaFeedback);
        LinearLayout qaSales         = findViewById(R.id.qaSales);
        LinearLayout qaNotifications = findViewById(R.id.qaNotifications);

        if (qaMyAttendance != null)
            qaMyAttendance.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerMarkAttendanceActivity.class)));

        if (qaAttendance != null)
            qaAttendance.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerAttendanceActivity.class)));

        if (qaMyLeave != null)
            qaMyLeave.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerLeaveStatusActivity.class)));

        if (qaTeamLeave != null)
            qaTeamLeave.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerLeaveApprovalActivity.class)));

        if (qaPerformance != null)
            qaPerformance.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerPerformanceActivity.class)));

        if (qaFeedback != null)
            qaFeedback.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerFeedbackActivity.class)));

        if (qaSales != null)
            qaSales.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerSalesReportActivity.class)));

        if (qaNotifications != null)
            qaNotifications.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerNotificationsActivity.class)));

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
        String uid = spm.getUid();
        if (uid == null) return;

        TextView tvPresent = findViewById(R.id.tvPresentDays);
        TextView tvLeave   = findViewById(R.id.tvLeaveCount);

        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", "Present")
                .get()
                .addOnSuccessListener(s -> {
                    if (tvPresent != null)
                        tvPresent.setText(String.valueOf(s.size()));
                });

        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(s -> {
                    if (tvLeave != null)
                        tvLeave.setText(String.valueOf(s.size()));
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