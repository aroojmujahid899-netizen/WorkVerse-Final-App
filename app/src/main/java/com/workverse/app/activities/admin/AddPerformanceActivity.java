package com.workverse.app.activities.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.models.PerformanceReport;
import com.workverse.app.models.User;
import com.workverse.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AddPerformanceActivity extends AppCompatActivity {

    Spinner spEmployee;
    AutoCompleteTextView actCampaign;
    TextInputEditText etMonth, etYear, etTasksAssigned, etTasksCompleted, etKpiScore, etAttendance;
    Button btnSave;
    ProgressBar progressBar;

    List<User> employeeList = new ArrayList<>();
    List<String> employeeNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_performance);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Performance Record");
        tb.setNavigationOnClickListener(v -> finish());

        spEmployee = findViewById(R.id.spEmployee);
        actCampaign = findViewById(R.id.actCampaign);
        etMonth = findViewById(R.id.etMonth);
        etYear = findViewById(R.id.etYear);
        etTasksAssigned = findViewById(R.id.etTasksAssigned);
        etTasksCompleted = findViewById(R.id.etTasksCompleted);
        etKpiScore = findViewById(R.id.etKpiScore);
        etAttendance = findViewById(R.id.etAttendance);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        setupCampaignDropdown();
        loadEmployees();

        btnSave.setOnClickListener(v -> saveRecord());
    }

    private void setupCampaignDropdown() {
        String[] campaigns = new String[]{"MEDICARE", "FE (Final Expense)", "Home Warranty"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campaigns);
        actCampaign.setAdapter(adapter);
    }

    private void loadEmployees() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS)
                .whereEqualTo("role", "Employee")
                .get()
                .addOnSuccessListener(snap -> {
                    employeeList.clear();
                    employeeNames.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        User u = d.toObject(User.class);
                        u.setUid(d.getId());
                        employeeList.add(u);

                        String empName = d.getString("name");
                        if (empName == null) empName = d.getString("fullName");
                        if (empName == null) empName = "Unknown";

                        employeeNames.add(empName);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, employeeNames);
                    spEmployee.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load employees", Toast.LENGTH_SHORT).show());
    }

    private void saveRecord() {
        if (employeeList.isEmpty() || spEmployee.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Please select an employee", Toast.LENGTH_SHORT).show();
            return;
        }

        String campaign = actCampaign.getText().toString().trim();
        String month = etMonth.getText().toString().trim();
        String year = etYear.getText().toString().trim();
        String tasksAssignedStr = etTasksAssigned.getText().toString().trim();
        String tasksCompletedStr = etTasksCompleted.getText().toString().trim();
        String kpiStr = etKpiScore.getText().toString().trim();
        String attendanceStr = etAttendance.getText().toString().trim();

        if (TextUtils.isEmpty(campaign)) {
            actCampaign.setError("Select campaign");
            return;
        }
        if (TextUtils.isEmpty(month) || TextUtils.isEmpty(year) ||
                TextUtils.isEmpty(tasksAssignedStr) || TextUtils.isEmpty(tasksCompletedStr) ||
                TextUtils.isEmpty(kpiStr) || TextUtils.isEmpty(attendanceStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        User selectedEmployee = employeeList.get(spEmployee.getSelectedItemPosition());
        String selectedName = employeeNames.get(spEmployee.getSelectedItemPosition());

        PerformanceReport report = new PerformanceReport();
        report.setUserId(selectedEmployee.getUid()); // Corrected to setUserId
        report.setEmployeeName(selectedName);
        report.setCampaign(campaign);
        report.setMonth(month);
        report.setYear(year);
        report.setTasksAssigned(Integer.parseInt(tasksAssignedStr));
        report.setTasksCompleted(Integer.parseInt(tasksCompletedStr));
        report.setKpiScore(Double.parseDouble(kpiStr));
        report.setAttendancePercentage(Double.parseDouble(attendanceStr));
        report.setTimestamp(System.currentTimeMillis());

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE)
                .add(report)
                .addOnSuccessListener(ref -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Performance Record Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error saving record", Toast.LENGTH_SHORT).show();
                });
    }
}