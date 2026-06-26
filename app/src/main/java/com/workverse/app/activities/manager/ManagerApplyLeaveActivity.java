package com.workverse.app.activities.manager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.workverse.app.R;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.HashMap;
import java.util.Map;

public class ManagerApplyLeaveActivity extends AppCompatActivity {

    private TextInputEditText etLeaveType, etFromDate, etToDate, etReason;
    private Button    btnSubmit;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_apply_leave);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        etLeaveType = findViewById(R.id.etLeaveType);
        etFromDate  = findViewById(R.id.etFromDate);
        etToDate    = findViewById(R.id.etToDate);
        etReason    = findViewById(R.id.etReason);
        btnSubmit   = findViewById(R.id.btnSubmit);
        pb          = findViewById(R.id.progressBar);

        btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String leaveType = etLeaveType.getText().toString().trim();
        String fromDate  = etFromDate.getText().toString().trim();
        String toDate    = etToDate.getText().toString().trim();
        String reason    = etReason.getText().toString().trim();

        if (TextUtils.isEmpty(leaveType)) {
            etLeaveType.setError("Required"); etLeaveType.requestFocus(); return;
        }
        if (TextUtils.isEmpty(fromDate)) {
            etFromDate.setError("Required"); etFromDate.requestFocus(); return;
        }
        if (TextUtils.isEmpty(toDate)) {
            etToDate.setError("Required"); etToDate.requestFocus(); return;
        }
        if (TextUtils.isEmpty(reason)) {
            etReason.setError("Required"); etReason.requestFocus(); return;
        }

        pb.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        SharedPrefManager spm = SharedPrefManager.getInstance(this);

        // Build the document — same collection as employee leaves
        // role = "Manager" so Admin can see it separately
        Map<String, Object> data = new HashMap<>();
        data.put("userId",       spm.getUid());
        data.put("employeeName", spm.getFullName() != null ? spm.getFullName() : "Manager");
        data.put("role",         "Manager");
        data.put("leaveType",    leaveType);
        data.put("fromDate",     fromDate);
        data.put("toDate",       toDate);
        data.put("reason",       reason);
        data.put("status",       "Pending");
        data.put("timestamp",    System.currentTimeMillis());

        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .add(data)
                .addOnSuccessListener(ref -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Leave request submitted! Admin will review it.",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}