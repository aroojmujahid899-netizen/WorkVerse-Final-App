
package com.workverse.app.activities.manager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class ManagerDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_dashboard);

        SharedPrefManager spm = SharedPrefManager.getInstance(this);
        TextView tvName = findViewById(R.id.tvUserName);
        if (tvName != null)
            tvName.setText(spm.getFullName() != null
                    ? spm.getFullName() : "Manager");

        // View Team Members — NEW
        LinearLayout qaTeamMembers =
                findViewById(R.id.qaTeamMembers);
        LinearLayout qaAttendance =
                findViewById(R.id.qaAttendance);
        // Team Leave Approve/Cancel
        LinearLayout qaTeamLeave =
                findViewById(R.id.qaTeamLeave);
        LinearLayout qaPerformance =
                findViewById(R.id.qaPerformance);
        // Feedback — View Only
        LinearLayout qaFeedback =
                findViewById(R.id.qaFeedback);
        LinearLayout qaSales =
                findViewById(R.id.qaSales);
        // Notifications — Send + View
        LinearLayout qaNotifications =
                findViewById(R.id.qaNotifications);
        LinearLayout navProfile = findViewById(R.id.navProfile);
        LinearLayout navNotif   = findViewById(R.id.navNotif);
        LinearLayout navLogout  = findViewById(R.id.navLogout);

        if (qaTeamMembers != null)
            qaTeamMembers.setOnClickListener(v ->
                    go(ManagerTeamActivity.class));
        if (qaAttendance != null)
            qaAttendance.setOnClickListener(v ->
                    go(ManagerAttendanceActivity.class));
        if (qaTeamLeave != null)
            qaTeamLeave.setOnClickListener(v ->
                    go(ManagerLeaveApprovalActivity.class));
        if (qaPerformance != null)
            qaPerformance.setOnClickListener(v ->
                    go(ManagerPerformanceActivity.class));
        if (qaFeedback != null)
            qaFeedback.setOnClickListener(v ->
                    go(ManagerFeedbackActivity.class));
        if (qaSales != null)
            qaSales.setOnClickListener(v ->
                    go(ManagerSalesReportActivity.class));
        if (qaNotifications != null)
            qaNotifications.setOnClickListener(v ->
                    go(ManagerNotificationsActivity.class));
        if (navNotif != null)
            navNotif.setOnClickListener(v ->
                    go(ManagerNotificationsActivity.class));
        if (navProfile != null)
            navProfile.setOnClickListener(v ->
                    go(ManagerProfileActivity.class));
        if (navLogout != null)
            navLogout.setOnClickListener(v -> {
                FirebaseHelper.getAuth().signOut();
                spm.clear();
                Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });
        loadStats();
    }

    @Override
    protected void onResume() { super.onResume(); loadStats(); }

    private void loadStats() {
        String uid =
                SharedPrefManager.getInstance(this).getUid();
        if (uid == null) return;
        TextView tvPresent = findViewById(R.id.tvPresentDays);
        TextView tvLeave   = findViewById(R.id.tvLeaveCount);
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", "Present").get()
                .addOnSuccessListener(s -> {
                    if (tvPresent != null)
                        tvPresent.setText(String.valueOf(s.size()));
                });
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .whereEqualTo("userId", uid).get()
                .addOnSuccessListener(s -> {
                    if (tvLeave != null)
                        tvLeave.setText(String.valueOf(s.size()));
                });
    }

    private void go(Class<?> c) {
        startActivity(new Intent(this, c));
    }
}
