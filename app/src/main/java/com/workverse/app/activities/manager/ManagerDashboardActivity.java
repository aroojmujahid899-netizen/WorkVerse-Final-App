
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_dashboard);

        SharedPrefManager spm = SharedPrefManager.getInstance(this);

        // Welcome name
        TextView tvName = findViewById(R.id.tvUserName);
        if (tvName != null)
            tvName.setText(spm.getFullName() != null ? spm.getFullName() : "Manager");

        // Quick action buttons
        LinearLayout qaMyAttendance  = findViewById(R.id.qaMyAttendance); // Manager's OWN check-in/out
        LinearLayout qaAttendance    = findViewById(R.id.qaAttendance);   // Team attendance (view)
        LinearLayout qaMyLeave       = findViewById(R.id.qaMyLeave);      // Manager's OWN leave
        LinearLayout qaTeamLeave     = findViewById(R.id.qaTeamLeave);    // Approve Employee leaves
        LinearLayout qaPerformance   = findViewById(R.id.qaPerformance);
        LinearLayout qaFeedback      = findViewById(R.id.qaFeedback);
        LinearLayout qaSales         = findViewById(R.id.qaSales);
        LinearLayout qaNotifications = findViewById(R.id.qaNotifications);

        // Bottom nav
        LinearLayout navHome    = findViewById(R.id.navHome);
        LinearLayout navProfile = findViewById(R.id.navProfile);
        LinearLayout navLogout  = findViewById(R.id.navLogout);

        // My Attendance — Manager marks their own check-in/check-out
        if (qaMyAttendance != null)
            qaMyAttendance.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerMarkAttendanceActivity.class)));

        // Attendance — view team attendance
        if (qaAttendance != null)
            qaAttendance.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerAttendanceActivity.class)));

        // My Leave — Manager applies leave / views own leave status
        if (qaMyLeave != null)
            qaMyLeave.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerLeaveStatusActivity.class)));

        // Team Leaves — Manager approves/rejects Employee leaves
        if (qaTeamLeave != null)
            qaTeamLeave.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerLeaveApprovalActivity.class)));

        // Performance
        if (qaPerformance != null)
            qaPerformance.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerPerformanceActivity.class)));

        // Feedback
        if (qaFeedback != null)
            qaFeedback.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerFeedbackActivity.class)));

        // Sales
        if (qaSales != null)
            qaSales.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerSalesReportActivity.class)));

        // Notifications
        if (qaNotifications != null)
            qaNotifications.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerNotificationsActivity.class)));

        // Profile
        if (navProfile != null)
            navProfile.setOnClickListener(v ->
                    startActivity(new Intent(this, ManagerProfileActivity.class)));

        // Logout
        if (navLogout != null)
            navLogout.setOnClickListener(v -> {
                FirebaseHelper.getAuth().signOut();
                spm.clear();
                Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });

        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        String uid = SharedPrefManager.getInstance(this).getUid();
        if (uid == null) return;

        TextView tvPresent = findViewById(R.id.tvPresentDays);
        TextView tvLeave   = findViewById(R.id.tvLeaveCount);

        // Manager's own present days
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", "Present")
                .get()
                .addOnSuccessListener(s -> {
                    if (tvPresent != null)
                        tvPresent.setText(String.valueOf(s.size()));
                });

        // Manager's own leave count
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(s -> {
                    if (tvLeave != null)
                        tvLeave.setText(String.valueOf(s.size()));
                });
    }
}
