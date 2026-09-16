package com.workverse.app.activities.employee;

import android.app.DatePickerDialog;
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
import com.workverse.app.models.Employee;
import com.workverse.app.models.LeaveRequest;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

import java.util.Calendar;
import java.util.Locale;

public class ApplyLeaveActivity extends AppCompatActivity {

    TextInputEditText etLeaveType, etFromDate, etToDate, etReason;
    Button btnSubmit;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_apply_leave);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        etLeaveType = findViewById(R.id.etLeaveType);
        etFromDate  = findViewById(R.id.etFromDate);
        etToDate    = findViewById(R.id.etToDate);
        etReason    = findViewById(R.id.etReason);
        btnSubmit   = findViewById(R.id.btnSubmit);
        pb          = findViewById(R.id.progressBar);

        etFromDate.setOnClickListener(v -> showDatePicker(etFromDate));
        etToDate.setOnClickListener(v -> showDatePicker(etToDate));

        btnSubmit.setOnClickListener(v -> submitLeave());
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formatted = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    target.setText(formatted);
                }, year, month, day);

        dialog.show();
    }

    private void submitLeave() {
        String type = etLeaveType.getText().toString().trim();
        String from = etFromDate.getText().toString().trim();
        String to = etToDate.getText().toString().trim();
        String reason = etReason.getText().toString().trim();

        if (TextUtils.isEmpty(type) || TextUtils.isEmpty(from) || TextUtils.isEmpty(to) || TextUtils.isEmpty(reason)) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        pb.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        SharedPrefManager spm = SharedPrefManager.getInstance(this);
        String uid = spm.getUid();

        // Fetch employee's designation & campaign before submitting
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES)
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    String designation = "";
                    String campaign = "";
                    if (!snap.isEmpty()) {
                        Employee emp = snap.getDocuments().get(0).toObject(Employee.class);
                        if (emp != null) {
                            designation = emp.getDesignation() != null ? emp.getDesignation() : "";
                            campaign = emp.getCampaign() != null ? emp.getCampaign() : "";
                        }
                    }

                    LeaveRequest lr = new LeaveRequest(uid, spm.getFullName() != null ? spm.getFullName() : "Employee",
                            type, from, to, reason, "Employee");
                    lr.setDesignation(designation);
                    lr.setCampaign(campaign);

                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_LEAVES).add(lr)
                            .addOnSuccessListener(r -> {
                                pb.setVisibility(View.GONE);
                                Toast.makeText(this, "Leave request submitted!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                pb.setVisibility(View.GONE);
                                btnSubmit.setEnabled(true);
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed to fetch employee info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}