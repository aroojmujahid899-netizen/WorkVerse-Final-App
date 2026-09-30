package com.workverse.app.activities.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
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
import com.workverse.app.adapters.SalesReportAdapter;
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
    private SalesReportAdapter adapter;

    private List<SalesReport> fullList = new ArrayList<>();
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
        adapter = new SalesReportAdapter(new ArrayList<>(), report -> showEmployeeDetailDialog(report));
        rvSales.setAdapter(adapter);

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showAddSalesDialog());
        }

        loadEmployees();
        loadSalesData();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    List<SalesReport> list = new ArrayList<>();
                    double totalTarget = 0, totalAchieved = 0;

                    for (var d : snap) {
                        SalesReport r = d.toObject(SalesReport.class);
                        r.setId(d.getId());
                        list.add(r);
                        totalTarget += r.getTargetAmount();
                        totalAchieved += r.getAchievedAmount();
                    }

                    fullList = list;
                    tvStat1.setText(String.valueOf((long) totalTarget));
                    tvStat2.setText(String.valueOf((long) totalAchieved));

                    setupBarChart(totalTarget, totalAchieved);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load sales data", Toast.LENGTH_SHORT).show());
    }

    private void setupBarChart(double target, double achieved) {
        if (barChartSales == null) return;

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1f, (float) target));
        entries.add(new BarEntry(2f, (float) achieved));

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

    private void showEmployeeDetailDialog(SalesReport clicked) {
        String empName = clicked.getEmployeeName();

        List<SalesReport> empRecords = new ArrayList<>();
        double empTotalTarget = 0, empTotalAchieved = 0;
        for (SalesReport r : fullList) {
            if (empName != null && empName.equals(r.getEmployeeName())) {
                empRecords.add(r);
                empTotalTarget += r.getTargetAmount();
                empTotalAchieved += r.getAchievedAmount();
            }
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(empName + " - Performance");
        tvTitle.setTextSize(16f);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setPadding(0, 0, 0, 16);
        container.addView(tvTitle);

        BarChart empChart = new BarChart(this);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 500);
        empChart.setLayoutParams(chartParams);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1f, (float) empTotalTarget));
        entries.add(new BarEntry(2f, (float) empTotalAchieved));

        BarDataSet dataSet = new BarDataSet(entries, empName + ": Target vs Achieved");
        dataSet.setColors(new int[]{Color.parseColor("#1976D2"), Color.parseColor("#388E3C")});
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        empChart.setData(barData);
        empChart.getDescription().setEnabled(false);
        empChart.animateY(800);

        container.addView(empChart);

        TextView tvHistoryLabel = new TextView(this);
        tvHistoryLabel.setText("Records:");
        tvHistoryLabel.setTextSize(14f);
        tvHistoryLabel.setTextColor(Color.BLACK);
        tvHistoryLabel.setPadding(0, 24, 0, 8);
        container.addView(tvHistoryLabel);

        for (SalesReport r : empRecords) {
            TextView tvLine = new TextView(this);
            tvLine.setText(r.getDate() + "  →  Achieved: " + r.getAchievedAmount()
                    + " / Target: " + r.getTargetAmount());
            tvLine.setTextSize(13f);
            tvLine.setPadding(0, 4, 0, 4);
            container.addView(tvLine);
        }

        new AlertDialog.Builder(this)
                .setView(container)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showAddSalesDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_sale, null);

        AutoCompleteTextView actEmployee = dialogView.findViewById(R.id.actEmployee);
        AutoCompleteTextView actDesignation = dialogView.findViewById(R.id.actDesignation);
        AutoCompleteTextView actCampaign = dialogView.findViewById(R.id.actCampaign);
        TextInputEditText etTarget = dialogView.findViewById(R.id.etTarget);
        TextInputEditText etAchieved = dialogView.findViewById(R.id.etAchieved);

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

        String[] designations = new String[]{"Fronters", "Verifiers", "Closers"};
        actDesignation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, designations));

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

                    if (selectedName.isEmpty() || designation.isEmpty() || campaign.isEmpty()
                            || targetStr.isEmpty() || achievedStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

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

                    int target, achieved;
                    try {
                        target = Integer.parseInt(targetStr);
                        achieved = Integer.parseInt(achievedStr);
                    } catch (NumberFormatException ex) {
                        Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SalesReport record = new SalesReport();
                    record.setUserId(matched.getUserId());
                    record.setEmployeeName(matched.getName());
                    record.setDesignation(designation);
                    record.setCampaign(campaign);
                    record.setTargetAmount(target);
                    record.setAchievedAmount(achieved);
                    record.setTimestamp(System.currentTimeMillis());

                    FirebaseHelper.getDb().collection("sales_reports")
                            .add(record)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Sales Reports Added!", Toast.LENGTH_SHORT).show();
                                loadSalesData();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("CANCEL", null)
                .create()
                .show();
    }
}