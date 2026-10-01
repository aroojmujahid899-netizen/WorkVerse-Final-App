package com.workverse.app.activities.admin;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.models.Role;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ManageRolesActivity extends AppCompatActivity {

    // Roles that can never be edited or deleted, matched case-insensitively.
    private static final java.util.Set<String> PROTECTED_ROLES =
            new java.util.HashSet<>(java.util.Arrays.asList("admin", "ceo"));

    RecyclerView rv; ProgressBar pb; TextView tvEmpty; FloatingActionButton fabAdd;
    TextInputEditText etSearch;
    RoleAdapter adapter;
    List<Role> fullList = new ArrayList<>();

    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_manage_roles);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        fabAdd = findViewById(R.id.fabAdd);
        etSearch = findViewById(R.id.etSearch);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoleAdapter(new ArrayList<>(), new RoleAdapter.Listener() {
            @Override public void onEdit(Role r) { showEditDialog(r); }
            @Override public void onDelete(Role r) { confirmDelete(r); }
            @Override public void onProtectedTap(Role r) {
                Toast.makeText(ManageRolesActivity.this,
                        r.getName() + " role can't be edited or deleted", Toast.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);

        if (fabAdd != null) fabAdd.setOnClickListener(v -> showAddDialog());

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) { filterRoles(s.toString()); }
            });
        }

        loadRoles();
    }

    @Override protected void onResume() { super.onResume(); loadRoles(); }

    private boolean isProtected(Role r) {
        return r.getName() != null && PROTECTED_ROLES.contains(r.getName().trim().toLowerCase(Locale.ROOT));
    }

    // Admin first, then CEO, then everything else in its existing order.
    private int rankOf(Role r) {
        if (r.getName() == null) return 2;
        String n = r.getName().trim().toLowerCase(Locale.ROOT);
        if (n.equals("admin")) return 0;
        if (n.equals("ceo")) return 1;
        return 2;
    }

    private void sortWithProtectedFirst(List<Role> roles) {
        roles.sort((a, b) -> rankOf(a) - rankOf(b));
    }

    private void filterRoles(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<Role> filtered = new ArrayList<>();
        if (q.isEmpty()) {
            filtered.addAll(fullList);
        } else {
            for (Role r : fullList) {
                if (r.getName() != null && r.getName().toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(r);
                }
            }
        }
        sortWithProtectedFirst(filtered);
        adapter.updateList(filtered);
        if (tvEmpty != null) tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadRoles() {
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_ROLES).get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Role r = d.toObject(Role.class); r.setId(d.getId()); fullList.add(r);
                    }
                    pb.setVisibility(View.GONE);
                    if (fullList.isEmpty()) {
                        seedDefaultRoles();
                    } else {
                        String currentQuery = etSearch != null && etSearch.getText() != null
                                ? etSearch.getText().toString() : "";
                        filterRoles(currentQuery);
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
                    fullList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Role r = d.toObject(Role.class); r.setId(d.getId()); fullList.add(r);
                    }
                    filterRoles(etSearch != null && etSearch.getText() != null ? etSearch.getText().toString() : "");
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
        if (isProtected(role)) {
            Toast.makeText(this, role.getName() + " role can't be edited or deleted", Toast.LENGTH_SHORT).show();
            return;
        }
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
        if (isProtected(role)) {
            Toast.makeText(this, role.getName() + " role can't be edited or deleted", Toast.LENGTH_SHORT).show();
            return;
        }
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

    // Adapter for roles — Edit/Delete disabled for Admin and CEO.
    static class RoleAdapter extends RecyclerView.Adapter<RoleAdapter.VH> {
        interface Listener {
            void onEdit(Role r);
            void onDelete(Role r);
            void onProtectedTap(Role r);
        }
        private List<Role> list;
        private final Listener listener;
        RoleAdapter(List<Role> l, Listener listener) { this.list = l; this.listener = listener; }
        void updateList(List<Role> nl) { list = nl; notifyDataSetChanged(); }

        private boolean isProtected(Role r) {
            return r.getName() != null &&
                    (r.getName().trim().equalsIgnoreCase("Admin") || r.getName().trim().equalsIgnoreCase("CEO"));
        }

        @Override public VH onCreateViewHolder(ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_role, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            Role r = list.get(pos);
            h.tvRoleName.setText(r.getName());

            boolean protectedRole = isProtected(r);
            float alpha = protectedRole ? 0.35f : 1f;
            h.ivEdit.setAlpha(alpha);
            h.ivDelete.setAlpha(alpha);

            if (protectedRole) {
                h.ivEdit.setOnClickListener(v -> listener.onProtectedTap(r));
                h.ivDelete.setOnClickListener(v -> listener.onProtectedTap(r));
            } else {
                h.ivEdit.setOnClickListener(v -> listener.onEdit(r));
                h.ivDelete.setOnClickListener(v -> listener.onDelete(r));
            }
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