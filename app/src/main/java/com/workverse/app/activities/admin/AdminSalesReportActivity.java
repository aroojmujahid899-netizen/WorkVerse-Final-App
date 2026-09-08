package com.workverse.app.activities.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
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
import com.workverse.app.R;
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

        loadSalesData();
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

                    // Display counts without "PKR"
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

        TextInputEditText etName = dialogView.findViewById(R.id.etEmployeeName);
        AutoCompleteTextView actDesignation = dialogView.findViewById(R.id.actDesignation);
        AutoCompleteTextView actCampaign = dialogView.findViewById(R.id.actCampaign);
        TextInputEditText etTarget = dialogView.findViewById(R.id.etTarget);
        TextInputEditText etAchieved = dialogView.findViewById(R.id.etAchieved);

        // Setup Designation Dropdown
        String[] designations = new String[]{"Fronters", "Verifiers", "Closers"};
        actDesignation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, designations));

        // Setup Campaign Dropdown
        String[] campaigns = new String[]{"MEDICARE", "FE", "Home Warranty"};
        actCampaign.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campaigns));

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("SAVE", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String designation = actDesignation.getText().toString().trim();
                    String campaign = actCampaign.getText().toString().trim();
                    String targetStr = etTarget.getText().toString().trim();
                    String achievedStr = etAchieved.getText().toString().trim();

                    if (name.isEmpty() || designation.isEmpty() || campaign.isEmpty() || targetStr.isEmpty() || achievedStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SalesReport record = new SalesReport();
                    record.setEmployeeName(name);
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