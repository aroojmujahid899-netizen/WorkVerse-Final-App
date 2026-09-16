package com.workverse.app.activities.employee;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.NotificationAdapter;
import com.workverse.app.models.Notification;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class EmployeeNotificationsActivity extends AppCompatActivity {

    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    NotificationAdapter adapter;

    String myUid;
    String myDesignation = "";

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_employee_notifications);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        myUid = SharedPrefManager.getInstance(this).getUid();
        loadMyDesignationThenData();
    }

    private void loadMyDesignationThenData() {
        if (myUid == null) { loadData(); return; }
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS).document(myUid).get()
                .addOnSuccessListener(doc -> {
                    String desig = doc.getString("designation");
                    myDesignation = desig != null ? desig : "";
                    loadData();
                })
                .addOnFailureListener(e -> loadData());
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_NOTIFICATIONS)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        Notification n = d.toObject(Notification.class);
                        n.setId(d.getId());

                        String role = n.getTargetRole();
                        String targetUserId = n.getTargetUserId();
                        String targetDesignation = n.getTargetDesignation();

                        boolean isForMe = false;

                        // 1. Sent specifically to this employee
                        if (targetUserId != null && !targetUserId.isEmpty()) {
                            isForMe = targetUserId.equals(myUid);
                        }
                        // 2. Broadcast to everyone
                        else if ("All".equals(role)) {
                            isForMe = true;
                        }
                        // 3. Sent to all Employees or to my specific designation
                        else if ("Employee".equals(role)) {
                            if (targetDesignation == null || targetDesignation.isEmpty()
                                    || "All".equals(targetDesignation)) {
                                isForMe = true;
                            } else {
                                isForMe = targetDesignation.equals(myDesignation);
                            }
                        }

                        if (isForMe) list.add(n);
                    }
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> { pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show(); });
    }
}