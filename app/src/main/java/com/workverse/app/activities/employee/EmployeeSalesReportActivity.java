package com.workverse.app.activities.employee;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.SalesReportAdapter;
import com.workverse.app.models.SalesReport;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSalesReportActivity extends AppCompatActivity {

    RecyclerView rv; ProgressBar pb; TextView tvStat1, tvStat2;
    BarChart barChartSales;
    SalesReportAdapter adapter;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_employee_sales_report);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        rv             = findViewById(R.id.recyclerView);
        pb             = findViewById(R.id.progressBar);
        tvStat1        = findViewById(R.id.tvStat1);
        tvStat2        = findViewById(R.id.tvStat2);
        barChartSales  = findViewById(R.id.barChartSales);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalesReportAdapter(new ArrayList<>(), report -> showEntryDetailDialog(report));
        rv.setAdapter(adapter);

        loadData();
    }

    @Override protected void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        if (pb != null) pb.setVisibility(View.VISIBLE);
        String uid = SharedPrefManager.getInstance(this).getUid();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES)
                .whereEqualTo("userId", uid).get()
                .addOnSuccessListener(snap -> {
                    List<SalesReport> list = new ArrayList<>();
                    double tt = 0, ta = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        SalesReport r = d.toObject(SalesReport.class); r.setId(d.getId()); list.add(r);
                        tt += r.getTargetAmount(); ta += r.getAchievedAmount();
                    }
                    if (pb != null) pb.setVisibility(View.GONE);
                    if (tvStat1 != null) tvStat1.setText(String.format("%.0f", tt));
                    if (tvStat2 != null) tvStat2.setText(String.format("%.0f", ta));

                    setupBarChart(tt, ta);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    if (pb != null) pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
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

    /**
     * Shows a mini bar chart dialog for a SINGLE specific sales entry
     * (the one the user tapped on), not the combined total.
     */
    private void showEntryDetailDialog(SalesReport entry) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(entry.getDate());
        tvTitle.setTextSize(16f);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setPadding(0, 0, 0, 16);
        container.addView(tvTitle);

        BarChart entryChart = new BarChart(this);
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 500);
        entryChart.setLayoutParams(chartParams);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1f, (float) entry.getTargetAmount()));
        entries.add(new BarEntry(2f, (float) entry.getAchievedAmount()));

        BarDataSet dataSet = new BarDataSet(entries, "Target vs Achieved");
        dataSet.setColors(new int[]{Color.parseColor("#1976D2"), Color.parseColor("#388E3C")});
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        entryChart.setData(barData);
        entryChart.getDescription().setEnabled(false);
        entryChart.animateY(800);

        container.addView(entryChart);

        TextView tvDetails = new TextView(this);
        tvDetails.setText("Target: " + entry.getTargetAmount() + "\nAchieved: " + entry.getAchievedAmount());
        tvDetails.setTextSize(14f);
        tvDetails.setTextColor(Color.BLACK);
        tvDetails.setPadding(0, 24, 0, 0);
        container.addView(tvDetails);

        new AlertDialog.Builder(this)
                .setView(container)
                .setPositiveButton("Close", null)
                .show();
    }
}