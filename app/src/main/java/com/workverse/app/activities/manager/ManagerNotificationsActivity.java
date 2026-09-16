package com.workverse.app.activities.manager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.NotificationAdapter;
import com.workverse.app.models.Notification;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerNotificationsActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar pb;
    TextView tvEmpty;
    NotificationAdapter adapter;
    FloatingActionButton fab;

    // employee list for "specific employee" dropdown
    List<String> employeeDisplayNames = new ArrayList<>();
    Map<String, String> employeeNameToUid = new HashMap<>();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_notifications);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Notifications");
        tb.setNavigationOnClickListener(v -> finish());

        rv      = findViewById(R.id.recyclerView);
        pb      = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fab     = findViewById(R.id.fabAdd);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        rv.setAdapter(adapter);

        if (fab != null) fab.setOnClickListener(v -> loadEmployeesThenShowDialog());
        loadData();
    }

    private void loadEmployeesThenShowDialog() {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_USERS)
                .whereEqualTo("role", "Employee")
                .get()
                .addOnSuccessListener(snap -> {
                    employeeDisplayNames.clear();
                    employeeNameToUid.clear();
                    employeeDisplayNames.add("None (use filter above)");
                    for (QueryDocumentSnapshot d : snap) {
                        String name = d.getString("fullName");
                        if (name == null || name.isEmpty()) name = d.getString("name");
                        if (name == null || name.isEmpty()) name = "Employee";
                        employeeNameToUid.put(name, d.getId());
                        employeeDisplayNames.add(name);
                    }
                    showSendDialog();
                })
                .addOnFailureListener(e -> {
                    employeeDisplayNames.clear();
                    employeeDisplayNames.add("None (use filter above)");
                    showSendDialog();
                });
    }

    private void showSendDialog() {
        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Title");

        EditText etMsg = new EditText(this);
        etMsg.setHint("Message");
        etMsg.setMinLines(2);

        TextView tvLabel1 = new TextView(this);
        tvLabel1.setText("Send to (Designation):");
        tvLabel1.setPadding(0, 24, 0, 4);

        Spinner spDesignation = new Spinner(this);
        ArrayAdapter<String> desigAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Fronters", "Verifiers", "Closers"});
        desigAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDesignation.setAdapter(desigAdapter);

        TextView tvLabel2 = new TextView(this);
        tvLabel2.setText("OR send to a specific employee only:");
        tvLabel2.setPadding(0, 24, 0, 4);

        Spinner spEmployee = new Spinner(this);
        ArrayAdapter<String> empAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, employeeDisplayNames);
        empAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEmployee.setAdapter(empAdapter);

        ll.addView(etTitle);
        ll.addView(etMsg);
        ll.addView(tvLabel1);
        ll.addView(spDesignation);
        ll.addView(tvLabel2);
        ll.addView(spEmployee);

        new AlertDialog.Builder(this)
                .setTitle("Send Notification")
                .setView(ll)
                .setPositiveButton("Send", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String msg = etMsg.getText().toString().trim();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(msg)) {
                        Toast.makeText(this, "Title and message required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String sender = SharedPrefManager.getInstance(this).getFullName();
                    if (sender == null) sender = "Manager";

                    String selectedEmployeeName = (String) spEmployee.getSelectedItem();
                    String targetRole;
                    String targetDesignation = null;
                    String targetUserId = null;
                    String sentToLabel;

                    if (selectedEmployeeName != null && employeeNameToUid.containsKey(selectedEmployeeName)) {
                        // Specific employee selected - overrides designation filter
                        targetUserId = employeeNameToUid.get(selectedEmployeeName);
                        targetRole = "Employee";
                        sentToLabel = selectedEmployeeName;
                    } else {
                        String designation = (String) spDesignation.getSelectedItem();
                        if ("All".equals(designation)) {
                            targetRole = "All";
                            sentToLabel = "All";
                        } else {
                            targetRole = "Employee";
                            targetDesignation = designation;
                            sentToLabel = designation;
                        }
                    }

                    Notification n = new Notification(title, msg, targetRole, sender, targetDesignation, targetUserId);

                    String finalSentToLabel = sentToLabel;
                    FirebaseHelper.getDb()
                            .collection(FirebaseHelper.COL_NOTIFICATIONS)
                            .add(n)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Sent to " + finalSentToLabel, Toast.LENGTH_SHORT).show();
                                loadData();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_NOTIFICATIONS)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Notification> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        Notification n = d.toObject(Notification.class);
                        n.setId(d.getId());
                        String role = n.getTargetRole();
                        if ("Manager".equals(role) || "All".equals(role))
                            list.add(n);
                    }
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null)
                        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                });
    }
}