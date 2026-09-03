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
import com.workverse.app.adapters.PerformanceAdapter;
import com.workverse.app.models.PerformanceReport;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class CEOPerformanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    TextView tvAvgKpi, tvTotalRecords, tvNeedsImprovement, tvEmpty;
    PerformanceAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_performance);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Overall KPI Reports");
        tb.setNavigationOnClickListener(v -> finish());

        rv                 = findViewById(R.id.recyclerView);
        pb                 = findViewById(R.id.progressBar);
        tvAvgKpi           = findViewById(R.id.tvAvgKpi);
        tvTotalRecords     = findViewById(R.id.tvTotalRecords);
        tvNeedsImprovement = findViewById(R.id.tvNeedsImprovement);
        tvEmpty            = findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerformanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
    }

    @Override
    protected void onResume() { super.onResume(); loadData(); }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE).get()
                .addOnSuccessListener(snap -> {
                    List<PerformanceReport> list = new ArrayList<>();
                    double totalKpi = 0;
                    int needsImprovement = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        PerformanceReport r = d.toObject(PerformanceReport.class);
                        r.setId(d.getId()); list.add(r);
                        totalKpi += r.getKpiScore();
                        if (r.getKpiScore() < 70) needsImprovement++;
                    }
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    double avg = list.isEmpty() ? 0 : totalKpi / list.size();
                    if (tvAvgKpi != null) tvAvgKpi.setText(String.format("%.0f%%", avg));
                    if (tvTotalRecords != null) tvTotalRecords.setText(String.valueOf(list.size()));
                    if (tvNeedsImprovement != null) tvNeedsImprovement.setText(String.valueOf(needsImprovement));
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }
}
