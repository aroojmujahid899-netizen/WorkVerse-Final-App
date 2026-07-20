package com.workverse.app.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.workverse.app.R;
import com.workverse.app.activities.LoginActivity;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView tvAdminName, tvTotalEmployees, tvPresentToday,
            tvPendingLeaves, tvTotalManagers;
    LinearLayout qaRoles, qaEmployees, qaAddMgr, qaAttendance,
            qaLeave, qaPerformance, qaFeedback, qaSales;
    LinearLayout navHome, navNotif, navProfile, navLogout;
    FirebaseFirestore db;
    SharedPrefManager spm;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_dashboard);

        spm = SharedPrefManager.getInstance(this);
        db  = FirebaseHelper.getDb();

        tvAdminName      = findViewById(R.id.tvAdminName);
        tvTotalEmployees = findViewById(R.id.tvTotalEmployees);
        tvPresentToday   = findViewById(R.id.tvPresentToday);
        tvPendingLeaves  = findViewById(R.id.tvPendingLeaves);
        tvTotalManagers  = findViewById(R.id.tvTotalManagers);

        if (tvAdminName != null)
            tvAdminName.setText(spm.getFullName() != null
                    ? spm.getFullName() : "Admin");

        // MERGED: qaEmployees = Add + View Employee
        qaRoles       = findViewById(R.id.qaRoles);
        qaEmployees   = findViewById(R.id.qaViewEmp);
        qaAddMgr      = findViewById(R.id.qaAddMgr);
        qaAttendance  = findViewById(R.id.qaAttendance);
        qaLeave       = findViewById(R.id.qaLeave);
        qaPerformance = findViewById(R.id.qaPerformance);
        qaFeedback    = findViewById(R.id.qaFeedback);
        qaSales       = findViewById(R.id.qaSales);

        navHome    = findViewById(R.id.navHome);
        navNotif   = findViewById(R.id.navNotif);
        navProfile = findViewById(R.id.navProfile);
        navLogout  = findViewById(R.id.navLogout);

        if (qaRoles      != null) qaRoles.setOnClickListener(v ->
                go(ManageRolesActivity.class));
        if (qaEmployees  != null) qaEmployees.setOnClickListener(v ->
                go(ViewEmployeesActivity.class));
        if (qaAddMgr     != null) qaAddMgr.setOnClickListener(v ->
                go(AddManagerActivity.class));
        if (qaAttendance != null) qaAttendance.setOnClickListener(v ->
                go(AdminAttendanceActivity.class));
        if (qaLeave      != null) qaLeave.setOnClickListener(v ->
                go(AdminLeaveManagementActivity.class));
        if (qaPerformance != null) qaPerformance.setOnClickListener(v ->
                go(AdminPerformanceActivity.class));
        if (qaFeedback   != null) qaFeedback.setOnClickListener(v ->
                go(AdminFeedbackActivity.class));
        if (qaSales      != null) qaSales.setOnClickListener(v ->
                go(AdminSalesReportActivity.class));

        if (navNotif   != null) navNotif.setOnClickListener(v ->
                go(AdminNotificationsActivity.class));
        if (navProfile != null) navProfile.setOnClickListener(v ->
                go(AdminProfileActivity.class));
        if (navLogout  != null) navLogout.setOnClickListener(v ->
                logout());

        loadStats();
    }

    @Override
    protected void onResume() { super.onResume(); loadStats(); }

    private void loadStats() {
        db.collection(FirebaseHelper.COL_EMPLOYEES).get()
                .addOnSuccessListener(s -> {
                    if (tvTotalEmployees != null)
                        tvTotalEmployees.setText(String.valueOf(s.size()));
                });
        db.collection(FirebaseHelper.COL_MANAGERS).get()
                .addOnSuccessListener(s -> {
                    if (tvTotalManagers != null)
                        tvTotalManagers.setText(String.valueOf(s.size()));
                });
        db.collection(FirebaseHelper.COL_LEAVES)
                .whereEqualTo("status", "Pending").get()
                .addOnSuccessListener(s -> {
                    if (tvPendingLeaves != null)
                        tvPendingLeaves.setText(String.valueOf(s.size()));
                });
        db.collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("date",
                        com.workverse.app.utils.DateTimeUtils.getCurrentDate())
                .whereEqualTo("status", "Present").get()
                .addOnSuccessListener(s -> {
                    if (tvPresentToday != null)
                        tvPresentToday.setText(String.valueOf(s.size()));
                });
    }

    private void go(Class<?> c) {
        startActivity(new Intent(this, c));
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