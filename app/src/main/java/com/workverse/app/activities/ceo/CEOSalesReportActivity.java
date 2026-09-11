package com.workverse.app.activities.ceo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.SalesReportAdapter;
import com.workverse.app.models.SalesReport;
import com.workverse.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CEOSalesReportActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    TextView tvTotalSales, tvTotalAmount, tvTargetAmount, tvEmpty;
    BarChart barChartSales;
    SalesReportAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_sales_report);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Company Sales Analytics");
        tb.setNavigationOnClickListener(v -> finish());

        rv             = findViewById(R.id.recyclerView);
        pb             = findViewById(R.id.progressBar);
        tvTotalSales   = findViewById(R.id.tvTotalSales);
        tvTotalAmount  = findViewById(R.id.tvTotalAmount);
        tvTargetAmount = findViewById(R.id.tvTargetAmount);
        tvEmpty        = findViewById(R.id.tvEmpty);
        barChartSales  = findViewById(R.id.barChartSales);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalesReportAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
    }

    @Override
    protected void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<SalesReport> list = new ArrayList<>();
                    double totalTarget = 0, totalAchieved = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        SalesReport r = d.toObject(SalesReport.class);
                        r.setId(d.getId()); list.add(r);
                        totalTarget   += r.getTargetAmount();
                        totalAchieved += r.getAchievedAmount();
                    }
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    if (tvTotalSales   != null) tvTotalSales.setText(String.valueOf(list.size()));
                    if (tvTotalAmount  != null) tvTotalAmount.setText(String.format("%.0f", totalAchieved));
                    if (tvTargetAmount != null) tvTargetAmount.setText(String.format("%.0f", totalTarget));

                    setupBarChart(totalTarget, totalAchieved);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
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
}