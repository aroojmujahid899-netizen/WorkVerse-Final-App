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
import com.workverse.app.adapters.AttendanceAdapter;
import com.workverse.app.models.Attendance;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerAttendanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    AutoCompleteTextView actDesignationFilter, actCampaignFilter;
    AttendanceAdapter adapter;
    List<Attendance> fullList = new ArrayList<>();
    Map<String, String> userDesignationMap = new HashMap<>();
    Map<String, String> userCampaignMap = new HashMap<>();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_attendance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        actDesignationFilter = findViewById(R.id.actDesignationFilter);
        actCampaignFilter = findViewById(R.id.actCampaignFilter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        setupFilterDropdowns();
        loadUserInfoThenData();
    }

    @Override protected void onResume() { super.onResume(); loadUserInfoThenData(); }

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

    private void loadUserInfoThenData() {
        pb.setVisibility(View.VISIBLE);

        // Build userId -> designation/campaign map from Users collection
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
                    loadData();
                })
                .addOnFailureListener(e -> loadData());
    }

    private void loadData() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Attendance a = d.toObject(Attendance.class);
                        if ("Manager".equals(a.getRole())) continue;
                        a.setId(d.getId());

                        // Fill missing designation/campaign from Users lookup (for old records)
                        if (a.getDesignation() == null || a.getDesignation().isEmpty()) {
                            String fallback = userDesignationMap.get(a.getUserId());
                            if (fallback != null) a.setDesignation(fallback);
                        }
                        if (a.getCampaign() == null || a.getCampaign().isEmpty()) {
                            String fallback = userCampaignMap.get(a.getUserId());
                            if (fallback != null) a.setCampaign(fallback);
                        }

                        fullList.add(a);
                    }
                    pb.setVisibility(View.GONE);
                    applyFilters();
                })
                .addOnFailureListener(e -> { pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show(); });
    }

    private void applyFilters() {
        String desig = actDesignationFilter.getText().toString();
        String camp = actCampaignFilter.getText().toString();

        List<Attendance> filtered = new ArrayList<>();
        for (Attendance a : fullList) {
            boolean desigMatch = "All".equals(desig) || desig.equals(a.getDesignation());
            boolean campMatch = "All".equals(camp) || camp.equals(a.getCampaign());
            if (desigMatch && campMatch) filtered.add(a);
        }

        if (tvEmpty != null) tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.updateList(filtered);
    }
}