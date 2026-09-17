package com.workverse.app.activities.manager;

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
import com.workverse.app.activities.admin.AddPerformanceActivity;
import com.workverse.app.adapters.PerformanceAdapter;
import com.workverse.app.models.Employee;
import com.workverse.app.models.PerformanceReport;
import com.workverse.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerPerformanceActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar pb;
    TextView tvEmpty;
    FloatingActionButton fab;
    PerformanceAdapter adapter;

    AutoCompleteTextView actFilterDesignation, actFilterCampaign;

    List<PerformanceReport> fullList = new ArrayList<>();

    // userId -> designation / campaign (from employees collection)
    Map<String, String> designationByUser = new HashMap<>();
    Map<String, String> campaignByUser = new HashMap<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_performance);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fab = findViewById(R.id.fabAdd);
        actFilterDesignation = findViewById(R.id.actFilterDesignation);
        actFilterCampaign = findViewById(R.id.actFilterCampaign);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerformanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        if (fab != null) fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddPerformanceActivity.class)));

        setupFilterDropdowns();
        loadEmployeeInfoThenData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEmployeeInfoThenData();
    }

    private void setupFilterDropdowns() {
        List<String> designations = Arrays.asList("All", "Fronters", "Verifiers", "Closers");
        List<String> campaigns = Arrays.asList("All", "MEDICARE", "FE", "Home Warranty");

        if (actFilterDesignation != null) {
            actFilterDesignation.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, designations));
            actFilterDesignation.setOnItemClickListener((p, v, pos, id) -> applyFilters());
            actFilterDesignation.setOnClickListener(v -> actFilterDesignation.showDropDown());
        }

        if (actFilterCampaign != null) {
            actFilterCampaign.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, campaigns));
            actFilterCampaign.setOnItemClickListener((p, v, pos, id) -> applyFilters());
            actFilterCampaign.setOnClickListener(v -> actFilterCampaign.showDropDown());
        }
    }

    /** First load employees (for designation/campaign lookup), then load performance records. */
    private void loadEmployeeInfoThenData() {
        if (pb != null) pb.setVisibility(View.VISIBLE);

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).get()
                .addOnSuccessListener(snap -> {
                    designationByUser.clear();
                    campaignByUser.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Employee e = d.toObject(Employee.class);
                        if (e != null && e.getUserId() != null) {
                            if (e.getDesignation() != null)
                                designationByUser.put(e.getUserId(), e.getDesignation());
                            if (e.getCampaign() != null)
                                campaignByUser.put(e.getUserId(), e.getCampaign());
                        }
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
                        r.setId(d.getId());
                        fullList.add(r);
                    }
                    if (pb != null) pb.setVisibility(View.GONE);
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    if (pb != null) pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilters() {
        String selDesignation = actFilterDesignation != null
                ? actFilterDesignation.getText().toString().trim() : "All";
        String selCampaign = actFilterCampaign != null
                ? actFilterCampaign.getText().toString().trim() : "All";

        if (selDesignation.isEmpty()) selDesignation = "All";
        if (selCampaign.isEmpty()) selCampaign = "All";

        List<PerformanceReport> filtered = new ArrayList<>();
        for (PerformanceReport r : fullList) {
            String uid = r.getUserId();

            // Designation: performance record has none, so look it up from employees
            String designation = uid != null ? designationByUser.get(uid) : null;

            // Campaign: prefer the one stored on the record, fall back to employee record
            String campaign = r.getCampaign();
            if ((campaign == null || campaign.isEmpty()) && uid != null) {
                campaign = campaignByUser.get(uid);
            }

            boolean designationOk = "All".equalsIgnoreCase(selDesignation)
                    || (designation != null && designation.equalsIgnoreCase(selDesignation));

            boolean campaignOk = "All".equalsIgnoreCase(selCampaign)
                    || (campaign != null && campaign.equalsIgnoreCase(selCampaign));

            if (designationOk && campaignOk) filtered.add(r);
        }

        if (tvEmpty != null)
            tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

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
        if (pb != null) pb.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Syncing AI Insights...", Toast.LENGTH_SHORT).show();

        java.util.LinkedHashSet<String> userIds = new java.util.LinkedHashSet<>();
        for (PerformanceReport r : fullList) {
            userIds.add(r.getUserId());
        }

        for (String uid : userIds) {
            com.workverse.app.utils.KPISyncHelper.recomputeForUser(uid);
        }

        rv.postDelayed(() -> {
            if (pb != null) pb.setVisibility(View.GONE);
            Toast.makeText(this, "AI Sync triggered for " + userIds.size() + " users.", Toast.LENGTH_SHORT).show();
            loadEmployeeInfoThenData();
        }, 4000);
    }
}