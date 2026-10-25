package com.workverse.app.activities.admin;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAttendanceActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar pb;
    TextView tvEmpty;
    AutoCompleteTextView actDesignationFilter, actCampaignFilter;
    AttendanceAdapter adapter;
    List<Attendance> fullList = new ArrayList<>();
    Map<String, String> userDesignationMap = new HashMap<>();
    Map<String, String> userCampaignMap = new HashMap<>();
    // Check-in timestamp per record id (0 for old records that have no timestamp)
    Map<String, Long> timestampMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_attendance);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        actDesignationFilter = findViewById(R.id.actDesignationFilter);
        actCampaignFilter = findViewById(R.id.actCampaignFilter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        setupFilterDropdowns();
        loadUserInfoThenData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfoThenData();
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

    private void loadUserInfoThenData() {
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
                    loadData();
                })
                .addOnFailureListener(e -> loadData());
    }

    // Newest date first, then newest check-in first. Done in Java (not in the
    // Firestore query) so records without a "timestamp" field are not dropped.
    private void sortNewestFirst(List<Attendance> list) {
        Collections.sort(list, (x, y) -> {
            String dx = x.getDate() != null ? x.getDate() : "";
            String dy = y.getDate() != null ? y.getDate() : "";
            int byDate = dy.compareTo(dx);
            if (byDate != 0) return byDate;
            Long tx = timestampMap.get(x.getId());
            Long ty = timestampMap.get(y.getId());
            long lx = tx != null ? tx : 0L;
            long ly = ty != null ? ty : 0L;
            return Long.compare(ly, lx);
        });
    }

    private void loadData() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                .get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    timestampMap.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Attendance a = d.toObject(Attendance.class);
                        a.setId(d.getId());
                        Long ts = d.getLong("timestamp");
                        timestampMap.put(d.getId(), ts != null ? ts : 0L);

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
                    sortNewestFirst(fullList);
                    pb.setVisibility(View.GONE);
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    android.util.Log.e("AdminAttendance", "Failed to load attendance", e);
                    Toast.makeText(this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                });
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

        adapter.updateList(filtered);
        if (tvEmpty != null) tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}