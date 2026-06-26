package com.workverse.app.activities.manager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.activities.admin.AddPerformanceActivity;
import com.workverse.app.adapters.PerformanceAdapter;
import com.workverse.app.models.PerformanceReport;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;
public class ManagerPerformanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    FloatingActionButton fab; PerformanceAdapter adapter;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manager_performance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv  = findViewById(R.id.recyclerView);
        pb  = findViewById(R.id.progressBar);
        fab = findViewById(R.id.fabAdd);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PerformanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        if (fab != null) fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddPerformanceActivity.class)));
        loadData();
    }
    @Override protected void onResume() { super.onResume(); loadData(); }
    private void loadData() {
        if (pb != null) pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_PERFORMANCE).get()
                .addOnSuccessListener(snap -> {
                    List<PerformanceReport> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        PerformanceReport r = d.toObject(PerformanceReport.class);
                        r.setId(d.getId()); list.add(r);
                    }
                    if (pb != null) pb.setVisibility(View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    if (pb != null) pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                });
    }
}