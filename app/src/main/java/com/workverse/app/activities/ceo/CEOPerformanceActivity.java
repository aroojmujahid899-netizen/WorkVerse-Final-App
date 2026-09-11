package com.workverse.app.activities.ceo;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.PerformanceAdapter;
import com.workverse.app.models.PerformanceReport;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class CEOPerformanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    TextView tvAvgKpi, tvTotalRecords, tvNeedsImprovement, tvEmpty;
    PerformanceAdapter adapter;
    List<PerformanceReport> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_performance);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Overall KPI Reports");
        tb.setNavigationOnClickListener(v -> finish());

        rv                 = findViewById(R.id.recyclerView);
        pb                 = findViewById(R.id.progressBar);
        tvAvgKpi           = findViewById(R.id.tvAvgKpi);
        tvTotalRecords     = findViewById(R.id.tvTotalRecords);
        tvNeedsImprovement = findViewById(R.id.tvNeedsImprovement);
        tvEmpty            = findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerformanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
    }

    @Override
    protected void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE).get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    double totalKpi = 0;
                    int needsImprovement = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        PerformanceReport r = d.toObject(PerformanceReport.class);
                        r.setId(d.getId()); fullList.add(r);
                        totalKpi += r.getDisplayKpi();
                        if (r.getDisplayKpi() < 70) needsImprovement++;
                    }
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(fullList.isEmpty() ? View.VISIBLE : View.GONE);
                    double avg = fullList.isEmpty() ? 0 : totalKpi / fullList.size();
                    if (tvAvgKpi != null) tvAvgKpi.setText(String.format("%.0f%%", avg));
                    if (tvTotalRecords != null) tvTotalRecords.setText(String.valueOf(fullList.size()));
                    if (tvNeedsImprovement != null) tvNeedsImprovement.setText(String.valueOf(needsImprovement));
                    adapter.updateList(fullList);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_performance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_sync_ai) {
            syncAllAiInsights();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void syncAllAiInsights() {
        if (fullList.isEmpty()) {
            Toast.makeText(this, "No records to sync", Toast.LENGTH_SHORT).show();
            return;
        }
        pb.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Syncing AI Insights...", Toast.LENGTH_SHORT).show();
        
        java.util.LinkedHashSet<String> userIds = new java.util.LinkedHashSet<>();
        for (PerformanceReport r : fullList) {
            userIds.add(r.getUserId());
        }

        for (String uid : userIds) {
            com.workverse.app.utils.KPISyncHelper.recomputeForUser(uid);
        }

        rv.postDelayed(() -> {
            pb.setVisibility(View.GONE);
            Toast.makeText(this, "AI Sync triggered for " + userIds.size() + " users.", Toast.LENGTH_SHORT).show();
            loadData();
        }, 4000);
    }
}
