package com.workverse.app.activities.manager;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.workverse.app.R;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

public class ManagerMarkAttendanceActivity extends AppCompatActivity {

    TextView tvDate, tvTime, tvStatus;
    Button btnCheckIn, btnCheckOut;
    ProgressBar pb;
    Handler handler = new Handler();
    String attendanceId = null;
    Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_mark_attendance);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        pb = findViewById(R.id.progressBar);

        tvDate.setText(DateTimeUtils.getCurrentDate());
        clockRunnable = () -> {
            tvTime.setText(DateTimeUtils.getCurrentTime());
            handler.postDelayed(clockRunnable, 1000);
        };
        handler.post(clockRunnable);

        checkExistingAttendance();
        btnCheckIn.setOnClickListener(v -> markCheckIn());
        btnCheckOut.setOnClickListener(v -> markCheckOut());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable);
    }

    private void checkExistingAttendance() {
        String uid = SharedPrefManager.getInstance(this).getUid();
        String today = DateTimeUtils.getCurrentDate();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("userId", uid).whereEqualTo("date", today).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        attendanceId = snap.getDocuments().get(0).getId();
                        String co = snap.getDocuments().get(0).getString("checkOutTime");
                        if (co != null && !co.isEmpty()) {
                            tvStatus.setText("Checked Out: " + co);
                            btnCheckIn.setEnabled(false);
                            btnCheckOut.setEnabled(false);
                        } else {
                            tvStatus.setText("Checked In \u2713");
                            btnCheckIn.setEnabled(false);
                            btnCheckOut.setEnabled(true);
                        }
                    }
                });
    }

    private void markCheckIn() {
        pb.setVisibility(View.VISIBLE);
        btnCheckIn.setEnabled(false);
        SharedPrefManager spm = SharedPrefManager.getInstance(this);
        String uid = spm.getUid();
        String name = spm.getFullName();

        // Firestore se designation/campaign fetch karo (role already known: Manager)
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    String designation = userDoc.getString("designation");
                    String campaign = userDoc.getString("campaign");

                    Map<String, Object> att = new HashMap<>();
                    att.put("userId", uid);
                    att.put("employeeName", name != null ? name : "Manager");
                    att.put("role", "Manager");
                    att.put("designation", designation != null ? designation : "");
                    att.put("campaign", campaign != null ? campaign : "");
                    att.put("date", DateTimeUtils.getCurrentDate());
                    att.put("checkInTime", DateTimeUtils.getCurrentTime());
                    att.put("status", "Present");

                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE).add(att)
                            .addOnSuccessListener(r -> {
                                attendanceId = r.getId();
                                pb.setVisibility(View.GONE);
                                tvStatus.setText("Checked In \u2713");
                                btnCheckOut.setEnabled(true);
                                Toast.makeText(this, "Check-in recorded!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                pb.setVisibility(View.GONE);
                                btnCheckIn.setEnabled(true);
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnCheckIn.setEnabled(true);
                    Toast.makeText(this, "Could not fetch user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void markCheckOut() {
        if (attendanceId == null) {
            Toast.makeText(this, "No check-in found", Toast.LENGTH_SHORT).show();
            return;
        }
        pb.setVisibility(View.VISIBLE);
        btnCheckOut.setEnabled(false);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE).document(attendanceId)
                .update("checkOutTime", DateTimeUtils.getCurrentTime())
                .addOnSuccessListener(r -> {
                    pb.setVisibility(View.GONE);
                    tvStatus.setText("Checked Out \u2713");
                    btnCheckIn.setEnabled(false);
                    Toast.makeText(this, "Check-out recorded!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnCheckOut.setEnabled(true);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                });
    }
}