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
import com.workverse.app.adapters.AttendanceAdapter;
import com.workverse.app.models.Attendance;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewAttendanceActivity extends AppCompatActivity {

    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    AttendanceAdapter adapter;
    // Check-in timestamp per record id (0 for old records that have no timestamp)
    Map<String, Long> timestampMap = new HashMap<>();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_view_attendance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
    }

    // Newest date first, then newest check-in first. Done in Java (not in the
    // Firestore query) so records without a "timestamp" field are not dropped.
    private void sortNewestFirst(List<Attendance> list) {
        Collections.sort(list, (x, y) -> {
            String dx = x.getDate() != null ? x.getDate() : "";
            String dy = y.getDate() != null ? y.getDate() : "";
            int byDate = dy.compareTo(dx);
            if (byDate != 0) return byDate;
            Long tx = timestampMap.get(x.getId());
            Long ty = timestampMap.get(y.getId());
            long lx = tx != null ? tx : 0L;
            long ly = ty != null ? ty : 0L;
            return Long.compare(ly, lx);
        });
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        String uid = SharedPrefManager.getInstance(this).getUid();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Attendance> list = new ArrayList<>();
                    timestampMap.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Attendance a = d.toObject(Attendance.class);
                        a.setId(d.getId());
                        Long ts = d.getLong("timestamp");
                        timestampMap.put(d.getId(), ts != null ? ts : 0L);
                        list.add(a);
                    }
                    sortNewestFirst(list);
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    android.util.Log.e("ViewAttendance", "Failed to load", e);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                });
    }
}