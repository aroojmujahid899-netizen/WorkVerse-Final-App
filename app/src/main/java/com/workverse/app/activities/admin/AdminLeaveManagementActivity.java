package com.workverse.app.activities.admin;

import android.os.Bundle;
import android.view.View;
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
    private ProgressBar  pb;
    private TextView     tvEmpty;
    private LeaveRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_leave_management);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        rv      = findViewById(R.id.recyclerView);
        pb      = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));

        // Admin CAN approve/reject everyone — showActions=true
        adapter = new LeaveRequestAdapter(new ArrayList<>(), true,
                new LeaveRequestAdapter.Listener() {
                    public void onApprove(LeaveRequest l) { updateStatus(l, "Approved"); }
                    public void onReject(LeaveRequest l)  { updateStatus(l, "Rejected"); }
                });
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
        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);

        // Admin sees ALL leaves — Employee AND Manager
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_LEAVES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<LeaveRequest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        LeaveRequest lr = d.toObject(LeaveRequest.class);
                        lr.setId(d.getId());
                        list.add(lr);
                    }
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null)
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
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