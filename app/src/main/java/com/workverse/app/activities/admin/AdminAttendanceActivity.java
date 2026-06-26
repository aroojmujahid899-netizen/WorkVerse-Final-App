package com.workverse.app.activities.admin;
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
import java.util.ArrayList;
import java.util.List;
public class AdminAttendanceActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    AttendanceAdapter adapter; List<Attendance> list = new ArrayList<>();
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_admin_attendance);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadData();
    }
    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ATTENDANCE)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    list.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Attendance a = d.toObject(Attendance.class); a.setId(d.getId()); list.add(a);
                    }
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(new ArrayList<>(list));
                })
                .addOnFailureListener(e -> { pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load attendance", Toast.LENGTH_SHORT).show(); });
    }
}