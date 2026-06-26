package com.workverse.app.activities.employee;
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
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
public class EmployeePerformanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvStat1, tvStat2, tvEmpty;
    PerformanceAdapter adapter;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_employee_performance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv      = findViewById(R.id.recyclerView);
        pb      = findViewById(R.id.progressBar);
        tvStat1 = findViewById(R.id.tvStat1);
        tvStat2 = findViewById(R.id.tvStat2);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerformanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
    }
    @Override protected void onResume() { super.onResume(); loadData(); }
    private void loadData() {
        if (pb != null) pb.setVisibility(View.VISIBLE);
        String uid = SharedPrefManager.getInstance(this).getUid();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE)
                .whereEqualTo("userId", uid).get()
                .addOnSuccessListener(snap -> {
                    List<PerformanceReport> list = new ArrayList<>();
                    double totalKpi = 0; int totalTasks = 0;
                    for (QueryDocumentSnapshot d : snap) {
                        PerformanceReport r = d.toObject(PerformanceReport.class);
                        r.setId(d.getId()); list.add(r);
                        totalKpi   += r.getKpiScore();
                        totalTasks += r.getTasksCompleted();
                    }
                    if (pb != null) pb.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    double avgKpi = list.isEmpty() ? 0 : totalKpi / list.size();
                    if (tvStat1 != null) tvStat1.setText(String.format("%.0f%%", avgKpi));
                    if (tvStat2 != null) tvStat2.setText(String.valueOf(totalTasks));
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    if (pb != null) pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
                });
    }
}