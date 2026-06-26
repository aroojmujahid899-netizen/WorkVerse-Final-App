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
import com.workverse.app.adapters.LeaveRequestAdapter;
import com.workverse.app.models.LeaveRequest;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
public class ViewLeaveStatusActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    LeaveRequestAdapter adapter;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_view_leave_status);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView); pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaveRequestAdapter(new ArrayList<>(), false, null);
        rv.setAdapter(adapter);
        loadData();
    }
    @Override protected void onResume() { super.onResume(); loadData(); }
    private void loadData() {
        pb.setVisibility(View.VISIBLE);
        String uid = SharedPrefManager.getInstance(this).getUid();
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_LEAVES)
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<LeaveRequest> list = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) {
                        LeaveRequest r = d.toObject(LeaveRequest.class); r.setId(d.getId()); list.add(r);
                    }
                    pb.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(list);
                })
                .addOnFailureListener(e -> { pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show(); });
    }
}