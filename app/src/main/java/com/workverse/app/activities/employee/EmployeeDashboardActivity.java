package com.workverse.app.activities.employee;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
public class EmployeeDashboardActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_employee_dashboard);
        SharedPrefManager spm = SharedPrefManager.getInstance(this);
        TextView tvName = findViewById(R.id.tvUserName);
        if (tvName != null) tvName.setText(spm.getFullName() != null ? spm.getFullName() : "Employee");
        LinearLayout qaAttendance = findViewById(R.id.qaAttendance);
        LinearLayout qaLeave = findViewById(R.id.qaLeave);
        LinearLayout qaPerformance = findViewById(R.id.qaPerformance);
        LinearLayout qaFeedback = findViewById(R.id.qaFeedback);
        LinearLayout qaSales = findViewById(R.id.qaSales);
        LinearLayout qaNotifications = findViewById(R.id.qaNotifications);
        LinearLayout navProfile = findViewById(R.id.navProfile);
        LinearLayout navLogout = findViewById(R.id.navLogout);
        if (qaAttendance != null) qaAttendance.setOnClickListener(v -> startActivity(new Intent(this, MarkAttendanceActivity.class)));
        // "My Leave" now opens the stats + list + apply combo screen, same experience as Manager's My Leave
        if (qaLeave != null) qaLeave.setOnClickListener(v -> startActivity(new Intent(this, ViewLeaveStatusActivity.class)));
        if (qaPerformance != null) qaPerformance.setOnClickListener(v -> startActivity(new Intent(this, EmployeePerformanceActivity.class)));
        if (qaFeedback != null) qaFeedback.setOnClickListener(v -> startActivity(new Intent(this, SubmitFeedbackActivity.class)));
        if (qaSales != null) qaSales.setOnClickListener(v -> startActivity(new Intent(this, EmployeeSalesReportActivity.class)));
        if (qaNotifications != null) qaNotifications.setOnClickListener(v -> startActivity(new Intent(this, EmployeeNotificationsActivity.class)));
        if (navProfile != null) navProfile.setOnClickListener(v -> startActivity(new Intent(this, EmployeeProfileActivity.class)));
        if (navLogout != null) navLogout.setOnClickListener(v -> {
            FirebaseHelper.getAuth().signOut(); spm.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
        loadStats();
    }
    @Override protected void onResume() { super.onResume(); loadStats(); }
    private void loadStats() {
        String uid = SharedPrefManager.getInstance(this).getUid();
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
}