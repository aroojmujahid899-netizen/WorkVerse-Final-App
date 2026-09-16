package com.workverse.app.activities.manager;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.SalesReportAdapter;
import com.workverse.app.models.SalesReport;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ManagerSalesReportActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvStat1, tvStat2;
    BarChart barChartSales;
    FloatingActionButton fab; SalesReportAdapter adapter;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_sales_report);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv      = findViewById(R.id.recyclerView);
        pb      = findViewById(R.id.progressBar);
        tvStat1 = findViewById(R.id.tvStat1);
        tvStat2 = findViewById(R.id.tvStat2);
        barChartSales = findViewById(R.id.barChartSales);
        fab     = findViewById(R.id.fabAdd);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalesReportAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        if (fab != null) fab.setOnClickListener(v -> showAddDialog());
        loadData();
    }
    @Override protected void onResume() { super.onResume(); loadData(); }
    private void showAddDialog() {
        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 12);
        EditText etName    = new EditText(this); etName.setHint("Employee Name");
        EditText etDate    = new EditText(this); etDate.setHint("Date");
        etDate.setText(DateTimeUtils.getCurrentDate());
        EditText etMonth   = new EditText(this); etMonth.setHint("Month (e.g. March 2026)");
        EditText etTarget  = new EditText(this); etTarget.setHint("Target Amount");
        etTarget.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText etAchieve = new EditText(this); etAchieve.setHint("Achieved Amount");
        etAchieve.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ll.addView(etName); ll.addView(etDate); ll.addView(etMonth); ll.addView(etTarget); ll.addView(etAchieve);
        new AlertDialog.Builder(this).setTitle("Add Sales Record").setView(ll)
                .setPositiveButton("Save", (d, w) -> {
                    String name  = etName.getText().toString().trim();
                    String date  = etDate.getText().toString().trim();
                    String month = etMonth.getText().toString().trim();
                    String tStr  = etTarget.getText().toString().trim();
                    String aStr  = etAchieve.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(tStr) || TextUtils.isEmpty(aStr)) {
                        Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show(); return;
                    }
                    double target = 0, achieved = 0;
                    try { target = Double.parseDouble(tStr); achieved = Double.parseDouble(aStr); }
                    catch (NumberFormatException e) { Toast.makeText(this, "Invalid amounts", Toast.LENGTH_SHORT).show(); return; }
                    Map<String, Object> data = new HashMap<>();
                    data.put("userId", ""); data.put("employeeName", name);
                    data.put("date", date); data.put("month", TextUtils.isEmpty(month) ? date : month);
                    data.put("targetAmount", target); data.put("achievedAmount", achieved);
                    data.put("timestamp", System.currentTimeMillis());
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES).add(data)
                            .addOnSuccessListener(r -> { Toast.makeText(this, "Sale record added!", Toast.LENGTH_SHORT).show(); loadData(); })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }
    private void loadData() {
        if (pb != null) pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES).get()
                .addOnSuccessListener(snap -> {
                    List<SalesReport> list = new ArrayList<>();
                    double tt = 0, ta = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        SalesReport r = d.toObject(SalesReport.class); r.setId(d.getId()); list.add(r);
                        tt += r.getTargetAmount(); ta += r.getAchievedAmount();
                    }
                    if (pb != null) pb.setVisibility(View.GONE);
                    if (tvStat1 != null) tvStat1.setText(String.valueOf((long) tt));
                    if (tvStat2 != null) tvStat2.setText(String.valueOf((long) ta));
                    setupBarChart(tt, ta);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    if (pb != null) pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
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