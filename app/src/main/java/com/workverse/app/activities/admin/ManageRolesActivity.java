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
import com.workverse.app.models.Role;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
public class ManageRolesActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty;
    RoleAdapter adapter; List<Role> list = new ArrayList<>();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manage_roles);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoleAdapter(new ArrayList<>());
        rv.setAdapter(adapter);
        loadRoles();
    }

    private void loadRoles() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ROLES).get()
                .addOnSuccessListener(snap -> {
                    list.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Role r = d.toObject(Role.class); r.setId(d.getId()); list.add(r);
                    }
                    pb.setVisibility(View.GONE);
                    if (list.isEmpty()) {
                        seedDefaultRoles();
                    } else {
                        if (tvEmpty != null) tvEmpty.setVisibility(View.GONE);
                        adapter.updateList(new ArrayList<>(list));
                    }
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load roles", Toast.LENGTH_SHORT).show();
                });
    }

    private void seedDefaultRoles() {
        String[] roles = {"Admin", "Manager", "Employee"};
        for (String r : roles) {
            Role role = new Role(r, "Default " + r + " role");
            FirebaseHelper.getDb().collection(FirebaseHelper.COL_ROLES).add(role);
        }
        Toast.makeText(this, "Default roles initialized", Toast.LENGTH_SHORT).show();
        // reload after seeding
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ROLES).get()
                .addOnSuccessListener(snap -> {
                    list.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Role r = d.toObject(Role.class); r.setId(d.getId()); list.add(r);
                    }
                    adapter.updateList(new ArrayList<>(list));
                    if (tvEmpty != null) tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // Inner adapter for roles
    static class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.VH> {
        private List<Role> list;
        RoleAdapter(List<Role> l) { this.list = l; }
        void updateList(List<Role> nl) { list = nl; notifyDataSetChanged(); }
        @Override public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_role, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            h.tvRoleName.setText(list.get(pos).getName());
        }
        @Override public int getItemCount() { return list.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvRoleName;
            VH(View v) { super(v); tvRoleName = v.findViewById(R.id.tvRoleName); }
        }
    }
}