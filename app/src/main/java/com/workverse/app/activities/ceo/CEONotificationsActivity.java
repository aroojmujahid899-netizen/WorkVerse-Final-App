
package com.workverse.app.activities.ceo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton
        .FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.NotificationAdapter;
import com.workverse.app.models.Notification;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;

public class CEONotificationsActivity
        extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar pb;
    TextView tvEmpty;
    NotificationAdapter adapter;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        // Reuse manager_notifications layout
        setContentView(
                R.layout.activity_manager_notifications);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Notifications");
        tb.setNavigationOnClickListener(v -> finish());

        rv      = findViewById(R.id.recyclerView);
        pb      = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fab     = findViewById(R.id.fabAdd);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        if (fab != null)
            fab.setOnClickListener(v -> showSendDialog());
        loadData();
    }

    private void showSendDialog() {
        android.widget.LinearLayout ll =
                new android.widget.LinearLayout(this);
        ll.setOrientation(
                android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);
        EditText etTitle = new EditText(this);
        etTitle.setHint("Title");
        EditText etMsg = new EditText(this);
        etMsg.setHint("Message");
        etMsg.setMinLines(2);
        Spinner spRole = new Spinner(this);
        ArrayAdapter<String> ra = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All","Employee","Manager","Admin"});
        ra.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(ra);
        ll.addView(etTitle);
        ll.addView(etMsg);
        ll.addView(spRole);

        new AlertDialog.Builder(this)
                .setTitle("Send Notification")
                .setView(ll)
                .setPositiveButton("Send", (d, w) -> {
                    String title = etTitle.getText()
                            .toString().trim();
                    String msg = etMsg.getText()
                            .toString().trim();
                    String role = spRole.getSelectedItem()
                            .toString();
                    if (TextUtils.isEmpty(title) ||
                            TextUtils.isEmpty(msg)) {
                        Toast.makeText(this,
                                "Title and message required",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String sender = SharedPrefManager
                            .getInstance(this).getFullName();
                    Notification n = new Notification(title,
                            msg, role,
                            sender != null ? sender : "CEO");
                    FirebaseHelper.getDb()
                            .collection(
                                    FirebaseHelper.COL_NOTIFICATIONS)
                            .add(n)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this,
                                        "Sent to " + role,
                                        Toast.LENGTH_SHORT).show();
                                loadData();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                                    "Failed", Toast.LENGTH_SHORT)
                                            .show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_NOTIFICATIONS)
                .orderBy("timestamp",
                        com.google.firebase.firestore.Query
                                .Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        Notification n =
                                d.toObject(Notification.class);
                        n.setId(d.getId());
                        String role = n.getTargetRole();
                        if ("CEO".equals(role) ||
                                "All".equals(role))
                            list.add(n);
                    }
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null)
                        tvEmpty.setVisibility(list.isEmpty()
                                ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed",
                            Toast.LENGTH_SHORT).show();
                });
    }
}

