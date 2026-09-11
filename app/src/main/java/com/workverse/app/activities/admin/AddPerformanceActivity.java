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
import com.workverse.app.models.Employee;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPerformanceActivity extends AppCompatActivity {
    Spinner spinnerEmployee;
    AutoCompleteTextView actCampaign;
    TextInputEditText etMonth, etYear,
            etTasksAssigned, etTasksCompleted, etKpiScore, etAttendance, etAiInsight;
    Button btnSubmit, btnGenerateAi; ProgressBar pb;
    List<Employee> employeeList = new ArrayList<>();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_add_performance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        spinnerEmployee  = findViewById(R.id.spinnerEmployee);
        actCampaign      = findViewById(R.id.actCampaign);
        etMonth          = findViewById(R.id.etMonth);
        etYear           = findViewById(R.id.etYear);
        etTasksAssigned  = findViewById(R.id.etTasksAssigned);
        etTasksCompleted = findViewById(R.id.etTasksCompleted);
        etKpiScore       = findViewById(R.id.etKpiScore);
        etAttendance     = findViewById(R.id.etAttendance);
        etAiInsight      = findViewById(R.id.etAiInsight);
        btnSubmit        = findViewById(R.id.btnSave);
        btnGenerateAi    = findViewById(R.id.btnGenerateAi);
        pb               = findViewById(R.id.progressBar);
        btnSubmit.setOnClickListener(v -> savePerformance());
        btnGenerateAi.setOnClickListener(v -> calculateKpiWithAi());
        setupCampaignSpinner();
        loadEmployees();
    }

    private void setupCampaignSpinner() {
        String[] campaigns = {"MEDICARE", "FE (Final Expense)", "Home Warranty"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, campaigns);
        actCampaign.setAdapter(adapter);
        actCampaign.setOnClickListener(v -> actCampaign.showDropDown());
        actCampaign.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actCampaign.showDropDown();
        });
    }

    private void loadEmployees() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).get()
                .addOnSuccessListener(snap -> {
                    employeeList.clear();
                    List<String> names = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        Employee e = d.toObject(Employee.class);
                        e.setId(d.getId());
                        employeeList.add(e);
                        names.add(e.getName());
                    }
                    if (employeeList.isEmpty()) {
                        names.add("No employees found");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, names);
                    spinnerEmployee.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load employees", Toast.LENGTH_SHORT).show());
    }

    private void calculateKpiWithAi() {
        String tasksA = etTasksAssigned.getText().toString().trim();
        String tasksC = etTasksCompleted.getText().toString().trim();
        String att = etAttendance.getText().toString().trim();

        if (TextUtils.isEmpty(tasksA) || TextUtils.isEmpty(tasksC)) {
            Toast.makeText(this, "Please enter Tasks Assigned and Completed first", Toast.LENGTH_SHORT).show();
            return;
        }

        int assigned, completed;
        double attendancePct;
        try {
            assigned = Integer.parseInt(tasksA);
            completed = Integer.parseInt(tasksC);
            attendancePct = TextUtils.isEmpty(att) ? 0 : Double.parseDouble(att);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        pb.setVisibility(View.VISIBLE);
        btnGenerateAi.setEnabled(false);

        String employeeName = (spinnerEmployee.getSelectedItem() != null) ? spinnerEmployee.getSelectedItem().toString() : "Employee";

        com.workverse.app.utils.GeminiFeedbackService.calculateKpiAndInsight(
                employeeName, assigned, completed, attendancePct,
                new com.workverse.app.utils.GeminiFeedbackService.Callback() {
                    @Override
                    public void onSuccess(com.workverse.app.utils.GeminiFeedbackService.Result result) {
                        runOnUiThread(() -> {
                            pb.setVisibility(View.GONE);
                            btnGenerateAi.setEnabled(true);
                            etKpiScore.setText(String.valueOf(result.score));
                            etAiInsight.setText(result.summary);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            pb.setVisibility(View.GONE);
                            btnGenerateAi.setEnabled(true);
                            Toast.makeText(AddPerformanceActivity.this, "AI Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void savePerformance() {
        int pos = spinnerEmployee.getSelectedItemPosition();
        if (employeeList.isEmpty() || pos < 0 || pos >= employeeList.size()) {
            Toast.makeText(this, "Please select an employee", Toast.LENGTH_SHORT).show();
            return;
        }
        Employee selected = employeeList.get(pos);
        String name = selected.getName();
        String uid  = selected.getUserId();
        String campaign = actCampaign.getText().toString().trim();

        String month   = etMonth.getText().toString().trim();
        String year    = etYear.getText().toString().trim();
        String tasksA  = etTasksAssigned.getText().toString().trim();
        String tasksC  = etTasksCompleted.getText().toString().trim();
        String kpi     = etKpiScore.getText().toString().trim();
        String att     = etAttendance.getText().toString().trim();

        if (TextUtils.isEmpty(uid)) {
            Toast.makeText(this, "This employee has no linked account yet", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(month) || TextUtils.isEmpty(year) || TextUtils.isEmpty(tasksA)
                || TextUtils.isEmpty(tasksC)) {
            Toast.makeText(this, "All fields required except Attendance", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(kpi)) {
            Toast.makeText(this, "Please tap 'Calculate KPI with AI' first", Toast.LENGTH_SHORT).show();
            return;
        }

        double kpiScore, attendancePct;
        int assigned, completed;
        try {
            kpiScore      = Double.parseDouble(kpi);
            attendancePct = TextUtils.isEmpty(att) ? 0 : Double.parseDouble(att);
            assigned      = Integer.parseInt(tasksA);
            completed     = Integer.parseInt(tasksC);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }
        pb.setVisibility(View.VISIBLE); btnSubmit.setEnabled(false);
        Map<String, Object> data = new HashMap<>();
        data.put("userId",               uid);
        data.put("employeeName",         name);
        data.put("campaign",             campaign);
        data.put("month",                month);
        data.put("year",                 year);
        data.put("tasksAssigned",        assigned);
        data.put("tasksCompleted",       completed);
        data.put("kpiScore",             kpiScore);
        data.put("attendancePercentage", attendancePct);
        data.put("performanceInsight",   etAiInsight.getText().toString().trim());
        data.put("timestamp",            System.currentTimeMillis());
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE).add(data)
                .addOnSuccessListener(r -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Performance record saved!", Toast.LENGTH_SHORT).show();
                    com.workverse.app.utils.KPISyncHelper.recomputeForUser(uid);
                    finish();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE); btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}