package com.workverse.app.activities.ceo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.workverse.app.R;

public class CEOAllReportsActivity extends AppCompatActivity {

    LinearLayout cardPerformanceReports, cardSalesReports,
            cardAttendanceReports, cardFeedbackReports;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_all_reports);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("All Reports");
        tb.setNavigationOnClickListener(v -> finish());

        cardPerformanceReports = findViewById(R.id.cardPerformanceReports);
        cardSalesReports       = findViewById(R.id.cardSalesReports);
        cardAttendanceReports  = findViewById(R.id.cardAttendanceReports);
        cardFeedbackReports    = findViewById(R.id.cardFeedbackReports);

        cardPerformanceReports.setOnClickListener(v -> go(CEOPerformanceActivity.class));
        cardSalesReports.setOnClickListener(v -> go(CEOSalesReportActivity.class));
        cardAttendanceReports.setOnClickListener(v -> go(CEOAttendanceActivity.class));
        cardFeedbackReports.setOnClickListener(v -> go(CEOFeedbackActivity.class));
    }

    private void go(Class<?> c) {
        startActivity(new Intent(this, c));
    }
}