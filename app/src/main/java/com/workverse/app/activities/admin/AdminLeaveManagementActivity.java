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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.LeaveRequestAdapter;
import com.workverse.app.models.LeaveRequest;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class AdminLeaveManagementActivity extends AppCompatActivity {

    private RecyclerView rv;
    private ProgressBar pb;
    private TextView tvEmpty;
    private AutoCompleteTextView actvRoleFilter;
    private LeaveRequestAdapter adapter;

    private List<LeaveRequest> fullList = new ArrayList<>();
    private String selectedFilterRole = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_leave_management);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        actvRoleFilter = findViewById(R.id.actvRoleFilter);

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LeaveRequestAdapter(new ArrayList<>(), true,
                new LeaveRequestAdapter.Listener() {
                    @Override
                    public void onApprove(LeaveRequest l) { updateStatus(l, "Approved"); }
                    @Override
                    public void onReject(LeaveRequest l)  { updateStatus(l, "Rejected"); }
                });
        rv.setAdapter(adapter);

        setupRoleDropdown();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void setupRoleDropdown() {
        String[] roles = new String[]{"All", "Employees", "Managers"};
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        actvRoleFilter.setAdapter(dropdownAdapter);

        actvRoleFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedFilterRole = parent.getItemAtPosition(position).toString();
            applyRoleFilter();
        });
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        LeaveRequest lr = d.toObject(LeaveRequest.class);
                        lr.setId(d.getId());
                        fullList.add(lr);
                    }
                    pb.setVisibility(View.GONE);
                    applyRoleFilter();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyRoleFilter() {
        List<LeaveRequest> filteredList = new ArrayList<>();

        if ("All".equalsIgnoreCase(selectedFilterRole)) {
            filteredList.addAll(fullList);
        } else {
            for (LeaveRequest item : fullList) {
                String userRole = item.getRole(); // Fixed: getRole() updated here
                if (userRole != null) {
                    if ("Employees".equalsIgnoreCase(selectedFilterRole) && "Employee".equalsIgnoreCase(userRole)) {
                        filteredList.add(item);
                    } else if ("Managers".equalsIgnoreCase(selectedFilterRole) && "Manager".equalsIgnoreCase(userRole)) {
                        filteredList.add(item);
                    }
                }
            }
        }

        if (tvEmpty != null) {
            tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        }
        adapter.updateList(filteredList);
    }

    private void updateStatus(LeaveRequest lr, String status) {
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .document(lr.getId())
                .update("status", status)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Leave " + status, Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }
}