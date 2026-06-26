package com.workverse.app.activities.manager;

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

public class ManagerLeaveStatusActivity extends AppCompatActivity {

    private RecyclerView         rv;
    private ProgressBar          pb;
    private TextView             tvEmpty, tvTotal, tvPending, tvApproved;
    private FloatingActionButton fab;
    private LeaveRequestAdapter  adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_leave_status);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        rv         = findViewById(R.id.recyclerView);
        pb         = findViewById(R.id.progressBar);
        tvEmpty    = findViewById(R.id.tvEmpty);
        tvTotal    = findViewById(R.id.tvTotalLeaves);
        tvPending  = findViewById(R.id.tvPendingLeaves);
        tvApproved = findViewById(R.id.tvApprovedLeaves);
        fab        = findViewById(R.id.fabApply);

        rv.setLayoutManager(new LinearLayoutManager(this));
        // Manager cannot approve their own leaves
        adapter = new LeaveRequestAdapter(new ArrayList<>(), false, null);
        rv.setAdapter(adapter);

        fab.setOnClickListener(v ->
                startActivity(new Intent(this, ManagerApplyLeaveActivity.class)));

        loadLeaves();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLeaves();
    }

    private void loadLeaves() {
        pb.setVisibility(View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        String uid = SharedPrefManager.getInstance(this).getUid();
        if (uid == null) {
            pb.setVisibility(View.GONE);
            Toast.makeText(this, "Session error. Please login again.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // NO orderBy — just whereEqualTo to avoid composite index requirement
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    List<LeaveRequest> list = new ArrayList<>();
                    int pending = 0, approved = 0;

                    for (QueryDocumentSnapshot doc : snap) {
                        LeaveRequest lr = doc.toObject(LeaveRequest.class);
                        lr.setId(doc.getId());
                        list.add(lr);
                        String st = lr.getStatus();
                        if ("Pending".equals(st))  pending++;
                        if ("Approved".equals(st)) approved++;
                    }

                    // Sort by timestamp descending — newest first — done in Java
                    Collections.sort(list, (a, b) ->
                            Long.compare(b.getTimestamp(), a.getTimestamp()));

                    pb.setVisibility(View.GONE);

                    if (tvTotal    != null) tvTotal.setText(String.valueOf(list.size()));
                    if (tvPending  != null) tvPending.setText(String.valueOf(pending));
                    if (tvApproved != null) tvApproved.setText(String.valueOf(approved));
                    if (tvEmpty    != null)
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Failed to load: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}