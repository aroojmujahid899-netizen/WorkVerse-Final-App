package com.workverse.app.activities.ceo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class CEODashboardActivity extends AppCompatActivity {

    TextView tvCEOName, tvTotalEmployees, tvPresentToday,
            tvTotalManagers, tvAvgKPI;
    LinearLayout qaPerformance, qaAttendance, qaFeedback,
            qaSales, qaNotifications, qaOverallPerformance;
    LinearLayout navHome, navNotif, navProfile, navLogout;
    SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_dashboard);

        spm = SharedPrefManager.getInstance(this);
        tvCEOName        = findViewById(R.id.tvCEOName);
        tvTotalEmployees = findViewById(R.id.tvTotalEmployees);
        tvPresentToday   = findViewById(R.id.tvPresentToday);
        tvTotalManagers  = findViewById(R.id.tvTotalManagers);
        tvAvgKPI         = findViewById(R.id.tvAvgKPI);

        if (tvCEOName != null)
            tvCEOName.setText(spm.getFullName() != null
                    ? spm.getFullName() : "CEO");

        qaPerformance        = findViewById(R.id.qaPerformance);
        qaAttendance         = findViewById(R.id.qaAttendance);
        qaFeedback           = findViewById(R.id.qaFeedback);
        qaSales              = findViewById(R.id.qaSales);
        qaNotifications      = findViewById(R.id.qaNotifications);
        // Overall Performance
        qaOverallPerformance = findViewById(R.id.qaReports);

        navHome    = findViewById(R.id.navHome);
        navNotif   = findViewById(R.id.navNotif);
        navProfile = findViewById(R.id.navProfile);
        navLogout  = findViewById(R.id.navLogout);

        if (qaPerformance != null)
            qaPerformance.setOnClickListener(v ->
                    go(CEOPerformanceActivity.class));
        if (qaAttendance != null)
            qaAttendance.setOnClickListener(v ->
                    go(CEOAttendanceActivity.class));
        if (qaFeedback != null)
            qaFeedback.setOnClickListener(v ->
                    go(CEOFeedbackActivity.class));
        if (qaSales != null)
            qaSales.setOnClickListener(v ->
                    go(CEOSalesReportActivity.class));
        // Send Notification
        if (qaNotifications != null)
            qaNotifications.setOnClickListener(v ->
                    go(CEONotificationsActivity.class));
        // View Overall Performance
        if (qaOverallPerformance != null)
            qaOverallPerformance.setOnClickListener(v ->
                    go(CEOPerformanceActivity.class));

        if (navNotif != null)
            navNotif.setOnClickListener(v ->
                    go(CEONotificationsActivity.class));
        if (navProfile != null)
            navProfile.setOnClickListener(v ->
                    go(CEOProfileActivity.class));
        if (navLogout != null)
            navLogout.setOnClickListener(v -> logout());

        loadStats();
    }

    @Override
    protected void onResume() { super.onResume(); loadStats(); }

    private void go(Class<?> c) {
        startActivity(new Intent(this, c));
    }

    private void loadStats() {
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_EMPLOYEES).get()
                .addOnSuccessListener(s -> {
                    if (tvTotalEmployees != null)
                        tvTotalEmployees.setText(
                                String.valueOf(s.size())); });
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_MANAGERS).get()
                .addOnSuccessListener(s -> {
                    if (tvTotalManagers != null)
                        tvTotalManagers.setText(
                                String.valueOf(s.size())); });
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("date",
                        DateTimeUtils.getCurrentDate())
                .whereEqualTo("status", "Present").get()
                .addOnSuccessListener(s -> {
                    if (tvPresentToday != null)
                        tvPresentToday.setText(
                                String.valueOf(s.size())); });
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_PERFORMANCE).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        if (tvAvgKPI != null)
                            tvAvgKPI.setText("N/A");
                        return;
                    }
                    double total = 0; int count = 0;
                    for (com.google.firebase.firestore
                            .QueryDocumentSnapshot d : snap) {
                        Double kpi = d.getDouble("kpiScore");
                        if (kpi != null) {
                            total += kpi; count++;
                        }
                    }
                    double avg = count > 0
                            ? total / count : 0;
                    if (tvAvgKPI != null)
                        tvAvgKPI.setText(
                                String.format("%.0f%%", avg));
                });
    }

    private void logout() {
        FirebaseHelper.getAuth().signOut();
        spm.clear();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
