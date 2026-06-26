package com.workverse.app.activities.employee;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    FloatingActionButton fab; SalesReportAdapter adapter;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_employee_sales_report);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv      = findViewById(R.id.recyclerView);
        pb      = findViewById(R.id.progressBar);
        tvStat1 = findViewById(R.id.tvStat1);
        tvStat2 = findViewById(R.id.tvStat2);
        fab     = findViewById(R.id.fabAdd);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalesReportAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        if (fab != null) fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddSaleActivity.class)));
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
                    if (tvStat1 != null) tvStat1.setText(String.format("PKR %.0f", tt));
                    if (tvStat2 != null) tvStat2.setText(String.format("PKR %.0f", ta));
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    if (pb != null) pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }
}