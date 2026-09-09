package com.workverse.app.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.workverse.app.adapters.ManagerAdapter;
import com.workverse.app.models.Manager;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;

public class ViewManagersActivity extends AppCompatActivity {

    RecyclerView rv;
    ProgressBar pb;
    TextView tvEmpty;
    TextInputEditText etSearch;
    FloatingActionButton fab;
    ManagerAdapter adapter;
    List<Manager> allList = new ArrayList<>();
    boolean isViewOnly = false;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_view_managers);

        String role = SharedPrefManager.getInstance(this).getRole();
        isViewOnly = "CEO".equalsIgnoreCase(role);

        Toolbar tb = findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
            tb.setNavigationOnClickListener(v -> finish());
        }

        rv = findViewById(R.id.recyclerView);
        pb = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        fab = findViewById(R.id.fabAdd);

        if (isViewOnly && fab != null) {
            fab.setVisibility(View.GONE);
        }

        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ManagerAdapter(new ArrayList<>(), isViewOnly, new ManagerAdapter.Listener() {
            @Override
            public void onEdit(Manager m) {
                Intent i = new Intent(ViewManagersActivity.this, AddManagerActivity.class);
                i.putExtra("manager_id", m.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Manager m) {
                if (m.getId() != null) {
                    FirebaseHelper.getDb().collection(FirebaseHelper.COL_MANAGERS)
                            .document(m.getId()).delete()
                            .addOnSuccessListener(r -> {
                                Toast.makeText(ViewManagersActivity.this, "Manager deleted", Toast.LENGTH_SHORT).show();
                                loadManagers();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ViewManagersActivity.this, "Delete failed", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onClick(Manager m) { }
        });

        rv.setAdapter(adapter);

        if (fab != null) {
            fab.setOnClickListener(v -> startActivity(new Intent(this, AddManagerActivity.class)));
        }

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence c, int a, int b, int d) {}
                public void onTextChanged(CharSequence c, int a, int b, int d) { filter(c.toString()); }
                public void afterTextChanged(Editable e) {}
            });
        }

        loadManagers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadManagers();
    }

    private void loadManagers() {
        if (pb != null) pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_MANAGERS).get()
                .addOnSuccessListener(snap -> {
                    allList.clear();
                    for (QueryDocumentSnapshot d : snap) {
                        Manager m = d.toObject(Manager.class);
                        if (m != null) {
                            m.setId(d.getId());
                            allList.add(m);
                        }
                    }
                    if (pb != null) pb.setVisibility(View.GONE);
                    if (tvEmpty != null) tvEmpty.setVisibility(allList.isEmpty() ? View.VISIBLE : View.GONE);
                    adapter.updateList(new ArrayList<>(allList));
                })
                .addOnFailureListener(e -> {
                    if (pb != null) pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load managers", Toast.LENGTH_SHORT).show();
                });
    }

    private void filter(String q) {
        if (q.trim().isEmpty()) {
            adapter.updateList(new ArrayList<>(allList));
            return;
        }
        List<Manager> filtered = new ArrayList<>();
        String query = q.toLowerCase().trim();
        for (Manager m : allList) {
            boolean nameMatch = m.getName() != null && m.getName().toLowerCase().contains(query);
            boolean desigMatch = m.getDesignation() != null && m.getDesignation().toLowerCase().contains(query);
            boolean campaignMatch = m.getCampaign() != null && m.getCampaign().toLowerCase().contains(query);

            if (nameMatch || desigMatch || campaignMatch) {
                filtered.add(m);
            }
        }
        adapter.updateList(filtered);
    }
}