package com.workverse.app.activities.employee;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.SalesReportAdapter;
import com.workverse.app.models.SalesReport;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmployeeSalesReportActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar pb;
    private TextView tvStat1, tvStat2, tvEmpty;
    private BarChart barChartSales;
    private SalesReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_sales_report);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("My Sales");
        tb.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvStat1 = findViewById(R.id.tvStat1);
        tvStat2 = findViewById(R.id.tvStat2);
        tvEmpty = findViewById(R.id.tvEmpty);
        barChartSales = findViewById(R.id.barChartSales);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalesReportAdapter(new ArrayList<>(), this::showEntryDialog);
        rv.setAdapter(adapter);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        SharedPrefManager spm = SharedPrefManager.getInstance(this);
        final String myUid = spm.getUid();
        final String myName = spm.getFullName();

        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES).get()
                .addOnSuccessListener(snap -> {
                    List<SalesReport> mine = new ArrayList<>();
                    int totalTarget = 0, totalAchieved = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        SalesReport r = d.toObject(SalesReport.class);
                        r.setId(d.getId());

                        // userId se match; purani entries (userId ke baghair) naam se match
                        boolean isMine = (myUid != null && myUid.equals(r.getUserId()))
                                || ((r.getUserId() == null || r.getUserId().isEmpty())
                                && myName != null && myName.equalsIgnoreCase(r.getEmployeeName()));
                        if (!isMine) continue;

                        mine.add(r);
                        totalTarget += r.getTargetAmount();
                        totalAchieved += r.getAchievedAmount();
                    }
                    Collections.sort(mine, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                    pb.setVisibility(View.GONE);
                    tvStat1.setText(String.valueOf(totalTarget));
                    tvStat2.setText(String.valueOf(totalAchieved));
                    tvEmpty.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
                    setupBarChart(totalTarget, totalAchieved);
                    adapter.updateList(mine);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load sales", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBarChart(int target, int achieved) {
        styleChart(barChartSales, target, achieved, "Target vs Achieved");
        barChartSales.animateY(800);
    }

    /** Ek chart: 2 bars (Target, Achieved), labels neeche, Y-axis 0 se. */
    private void styleChart(BarChart chart, int target, int achieved, String label) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, target));
        entries.add(new BarEntry(1f, achieved));

        BarDataSet set = new BarDataSet(entries, label);
        set.setColors(new int[]{Color.parseColor("#1976D2"), Color.parseColor("#388E3C")});
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(12f);
        set.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        chart.setData(new BarData(set));
        chart.getDescription().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);
        x.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Target", "Achieved"}));

        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);
        chart.invalidate();
    }

    /** Entry par click: sirf us entry ka alag graph + details. */
    private void showEntryDialog(SalesReport r) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(32, 24, 32, 24);

        TextView tvTitle = new TextView(this);
        tvTitle.setText((r.getEmployeeName() != null ? r.getEmployeeName() : "") + " - " + r.getDate());
        tvTitle.setTextSize(16f);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setPadding(0, 0, 0, 8);
        container.addView(tvTitle);

        TextView tvInfo = new TextView(this);
        String info = (r.getDesignation() != null ? r.getDesignation() : "")
                + (r.getCampaign() != null ? "  |  " + r.getCampaign() : "");
        tvInfo.setText(info);
        tvInfo.setTextSize(13f);
        tvInfo.setPadding(0, 0, 0, 16);
        container.addView(tvInfo);

        BarChart chart = new BarChart(this);
        chart.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600));
        styleChart(chart, r.getTargetAmount(), r.getAchievedAmount(), "Target vs Achieved");
        chart.animateY(600);
        container.addView(chart);

        TextView tvLine = new TextView(this);
        tvLine.setText("Achieved: " + r.getAchievedAmount() + " / Target: " + r.getTargetAmount());
        tvLine.setTextSize(14f);
        tvLine.setTextColor(Color.BLACK);
        tvLine.setPadding(0, 16, 0, 0);
        container.addView(tvLine);

        new AlertDialog.Builder(this)
                .setView(container)
                .setPositiveButton("Close", null)
                .show();
    }
}