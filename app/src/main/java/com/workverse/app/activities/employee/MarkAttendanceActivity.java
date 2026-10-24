package com.workverse.app.activities.employee;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.workverse.app.R;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

public class MarkAttendanceActivity extends AppCompatActivity {

    private static final String ERR_ALREADY_CHECKED_IN = "ALREADY_CHECKED_IN";

    TextView tvDate, tvTime, tvStatus;
    Button btnCheckIn, btnCheckOut;
    ProgressBar pb;
    Handler handler = new Handler();
    boolean checkedIn = false;
    boolean alreadyCheckedOutToday = false;
    String attendanceId = null;
    Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_mark_attendance);

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

        // Disable check-in until we know whether today's record already exists
        btnCheckIn.setEnabled(false);
        checkExistingAttendance();

        btnCheckIn.setOnClickListener(v -> markCheckIn());
        btnCheckOut.setOnClickListener(v -> markCheckOut());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockRunnable);
    }

    // Unique document ID per user per day, so duplicates are impossible
    private String buildAttendanceDocId(String uid) {
        return uid + "_" + DateTimeUtils.getCurrentDate();
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
                            alreadyCheckedOutToday = true;
                            tvStatus.setText("Checked Out: " + co);
                        } else {
                            checkedIn = true;
                            tvStatus.setText("Checked In \u2713");
                        }
                    }
                    btnCheckIn.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    // The transaction in markCheckIn() still prevents duplicates
                    btnCheckIn.setEnabled(true);
                    Toast.makeText(this, "Could not load today's attendance", Toast.LENGTH_SHORT).show();
                });
    }

    private void markCheckIn() {
        if (checkedIn || attendanceId != null) {
            Toast.makeText(this, "You can only check in once per day", Toast.LENGTH_SHORT).show();
            return;
        }

        pb.setVisibility(View.VISIBLE);
        btnCheckIn.setEnabled(false);

        String uid = SharedPrefManager.getInstance(this).getUid();
        String name = SharedPrefManager.getInstance(this).getFullName();

        // Fetch the user's role, designation and campaign from Firestore first
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(uid).get()
                .addOnSuccessListener(userDoc -> {
                    String role = userDoc.getString("role");
                    String designation = userDoc.getString("designation");
                    String campaign = userDoc.getString("campaign");

                    Map<String, Object> att = new HashMap<>();
                    att.put("userId", uid);
                    att.put("employeeName", name != null ? name : "Employee");
                    att.put("date", DateTimeUtils.getCurrentDate());
                    att.put("checkInTime", DateTimeUtils.getCurrentTime());
                    att.put("timestamp", System.currentTimeMillis());
                    att.put("status", "Present");
                    att.put("role", role != null ? role : "Employee");
                    att.put("designation", designation != null ? designation : "");
                    att.put("campaign", campaign != null ? campaign : "");

                    final String docId = buildAttendanceDocId(uid);
                    final DocumentReference ref = FirebaseHelper.getDb()
                            .collection(FirebaseHelper.COL_ATTENDANCE).document(docId);

                    // Transaction: only create the record if it does not exist yet
                    FirebaseHelper.getDb().runTransaction(tx -> {
                                DocumentSnapshot existing = tx.get(ref);
                                if (existing.exists()) {
                                    throw new FirebaseFirestoreException(
                                            ERR_ALREADY_CHECKED_IN,
                                            FirebaseFirestoreException.Code.ABORTED);
                                }
                                tx.set(ref, att);
                                return null;
                            })
                            .addOnSuccessListener(r -> {
                                attendanceId = docId;
                                checkedIn = true;
                                pb.setVisibility(View.GONE);
                                btnCheckIn.setEnabled(true);
                                tvStatus.setText("Checked In \u2713");
                                Toast.makeText(this, "Check-in recorded!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                pb.setVisibility(View.GONE);
                                btnCheckIn.setEnabled(true);
                                if (ERR_ALREADY_CHECKED_IN.equals(e.getMessage())) {
                                    attendanceId = docId;
                                    checkedIn = true;
                                    tvStatus.setText("Checked In \u2713");
                                    Toast.makeText(this, "You can only check in once per day", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnCheckIn.setEnabled(true);
                    Toast.makeText(this, "Could not fetch user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void markCheckOut() {
        if (alreadyCheckedOutToday) {
            Toast.makeText(this, "You've already checked out today", Toast.LENGTH_SHORT).show();
            return;
        }
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
                    btnCheckOut.setEnabled(true);
                    alreadyCheckedOutToday = true;
                    tvStatus.setText("Checked Out \u2713");
                    Toast.makeText(this, "Check-out recorded!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnCheckOut.setEnabled(true);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                });
    }
}