package com.workverse.app.activities.manager;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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
import com.workverse.app.adapters.FeedbackAdapter;
import com.workverse.app.models.Feedback;
import com.workverse.app.models.Notification;
import com.workverse.app.utils.FeedbackAnalysisHelper;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerFeedbackActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    FeedbackAdapter adapter; FloatingActionButton fab;

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_feedback);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty); fab = findViewById(R.id.fabAdd);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackAdapter(new ArrayList<>(), this::retryAnalysis, this::showRespondDialog);
        rv.setAdapter(adapter);
        if (fab != null) fab.setOnClickListener(v -> showSubmitDialog());
        loadData();
    }

    private void retryAnalysis(Feedback f) {
        if (f.getId() == null) return;
        Toast.makeText(this, "Re-analyzing…", Toast.LENGTH_SHORT).show();
        FeedbackAnalysisHelper.retry(f, () -> {
            Toast.makeText(this, "Re-analysis complete", Toast.LENGTH_SHORT).show();
            loadData();
        });
    }

    private void showRespondDialog(Feedback f) {
        if (f.getId() == null || f.getUserId() == null || f.getUserId().isEmpty()) {
            Toast.makeText(this, "Cannot respond: employee info missing", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText etResponse = new EditText(this);
        etResponse.setHint("Type your response...");
        etResponse.setMinLines(3);
        if (f.getManagerResponse() != null) etResponse.setText(f.getManagerResponse());

        int pad = 48;
        etResponse.setPadding(pad, 24, pad, 24);

        new AlertDialog.Builder(this)
                .setTitle("Respond to " + (f.getFromUserName() != null ? f.getFromUserName() : "Employee"))
                .setMessage("Feedback: " + f.getTitle())
                .setView(etResponse)
                .setPositiveButton("Send", (d, w) -> {
                    String response = etResponse.getText().toString().trim();
                    if (TextUtils.isEmpty(response)) {
                        Toast.makeText(this, "Response cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> upd = new HashMap<>();
                    upd.put("managerResponse", response);
                    upd.put("managerResponseAt", System.currentTimeMillis());

                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK)
                            .document(f.getId())
                            .update(upd)
                            .addOnSuccessListener(r -> {
                                sendResponseNotification(f, response);
                                Toast.makeText(this, "Response sent!", Toast.LENGTH_SHORT).show();
                                loadData();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends the response ONLY to the specific employee who submitted this feedback,
     * reusing the existing Notification system's targetUserId filtering.
     */
    private void sendResponseNotification(Feedback f, String response) {
        String sender = SharedPrefManager.getInstance(this).getFullName();
        if (sender == null) sender = "Manager";

        Notification n = new Notification(
                "Response to: " + f.getTitle(),
                response,
                "Employee",
                sender,
                null,          // no designation filter needed
                f.getUserId()  // targets only this employee
        );

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_NOTIFICATIONS)
                .add(n)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Response saved but notification failed", Toast.LENGTH_SHORT).show());
    }

    @Override protected void onResume() { super.onResume(); loadData(); }

    private void showSubmitDialog() {
        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);
        EditText etTitle = new EditText(this); etTitle.setHint("Title");
        EditText etMsg = new EditText(this); etMsg.setHint("Message"); etMsg.setMinLines(2);
        ll.addView(etTitle); ll.addView(etMsg);
        new AlertDialog.Builder(this).setTitle("Submit Feedback").setView(ll)
                .setPositiveButton("Submit", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String msg = etMsg.getText().toString().trim();
                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(msg)) {
                        Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show(); return;
                    }
                    SharedPrefManager spm = SharedPrefManager.getInstance(this);
                    String userId = spm.getUid();
                    Feedback fb = new Feedback(userId, spm.getFullName() != null ? spm.getFullName() : "Manager", title, msg, "Manager");
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).add(fb)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Feedback submitted! AI analysis in progress.", Toast.LENGTH_SHORT).show();
                                FeedbackAnalysisHelper.analyzeAndStore(r.getId(), userId, title, msg, this::loadData);
                                loadData();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK)
                .whereEqualTo("role", "Employee")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Feedback> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        Feedback f = d.toObject(Feedback.class); f.setId(d.getId()); list.add(f);
                    }
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> { pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show(); });
    }
}