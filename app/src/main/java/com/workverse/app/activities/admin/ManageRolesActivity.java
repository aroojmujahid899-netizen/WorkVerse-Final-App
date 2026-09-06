package com.workverse.app.activities.admin;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    RecyclerView rv; ProgressBar pb; TextView tvEmpty; FloatingActionButton fabAdd;
    RoleAdapter adapter; List<Role> list = new ArrayList<>();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manage_roles);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAdd = findViewById(R.id.fabAdd);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoleAdapter(new ArrayList<>(), new RoleAdapter.Listener() {
            @Override public void onEdit(Role r) { showEditDialog(r); }
            @Override public void onDelete(Role r) { confirmDelete(r); }
        });
        rv.setAdapter(adapter);
        if (fabAdd != null) fabAdd.setOnClickListener(v -> showAddDialog());
        loadRoles();
    }

    @Override protected void onResume() { super.onResume(); loadRoles(); }

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

    private void showAddDialog() {
        EditText etName = new EditText(this); etName.setHint("Role name (e.g. Supervisor)");
        EditText etDesc = new EditText(this); etDesc.setHint("Description");
        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);
        ll.addView(etName); ll.addView(etDesc);
        new AlertDialog.Builder(this).setTitle("Add Role").setView(ll)
                .setPositiveButton("Add", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Role name required", Toast.LENGTH_SHORT).show(); return;
                    }
                    Role role = new Role(name, desc);
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_ROLES).add(role)
                            .addOnSuccessListener(r -> { Toast.makeText(this, "Role added", Toast.LENGTH_SHORT).show(); loadRoles(); })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showEditDialog(Role role) {
        EditText etName = new EditText(this); etName.setHint("Role name"); etName.setText(role.getName());
        EditText etDesc = new EditText(this); etDesc.setHint("Description"); etDesc.setText(role.getDescription());
        android.widget.LinearLayout ll = new android.widget.LinearLayout(this);
        ll.setOrientation(android.widget.LinearLayout.VERTICAL);
        ll.setPadding(48, 24, 48, 24);
        ll.addView(etName); ll.addView(etDesc);
        new AlertDialog.Builder(this).setTitle("Edit Role").setView(ll)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Role name required", Toast.LENGTH_SHORT).show(); return;
                    }
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_ROLES).document(role.getId())
                            .update("name", name, "description", desc)
                            .addOnSuccessListener(r -> { Toast.makeText(this, "Role updated", Toast.LENGTH_SHORT).show(); loadRoles(); })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void confirmDelete(Role role) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Role")
                .setMessage("Delete \"" + role.getName() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_ROLES).document(role.getId())
                            .delete()
                            .addOnSuccessListener(r -> { Toast.makeText(this, "Role deleted", Toast.LENGTH_SHORT).show(); loadRoles(); })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null).show();
    }

    // Adapter for roles — now with working Edit / Delete
    static class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.VH> {
        interface Listener { void onEdit(Role r); void onDelete(Role r); }
        private List<Role> list;
        private final Listener listener;
        RoleAdapter(List<Role> l, Listener listener) { this.list = l; this.listener = listener; }
        void updateList(List<Role> nl) { list = nl; notifyDataSetChanged(); }
        @Override public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_role, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            Role r = list.get(pos);
            h.tvRoleName.setText(r.getName());
            h.ivEdit.setOnClickListener(v -> listener.onEdit(r));
            h.ivDelete.setOnClickListener(v -> listener.onDelete(r));
        }
        @Override public int getItemCount() { return list.size(); }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvRoleName; ImageView ivEdit, ivDelete;
            VH(View v) {
                super(v);
                tvRoleName = v.findViewById(R.id.tvRoleName);
                ivEdit = v.findViewById(R.id.ivEdit);
                ivDelete = v.findViewById(R.id.ivDelete);
            }
        }
    }
}
