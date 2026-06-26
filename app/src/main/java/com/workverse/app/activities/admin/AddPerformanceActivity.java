package com.workverse.app.activities.admin;

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
import com.workverse.app.utils.AIFeedbackAnalyzer;
import com.workverse.app.utils.FirebaseHelper;
import java.util.HashMap;
import java.util.Map;

public class AddPerformanceActivity extends AppCompatActivity {

    TextInputEditText etEmployeeName, etEmployeeId, etMonth, etYear,
            etTasksAssigned, etTasksCompleted, etKpiScore, etAttendance,
            etTotalCalls, etTotalSales, etFeedbackText;
    Button btnSubmit;
    ProgressBar pb;
    TextView tvAIPreview;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_add_performance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add Performance Record");
        tb.setNavigationOnClickListener(v -> finish());

        etEmployeeName   = findViewById(R.id.etEmployeeName);
        etEmployeeId     = findViewById(R.id.etEmployeeId);
        etMonth          = findViewById(R.id.etMonth);
        etYear           = findViewById(R.id.etYear);
        etTasksAssigned  = findViewById(R.id.etTasksAssigned);
        etTasksCompleted = findViewById(R.id.etTasksCompleted);
        etKpiScore       = findViewById(R.id.etKpiScore);
        etAttendance     = findViewById(R.id.etAttendance);
        etTotalCalls     = findViewById(R.id.etTotalCalls);
        etTotalSales     = findViewById(R.id.etTotalSales);
        etFeedbackText   = findViewById(R.id.etFeedbackText);
        btnSubmit        = findViewById(R.id.btnSubmit);
        pb               = findViewById(R.id.progressBar);
        tvAIPreview      = findViewById(R.id.tvAIPreview);

        btnSubmit.setOnClickListener(v -> savePerformance());
    }

    private void savePerformance() {
        String name   = etEmployeeName.getText().toString().trim();
        String uid    = etEmployeeId.getText().toString().trim();
        String month  = etMonth.getText().toString().trim();
        String year   = etYear.getText().toString().trim();
        String tasksA = etTasksAssigned.getText().toString().trim();
        String tasksC = etTasksCompleted.getText().toString().trim();
        String kpi    = etKpiScore.getText().toString().trim();
        String att    = etAttendance.getText().toString().trim();
        String callsS = etTotalCalls  != null ? etTotalCalls.getText().toString().trim()   : "0";
        String salesS = etTotalSales  != null ? etTotalSales.getText().toString().trim()   : "0";
        String fbText = etFeedbackText != null ? etFeedbackText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(uid) || TextUtils.isEmpty(month)
                || TextUtils.isEmpty(year) || TextUtils.isEmpty(tasksA)
                || TextUtils.isEmpty(tasksC) || TextUtils.isEmpty(kpi)) {
            Toast.makeText(this, "All fields required except optional ones", Toast.LENGTH_SHORT).show();
            return;
        }

        double kpiScore, attendancePct;
        int assigned, completed, calls, sales;
        try {
            kpiScore      = Double.parseDouble(kpi);
            attendancePct = TextUtils.isEmpty(att)   ? 0 : Double.parseDouble(att);
            assigned      = Integer.parseInt(tasksA);
            completed     = Integer.parseInt(tasksC);
            calls         = TextUtils.isEmpty(callsS) ? 0 : Integer.parseInt(callsS);
            sales         = TextUtils.isEmpty(salesS) ? 0 : Integer.parseInt(salesS);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // AI Feedback Analysis
        String sentiment = AIFeedbackAnalyzer.analyzeSentiment(fbText);
        String accuracy  = AIFeedbackAnalyzer.calculateFeedbackAccuracy(sentiment, calls, sales);
        String insight   = AIFeedbackAnalyzer.generateInsight(sentiment, accuracy, kpiScore);

        pb.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        Map<String, Object> data = new HashMap<>();
        data.put("userId",               uid);
        data.put("employeeName",         name);
        data.put("month",                month);
        data.put("year",                 year);
        data.put("tasksAssigned",        assigned);
        data.put("tasksCompleted",       completed);
        data.put("kpiScore",             kpiScore);
        data.put("attendancePercentage", attendancePct);
        data.put("totalCalls",           calls);
        data.put("totalSales",           sales);
        data.put("feedbackSentiment",    sentiment);
        data.put("feedbackAccuracy",     accuracy);
        data.put("performanceInsight",   insight);
        data.put("timestamp",            System.currentTimeMillis());

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE).add(data)
            .addOnSuccessListener(r -> {
                pb.setVisibility(View.GONE);
                Toast.makeText(this,
                    "Saved! AI Sentiment: " + sentiment + " | Insight: " + insight,
                    Toast.LENGTH_LONG).show();
                finish();
            })
            .addOnFailureListener(e -> {
                pb.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}
