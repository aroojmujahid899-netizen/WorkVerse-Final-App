package com.workverse.app.activities.manager;

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
import com.workverse.app.adapters.LeaveRequestAdapter;
import com.workverse.app.models.LeaveRequest;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerLeaveApprovalActivity extends AppCompatActivity {

    private RecyclerView        rv;
    private ProgressBar         pb;
    private TextView            tvEmpty;
    private AutoCompleteTextView actDesignationFilter, actCampaignFilter;
    private LeaveRequestAdapter adapter;
    private List<LeaveRequest> fullList = new ArrayList<>();
    private Map<String, String> userDesignationMap = new HashMap<>();
    private Map<String, String> userCampaignMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_leave_approval);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        rv      = findViewById(R.id.recyclerView);
        pb      = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        actDesignationFilter = findViewById(R.id.actDesignationFilter);
        actCampaignFilter    = findViewById(R.id.actCampaignFilter);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LeaveRequestAdapter(new ArrayList<>(), true,
                new LeaveRequestAdapter.Listener() {
                    public void onApprove(LeaveRequest l) { updateStatus(l, "Approved"); }
                    public void onReject(LeaveRequest l)  { updateStatus(l, "Rejected"); }
                });
        rv.setAdapter(adapter);

        setupFilterDropdowns();
        loadUserInfoThenLeaves();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfoThenLeaves();
    }

    private void setupFilterDropdowns() {
        List<String> desigList = Arrays.asList("All", "Fronters", "Verifiers", "Closers");
        List<String> campList = Arrays.asList("All", "MEDICARE", "FE", "Home Warranty");

        actDesignationFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, desigList));
        actCampaignFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campList));

        if (actDesignationFilter.getText().toString().isEmpty()) actDesignationFilter.setText("All", false);
        if (actCampaignFilter.getText().toString().isEmpty()) actCampaignFilter.setText("All", false);

        actDesignationFilter.setOnItemClickListener((parent, view, position, id) -> applyFilters());
        actCampaignFilter.setOnItemClickListener((parent, view, position, id) -> applyFilters());
    }

    private void loadUserInfoThenLeaves() {
        pb.setVisibility(View.VISIBLE);

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).get()
                .addOnSuccessListener(userSnap -> {
                    userDesignationMap.clear();
                    userCampaignMap.clear();
                    for (QueryDocumentSnapshot d : userSnap) {
                        String uid = d.getId();
                        String desig = d.getString("designation");
                        String camp = d.getString("campaign");
                        if (desig != null) userDesignationMap.put(uid, desig);
                        if (camp != null) userCampaignMap.put(uid, camp);
                    }
                    loadLeaves();
                })
                .addOnFailureListener(e -> loadLeaves());
    }

    private void loadLeaves() {
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        LeaveRequest lr = d.toObject(LeaveRequest.class);
                        lr.setId(d.getId());

                        String role = lr.getRole();
                        if (role == null || role.isEmpty() || "Employee".equals(role)) {
                            // Fill missing designation/campaign from Users lookup (old records)
                            if (lr.getDesignation() == null || lr.getDesignation().isEmpty()) {
                                String fallback = userDesignationMap.get(lr.getUserId());
                                if (fallback != null) lr.setDesignation(fallback);
                            }
                            if (lr.getCampaign() == null || lr.getCampaign().isEmpty()) {
                                String fallback = userCampaignMap.get(lr.getUserId());
                                if (fallback != null) lr.setCampaign(fallback);
                            }
                            fullList.add(lr);
                        }
                    }

                    Collections.sort(fullList, (a, b) ->
                            Long.compare(b.getTimestamp(), a.getTimestamp()));

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

        List<LeaveRequest> filtered = new ArrayList<>();
        for (LeaveRequest lr : fullList) {
            boolean desigMatch = "All".equals(desig) || desig.equals(lr.getDesignation());
            boolean campMatch = "All".equals(camp) || camp.equals(lr.getCampaign());
            if (desigMatch && campMatch) filtered.add(lr);
        }

        if (tvEmpty != null) tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.updateList(filtered);
    }

    private void updateStatus(LeaveRequest lr, String status) {
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .document(lr.getId())
                .update("status", status)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Leave " + status, Toast.LENGTH_SHORT).show();
                    loadUserInfoThenLeaves();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
}