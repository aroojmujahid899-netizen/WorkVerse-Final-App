package com.workverse.app.activities.employee;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.workverse.app.R;
import com.workverse.app.models.Feedback;
import com.workverse.app.utils.AIFeedbackAnalyzer;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class SubmitFeedbackActivity extends AppCompatActivity {

    TextInputEditText etTitle, etMessage;
    Button btnSubmit, btnAnalyze;
    ProgressBar pb;
    TextView tvSentimentResult, tvInsightResult, tvAccuracyResult;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_submit_feedback);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Submit Feedback");
        tb.setNavigationOnClickListener(v -> finish());

        etTitle           = findViewById(R.id.etTitle);
        etMessage         = findViewById(R.id.etMessage);
        btnSubmit         = findViewById(R.id.btnSubmit);
        btnAnalyze        = findViewById(R.id.btnAnalyze);
        pb                = findViewById(R.id.progressBar);
        tvSentimentResult = findViewById(R.id.tvSentimentResult);
        tvInsightResult   = findViewById(R.id.tvInsightResult);
        tvAccuracyResult  = findViewById(R.id.tvAccuracyResult);

        // Analyze button — preview AI result before submitting
        if (btnAnalyze != null) {
            btnAnalyze.setOnClickListener(v -> {
                String msg = etMessage.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(this, "Write your feedback first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String sentiment = AIFeedbackAnalyzer.analyzeSentiment(msg);
                String insight   = AIFeedbackAnalyzer.generateInsight(sentiment, "Medium", 60);
                if (tvSentimentResult != null) tvSentimentResult.setText("Sentiment: " + sentiment);
                if (tvInsightResult   != null) tvInsightResult.setText("Insight: " + insight);
                if (tvAccuracyResult  != null) tvAccuracyResult.setText("AI analysis complete — tap Submit to save");
            });
        }

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String msg   = etMessage.getText().toString().trim();
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(msg)) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            pb.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);

            SharedPrefManager spm = SharedPrefManager.getInstance(this);
            Feedback fb = new Feedback(
                spm.getUid(),
                spm.getFullName() != null ? spm.getFullName() : "Employee",
                title, msg, "Employee"
            );
            // AI analysis already done in Feedback constructor
            // Now cross-check with actual sales/calls data for accuracy
            String uid = spm.getUid();
            FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES)
                .whereEqualTo("userId", uid).get()
                .addOnSuccessListener(salesSnap -> {
                    int totalSales = salesSnap.size();
                    // Get performance KPI
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE)
                        .whereEqualTo("userId", uid).get()
                        .addOnSuccessListener(perfSnap -> {
                            double kpi = 0;
                            int calls  = 0;
                            for (com.google.firebase.firestore.QueryDocumentSnapshot d : perfSnap) {
                                Double k = d.getDouble("kpiScore");
                                Integer c = d.getLong("tasksCompleted") != null ?
                                    d.getLong("tasksCompleted").intValue() : 0;
                                if (k != null) kpi += k;
                                calls += c;
                            }
                            double avgKpi = perfSnap.size() > 0 ? kpi / perfSnap.size() : 0;
                            fb.updateWithPerformanceData(calls, totalSales, avgKpi);
                            saveFeedback(fb);
                        })
                        .addOnFailureListener(e -> saveFeedback(fb));
                })
                .addOnFailureListener(e -> saveFeedback(fb));
        });
    }

    private void saveFeedback(Feedback fb) {
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).add(fb)
            .addOnSuccessListener(r -> {
                pb.setVisibility(View.GONE);
                String sent = fb.getSentiment() != null ? fb.getSentiment() : "Neutral";
                Toast.makeText(this, "Feedback submitted! AI Sentiment: " + sent, Toast.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                pb.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "Failed to submit", Toast.LENGTH_SHORT).show();
            });
    }
}
