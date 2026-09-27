package com.workverse.app.activities.ceo;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CEOPerformanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    TextView tvAvgKpi, tvTotalRecords, tvNeedsImprovement, tvEmpty;
    AutoCompleteTextView actDesignationFilter, actCampaignFilter;
    PerformanceAdapter adapter;
    List<PerformanceReport> fullList = new ArrayList<>();
    Map<String, String> userDesignationMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_performance);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Overall KPI Reports");
        tb.setNavigationOnClickListener(v -> finish());

        rv                    = findViewById(R.id.recyclerView);
        pb                    = findViewById(R.id.progressBar);
        tvAvgKpi              = findViewById(R.id.tvAvgKpi);
        tvTotalRecords        = findViewById(R.id.tvTotalRecords);
        tvNeedsImprovement    = findViewById(R.id.tvNeedsImprovement);
        tvEmpty               = findViewById(R.id.tvEmpty);
        actDesignationFilter  = findViewById(R.id.actDesignationFilter);
        actCampaignFilter     = findViewById(R.id.actCampaignFilter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerformanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        setupFilterDropdowns();
        loadUserInfoThenData();
    }

    @Override
    protected void onResume() { super.onResume(); loadUserInfoThenData(); }

    private void setupFilterDropdowns() {
        List<String> desigList = Arrays.asList("All", "Fronters", "Verifiers", "Closers");
        List<String> campList = Arrays.asList("All", "MEDICARE", "FE", "Home Warranty");

        actDesignationFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, desigList));
        actCampaignFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campList));

        actDesignationFilter.setOnClickListener(v -> actDesignationFilter.showDropDown());
        actDesignationFilter.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actDesignationFilter.showDropDown();
        });

        actCampaignFilter.setOnClickListener(v -> actCampaignFilter.showDropDown());
        actCampaignFilter.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actCampaignFilter.showDropDown();
        });

        if (actDesignationFilter.getText().toString().isEmpty()) actDesignationFilter.setText("All", false);
        if (actCampaignFilter.getText().toString().isEmpty()) actCampaignFilter.setText("All", false);

        actDesignationFilter.setOnItemClickListener((parent, view, position, id) -> applyFilters());
        actCampaignFilter.setOnItemClickListener((parent, view, position, id) -> applyFilters());
    }

    private void loadUserInfoThenData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).get()
                .addOnSuccessListener(userSnap -> {
                    userDesignationMap.clear();
                    for (QueryDocumentSnapshot d : userSnap) {
                        String uid = d.getId();
                        String desig = d.getString("designation");
                        if (desig != null) userDesignationMap.put(uid, desig);
                    }
                    loadData();
                })
                .addOnFailureListener(e -> loadData());
    }

    private void loadData() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE).get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        PerformanceReport r = d.toObject(PerformanceReport.class);
                        r.setId(d.getId()); fullList.add(r);
                    }
                    // Latest entries (by timestamp) sabse upar
                    java.util.Collections.sort(fullList, (a, b) -> {
                        long ta = a.getTimestamp() != null ? a.getTimestamp() : 0;
                        long tb = b.getTimestamp() != null ? b.getTimestamp() : 0;
                        return Long.compare(tb, ta);
                    });
                    pb.setVisibility(View.GONE);
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        String desig = actDesignationFilter.getText().toString();
        String camp = actCampaignFilter.getText().toString();

        List<PerformanceReport> filtered = new ArrayList<>();
        double totalKpi = 0;
        int needsImprovement = 0;
        for (PerformanceReport r : fullList) {
            String rDesig = userDesignationMap.get(r.getUserId());
            boolean desigMatch = "All".equals(desig) || desig.equals(rDesig);
            boolean campMatch = "All".equals(camp) || camp.equals(r.getCampaign());
            if (desigMatch && campMatch) {
                filtered.add(r);
                totalKpi += r.getDisplayKpi();
                if (r.getDisplayKpi() < 70) needsImprovement++;
            }
        }

        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        double avg = filtered.isEmpty() ? 0 : totalKpi / filtered.size();
        if (tvAvgKpi != null) tvAvgKpi.setText(String.format("%.0f%%", avg));
        if (tvTotalRecords != null) tvTotalRecords.setText(String.valueOf(filtered.size()));
        if (tvNeedsImprovement != null) tvNeedsImprovement.setText(String.valueOf(needsImprovement));
        adapter.updateList(filtered);
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