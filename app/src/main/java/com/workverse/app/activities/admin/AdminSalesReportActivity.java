package com.workverse.app.activities.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.models.Employee;
import com.workverse.app.models.SalesReport;
import com.workverse.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminSalesReportActivity extends AppCompatActivity {

    private TextView tvStat1, tvStat2;
    private BarChart barChartSales;
    private RecyclerView rvSales;
    private FloatingActionButton fabAdd;

    private List<SalesReport> salesList = new ArrayList<>();
    private List<Employee> employeeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sales_report);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Sales Reports");
        tb.setNavigationOnClickListener(v -> finish());

        tvStat1 = findViewById(R.id.tvStat1);
        tvStat2 = findViewById(R.id.tvStat2);
        barChartSales = findViewById(R.id.barChartSales);
        rvSales = findViewById(R.id.rvSales);
        fabAdd = findViewById(R.id.fabAdd);

        rvSales.setLayoutManager(new LinearLayoutManager(this));

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddSalesDialog());
        }

        loadEmployees();
        loadSalesData();
    }

    private void loadEmployees() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).get()
                .addOnSuccessListener(snap -> {
                    employeeList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Employee e = d.toObject(Employee.class);
                        e.setId(d.getId());
                        employeeList.add(e);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load employees", Toast.LENGTH_SHORT).show());
    }

    private void loadSalesData() {
        FirebaseHelper.getDb().collection("sales_reports")
                .get()
                .addOnSuccessListener(snap -> {
                    salesList.clear();
                    int totalTarget = 0;
                    int totalAchieved = 0;

                    for (var d : snap) {
                        SalesReport r = d.toObject(SalesReport.class);
                        salesList.add(r);
                        totalTarget += r.getTargetAmount();
                        totalAchieved += r.getAchievedAmount();
                    }

                    tvStat1.setText(String.valueOf(totalTarget));
                    tvStat2.setText(String.valueOf(totalAchieved));

                    setupBarChart(totalTarget, totalAchieved);
                });
    }

    private void setupBarChart(int target, int achieved) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1f, target));
        entries.add(new BarEntry(2f, achieved));

        BarDataSet dataSet = new BarDataSet(entries, "Target vs Achieved");
        dataSet.setColors(new int[]{Color.parseColor("#1976D2"), Color.parseColor("#388E3C")});
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChartSales.setData(barData);
        barChartSales.getDescription().setEnabled(false);
        barChartSales.animateY(1000);
        barChartSales.invalidate();
    }

    private void showAddSalesDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_sale, null);

        AutoCompleteTextView actEmployee = dialogView.findViewById(R.id.actEmployee);
        AutoCompleteTextView actDesignation = dialogView.findViewById(R.id.actDesignation);
        AutoCompleteTextView actCampaign = dialogView.findViewById(R.id.actCampaign);
        TextInputEditText etTarget = dialogView.findViewById(R.id.etTarget);
        TextInputEditText etAchieved = dialogView.findViewById(R.id.etAchieved);

        // Setup Employee Dropdown
        List<String> employeeNames = new ArrayList<>();
        for (Employee e : employeeList) {
            employeeNames.add(e.getName());
        }
        if (employeeNames.isEmpty()) {
            employeeNames.add("No employees found");
        }
        actEmployee.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, employeeNames));
        actEmployee.setOnClickListener(v -> actEmployee.showDropDown());
        actEmployee.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actEmployee.showDropDown();
        });

        // Setup Designation Dropdown
        String[] designations = new String[]{"Fronters", "Verifiers", "Closers"};
        actDesignation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, designations));

        // Setup Campaign Dropdown
        String[] campaigns = new String[]{"MEDICARE", "FE", "Home Warranty"};
        actCampaign.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campaigns));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("SAVE", (dialog, which) -> {
                    String selectedName = actEmployee.getText().toString().trim();
                    String designation = actDesignation.getText().toString().trim();
                    String campaign = actCampaign.getText().toString().trim();
                    String targetStr = etTarget.getText().toString().trim();
                    String achievedStr = etAchieved.getText().toString().trim();

                    if (selectedName.isEmpty() || designation.isEmpty() || campaign.isEmpty() || targetStr.isEmpty() || achievedStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Find matching employee to get userId
                    Employee matched = null;
                    for (Employee e : employeeList) {
                        if (e.getName() != null && e.getName().equals(selectedName)) {
                            matched = e;
                            break;
                        }
                    }

                    if (matched == null) {
                        Toast.makeText(this, "Please select a valid employee from the list", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(matched.getUserId())) {
                        Toast.makeText(this, "This employee has no linked account yet", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SalesReport record = new SalesReport();
                    record.setUserId(matched.getUserId());
                    record.setEmployeeName(matched.getName());
                    record.setDesignation(designation);
                    record.setCampaign(campaign);
                    record.setTargetAmount(Integer.parseInt(targetStr));
                    record.setAchievedAmount(Integer.parseInt(achievedStr));
                    record.setTimestamp(System.currentTimeMillis());

                    FirebaseHelper.getDb().collection("sales_reports")
                            .add(record)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Sales Reports Added!", Toast.LENGTH_SHORT).show();
                                loadSalesData();
                            });
                })
                .setNegativeButton("CANCEL", null)
                .create()
                .show();
    }
}