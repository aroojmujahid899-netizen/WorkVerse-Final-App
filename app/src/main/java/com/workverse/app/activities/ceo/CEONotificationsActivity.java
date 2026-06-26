package com.workverse.app.activities.ceo;

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
import java.util.ArrayList;
import java.util.List;

public class CEONotificationsActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_notifications); // reuse same layout
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Notifications");
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
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
                    // CEO sees notifications sent to CEO or All
                    if ("CEO".equals(role) || "All".equals(role)) list.add(n);
                }
                pb.setVisibility(View.GONE);
                if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                adapter.updateList(list);
            })
            .addOnFailureListener(e -> {
                pb.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            });
    }
}
