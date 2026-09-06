package com.workverse.app.activities.admin;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
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
    TextInputEditText etMonth, etYear,
            etTasksAssigned, etTasksCompleted, etKpiScore, etAttendance;
    Button btnSubmit; ProgressBar pb;
    List<Employee> employeeList = new ArrayList<>();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_add_performance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        spinnerEmployee  = findViewById(R.id.spinnerEmployee);
        etMonth          = findViewById(R.id.etMonth);
        etYear           = findViewById(R.id.etYear);
        etTasksAssigned  = findViewById(R.id.etTasksAssigned);
        etTasksCompleted = findViewById(R.id.etTasksCompleted);
        etKpiScore       = findViewById(R.id.etKpiScore);
        etAttendance     = findViewById(R.id.etAttendance);
        btnSubmit        = findViewById(R.id.btnSubmit);
        pb               = findViewById(R.id.progressBar);
        btnSubmit.setOnClickListener(v -> savePerformance());
        loadEmployees();
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

    private void savePerformance() {
        int pos = spinnerEmployee.getSelectedItemPosition();
        if (employeeList.isEmpty() || pos < 0 || pos >= employeeList.size()) {
            Toast.makeText(this, "Please select an employee", Toast.LENGTH_SHORT).show();
            return;
        }
        Employee selected = employeeList.get(pos);
        String name = selected.getName();
        String uid  = selected.getUserId(); // real Firebase Auth UID — no manual typing needed

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
                || TextUtils.isEmpty(tasksC) || TextUtils.isEmpty(kpi)) {
            Toast.makeText(this, "All fields required except Attendance", Toast.LENGTH_SHORT).show();
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
        data.put("month",                month);
        data.put("year",                 year);
        data.put("tasksAssigned",        assigned);
        data.put("tasksCompleted",       completed);
        data.put("kpiScore",             kpiScore);
        data.put("attendancePercentage", attendancePct);
        data.put("timestamp",            System.currentTimeMillis());
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE).add(data)
                .addOnSuccessListener(r -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Performance record saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE); btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
