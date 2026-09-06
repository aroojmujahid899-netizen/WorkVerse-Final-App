package com.workverse.app.activities.employee;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.workverse.app.adapters.LeaveRequestAdapter;
import com.workverse.app.models.LeaveRequest;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class ViewLeaveStatusActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty, tvTotal, tvPending, tvApproved;
    FloatingActionButton fab;
    LeaveRequestAdapter adapter;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_view_leave_status);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotal = findViewById(R.id.tvTotalLeaves);
        tvPending = findViewById(R.id.tvPendingLeaves);
        tvApproved = findViewById(R.id.tvApprovedLeaves);
        fab = findViewById(R.id.fabApply);
        rv.setLayoutManager(new LinearLayoutManager(this));
        // Employee cannot approve their own leaves
        adapter = new LeaveRequestAdapter(new ArrayList<>(), false, null);
        rv.setAdapter(adapter);
        if (fab != null) fab.setOnClickListener(v -> startActivity(new Intent(this, ApplyLeaveActivity.class)));
        loadData();
    }
    @Override protected void onResume() { super.onResume(); loadData(); }
    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        String uid = SharedPrefManager.getInstance(this).getUid();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_LEAVES)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    List<LeaveRequest> list = new ArrayList<>();
                    int pending = 0, approved = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        LeaveRequest r = d.toObject(LeaveRequest.class); r.setId(d.getId()); list.add(r);
                        String st = r.getStatus();
                        if ("Pending".equals(st)) pending++;
                        if ("Approved".equals(st)) approved++;
                    }
                    Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    pb.setVisibility(View.GONE);
                    if (tvTotal != null) tvTotal.setText(String.valueOf(list.size()));
                    if (tvPending != null) tvPending.setText(String.valueOf(pending));
                    if (tvApproved != null) tvApproved.setText(String.valueOf(approved));
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> { pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show(); });
    }
}
