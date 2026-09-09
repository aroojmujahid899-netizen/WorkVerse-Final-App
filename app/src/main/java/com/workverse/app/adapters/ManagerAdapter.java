package com.workverse.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.Manager;
import java.util.List;

public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.VH> {

    public interface Listener {
        void onEdit(Manager m);
        void onDelete(Manager m);
        void onClick(Manager m);
    }

    private List<Manager> list;
    private Listener listener;
    private boolean viewOnly;

    public ManagerAdapter(List<Manager> list, Listener listener) {
        this(list, false, listener);
    }

    public ManagerAdapter(List<Manager> list, boolean viewOnly, Listener listener) {
        this.list = list;
        this.listener = listener;
        this.viewOnly = viewOnly;
    }

    public void updateList(List<Manager> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manager, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Manager m = list.get(position);

        String name = m.getName() != null ? m.getName() : (m.getFullName() != null ? m.getFullName() : "");
        h.tvName.setText(name);

        if (h.tvDesignation != null) {
            h.tvDesignation.setText(m.getDesignation() != null ? m.getDesignation() : "");
        }

        if (h.tvDepartment != null) {
            h.tvDepartment.setText(m.getCampaign() != null ? m.getCampaign() : "");
        }

        if (viewOnly) {
            if (h.btnEdit != null) h.btnEdit.setVisibility(View.GONE);
            if (h.btnDelete != null) h.btnDelete.setVisibility(View.GONE);
        } else {
            if (h.btnEdit != null) {
                h.btnEdit.setVisibility(View.VISIBLE);
                h.btnEdit.setOnClickListener(v -> {
                    if (listener != null) listener.onEdit(m);
                });
            }
            if (h.btnDelete != null) {
                h.btnDelete.setVisibility(View.VISIBLE);
                h.btnDelete.setOnClickListener(v -> {
                    if (listener != null) listener.onDelete(m);
                });
            }
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(m);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvDesignation, tvDepartment;
        ImageView btnEdit, btnDelete;

        public VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvDesignation = v.findViewById(R.id.tvDesignation);
            tvDepartment = v.findViewById(R.id.tvDepartment);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}