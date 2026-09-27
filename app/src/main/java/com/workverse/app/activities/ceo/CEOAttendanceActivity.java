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
import com.workverse.app.adapters.AttendanceAdapter;
import com.workverse.app.models.Attendance;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CEOAttendanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    TextView tvPresentCount, tvAbsentCount, tvTotalRecords, tvEmpty;
    AutoCompleteTextView actDesignationFilter, actCampaignFilter;
    AttendanceAdapter adapter;
    List<Attendance> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_attendance);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Attendance Summary");
        tb.setNavigationOnClickListener(v -> finish());

        rv                    = findViewById(R.id.recyclerView);
        pb                    = findViewById(R.id.progressBar);
        tvPresentCount        = findViewById(R.id.tvPresentCount);
        tvAbsentCount         = findViewById(R.id.tvAbsentCount);
        tvTotalRecords        = findViewById(R.id.tvTotalRecords);
        tvEmpty               = findViewById(R.id.tvEmpty);
        actDesignationFilter  = findViewById(R.id.actDesignationFilter);
        actCampaignFilter     = findViewById(R.id.actCampaignFilter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        setupFilterDropdowns();
        loadData();
    }

    @Override
    protected void onResume() { super.onResume(); loadData(); }

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

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        String today = DateTimeUtils.getCurrentDate();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("date", today).get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Attendance a = d.toObject(Attendance.class);
                        a.setId(d.getId()); fullList.add(a);
                    }
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

        List<Attendance> filtered = new ArrayList<>();
        int present = 0, absent = 0;
        for (Attendance a : fullList) {
            boolean desigMatch = "All".equals(desig) || desig.equals(a.getDesignation());
            boolean campMatch = "All".equals(camp) || camp.equals(a.getCampaign());
            if (desigMatch && campMatch) {
                filtered.add(a);
                if ("Present".equals(a.getStatus())) present++;
                else absent++;
            }
        }

        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (tvPresentCount != null) tvPresentCount.setText(String.valueOf(present));
        if (tvAbsentCount  != null) tvAbsentCount.setText(String.valueOf(absent));
        if (tvTotalRecords != null) tvTotalRecords.setText(String.valueOf(filtered.size()));
        adapter.updateList(filtered);
    }
}