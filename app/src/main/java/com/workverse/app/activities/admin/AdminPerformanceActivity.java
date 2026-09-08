package com.workverse.app.activities.admin;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.PerformanceAdapter;
import com.workverse.app.models.PerformanceReport;
import com.workverse.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class AdminPerformanceActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar pb;
    TextView tvEmpty, tvAvgKpi, tvTotalRecords, tvNeedsReview;
    AutoCompleteTextView actCampaignFilter;
    PerformanceAdapter adapter;
    FloatingActionButton fabAddPerformance;
    List<PerformanceReport> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_performance);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Performance Reports");
        tb.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvAvgKpi = findViewById(R.id.tvAvgKpi);
        tvTotalRecords = findViewById(R.id.tvTotalRecords);
        tvNeedsReview = findViewById(R.id.tvNeedsReview);
        actCampaignFilter = findViewById(R.id.actCampaignFilter);

        fabAddPerformance = findViewById(R.id.fabAdd);
        if (fabAddPerformance != null) {
            fabAddPerformance.setOnClickListener(v ->
                    startActivity(new Intent(AdminPerformanceActivity.this, AddPerformanceActivity.class)));
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerformanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_PERFORMANCE)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        PerformanceReport r = d.toObject(PerformanceReport.class);
                        r.setId(d.getId());
                        fullList.add(r);
                    }
                    pb.setVisibility(View.GONE);
                    setupCampaignFilter();
                    applyFilter(actCampaignFilter.getText().toString());
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupCampaignFilter() {
        LinkedHashSet<String> campaigns = new LinkedHashSet<>();
        campaigns.add("All");
        for (PerformanceReport r : fullList) {
            if (r.getCampaign() != null && !r.getCampaign().isEmpty()) {
                campaigns.add(r.getCampaign());
            }
        }
        List<String> campaignList = new ArrayList<>(campaigns);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campaignList);
        actCampaignFilter.setAdapter(adapter);

        if (actCampaignFilter.getText().toString().isEmpty()) {
            actCampaignFilter.setText("All", false);
        }

        actCampaignFilter.setOnItemClickListener((parent, view, position, id) ->
                applyFilter(campaignList.get(position)));
    }

    private void applyFilter(String campaign) {
        List<PerformanceReport> filtered = new ArrayList<>();
        double totalKpi = 0;
        int needsReviewCount = 0;

        for (PerformanceReport r : fullList) {
            if ("All".equals(campaign) || campaign.equals(r.getCampaign())) {
                filtered.add(r);
                totalKpi += r.getKpiScore();
                if (r.getKpiScore() < 50) needsReviewCount++;
            }
        }

        double avg = filtered.isEmpty() ? 0 : totalKpi / filtered.size();
        if (tvAvgKpi != null) tvAvgKpi.setText(String.format("%.0f%%", avg));
        if (tvTotalRecords != null) tvTotalRecords.setText(String.valueOf(filtered.size()));
        if (tvNeedsReview != null) tvNeedsReview.setText(String.valueOf(needsReviewCount));

        if (tvEmpty != null) tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.updateList(filtered);
    }
}