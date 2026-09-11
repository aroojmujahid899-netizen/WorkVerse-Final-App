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
import com.workverse.app.adapters.FeedbackAdapter;
import com.workverse.app.models.Feedback;
import com.workverse.app.utils.FeedbackAnalysisHelper;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class CEOFeedbackActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    TextView tvPositive, tvNegative, tvNeutral, tvEmpty;
    FeedbackAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_feedback);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("AI Feedback Insights");
        tb.setNavigationOnClickListener(v -> finish());

        rv         = findViewById(R.id.recyclerView);
        pb         = findViewById(R.id.progressBar);
        tvPositive = findViewById(R.id.tvPositive);
        tvNegative = findViewById(R.id.tvNegative);
        tvNeutral  = findViewById(R.id.tvNeutral);
        tvEmpty    = findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackAdapter(new ArrayList<>(), this::retryAnalysis);
        rv.setAdapter(adapter);
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

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Feedback> list = new ArrayList<>();
                    int pos = 0, neg = 0, neu = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        Feedback f = d.toObject(Feedback.class);
                        f.setId(d.getId()); list.add(f);
                        // Only count feedback the AI has actually finished analyzing —
                        // pending/failed items shouldn't skew the sentiment breakdown.
                        if (f.hasAiResult()) {
                            String sent = f.getAiSentiment();
                            if ("Positive".equals(sent)) pos++;
                            else if ("Negative".equals(sent)) neg++;
                            else neu++;
                        }
                    }
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    if (tvPositive != null) tvPositive.setText(String.valueOf(pos));
                    if (tvNegative != null) tvNegative.setText(String.valueOf(neg));
                    if (tvNeutral  != null) tvNeutral.setText(String.valueOf(neu));
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }
}
