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
import com.workverse.app.utils.AIFeedbackAnalyzer;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
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
        adapter = new FeedbackAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        if (fab != null) fab.setOnClickListener(v -> showSubmitDialog());
        loadData();
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
                    Feedback fb = new Feedback(spm.getUid(), spm.getFullName() != null ? spm.getFullName() : "Manager", title, msg, "Manager");
                    fb.setSentiment(AIFeedbackAnalyzer.analyzeSentiment(msg + " " + title));
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).add(fb)
                            .addOnSuccessListener(r -> { Toast.makeText(this, "Feedback submitted!", Toast.LENGTH_SHORT).show(); loadData(); })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        // Manager reviews feedback submitted BY employees (not just their own)
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
