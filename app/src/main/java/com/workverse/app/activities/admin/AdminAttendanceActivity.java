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
import java.util.List;

public class AdminAttendanceActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar pb;
    TextView tvEmpty;
    AutoCompleteTextView actFilter;
    AttendanceAdapter adapter;
    List<Attendance> fullList = new ArrayList<>();

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
        actFilter = findViewById(R.id.actFilter);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        String[] filterOptions = {"All", "Employees", "Managers"};
        actFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filterOptions));
        actFilter.setText("All", false);
        actFilter.setOnItemClickListener((parent, view, position, id) -> applyFilter(filterOptions[position]));

        loadData();
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Attendance a = d.toObject(Attendance.class);
                        a.setId(d.getId());
                        fullList.add(a);
                    }
                    pb.setVisibility(View.GONE);
                    applyFilter(actFilter.getText().toString());
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load attendance", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilter(String filter) {
        List<Attendance> filtered = new ArrayList<>();
        for (Attendance a : fullList) {
            String role = a.getRole() != null ? a.getRole() : "Employee";
            if ("All".equals(filter)) {
                filtered.add(a);
            } else if ("Employees".equals(filter) && "Employee".equalsIgnoreCase(role)) {
                filtered.add(a);
            } else if ("Managers".equals(filter) && "Manager".equalsIgnoreCase(role)) {
                filtered.add(a);
            }
        }
        adapter.updateList(filtered);
        if (tvEmpty != null) tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}