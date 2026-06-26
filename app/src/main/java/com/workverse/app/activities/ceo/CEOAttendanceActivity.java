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
import com.workverse.app.adapters.AttendanceAdapter;
import com.workverse.app.models.Attendance;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class CEOAttendanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb;
    TextView tvPresentCount, tvAbsentCount, tvEmpty;
    AttendanceAdapter adapter;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_attendance);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Attendance Summary");
        tb.setNavigationOnClickListener(v -> finish());

        rv             = findViewById(R.id.recyclerView);
        pb             = findViewById(R.id.progressBar);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount  = findViewById(R.id.tvAbsentCount);
        tvEmpty        = findViewById(R.id.tvEmpty);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        String today = DateTimeUtils.getCurrentDate();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
            .whereEqualTo("date", today).get()
            .addOnSuccessListener(snap -> {
                List<Attendance> list = new ArrayList<>();
                int present = 0, absent = 0;
                for (QueryDocumentSnapshot d : snap) {
                    Attendance a = d.toObject(Attendance.class);
                    a.setId(d.getId()); list.add(a);
                    if ("Present".equals(a.getStatus())) present++;
                    else absent++;
                }
                pb.setVisibility(View.GONE);
                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                if (tvPresentCount != null) tvPresentCount.setText(String.valueOf(present));
                if (tvAbsentCount != null) tvAbsentCount.setText(String.valueOf(absent));
                adapter.updateList(list);
            })
            .addOnFailureListener(e -> {
                pb.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            });
    }
}
