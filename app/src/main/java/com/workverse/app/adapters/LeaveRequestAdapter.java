package com.workverse.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.LeaveRequest;
import java.util.List;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.VH> {

    public interface Listener {
        void onApprove(LeaveRequest l);
        void onReject(LeaveRequest l);
    }

    private List<LeaveRequest> list;
    private final boolean showActions;
    private final Listener listener;

    public LeaveRequestAdapter(List<LeaveRequest> list, boolean showActions, Listener l) {
        this.list        = list;
        this.showActions = showActions;
        this.listener    = l;
    }

    public void updateList(List<LeaveRequest> nl) {
        list = nl;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leave_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        LeaveRequest r = list.get(pos);

        String name = r.getEmployeeName() != null && !r.getEmployeeName().isEmpty()
                ? r.getEmployeeName() : "Unknown";
        h.tvEmployeeName.setText(name);

        // Avatar initials
        String initials = name.trim().isEmpty() ? "?" : name.trim().substring(0, 1).toUpperCase();
        h.tvInitials.setText(initials);

        // Role badge
        if (r.getRole() != null && !r.getRole().isEmpty()) {
            h.tvRole.setVisibility(View.VISIBLE);
            h.tvRole.setText(r.getRole());
            if ("Manager".equalsIgnoreCase(r.getRole())) {
                h.tvRole.setBackgroundResource(R.drawable.bg_role_manager);
                h.tvRole.setTextColor(Color.parseColor("#6A1B9A"));
            } else {
                h.tvRole.setBackgroundResource(R.drawable.bg_role_employee);
                h.tvRole.setTextColor(Color.parseColor("#1565C0"));
            }
        } else {
            h.tvRole.setVisibility(View.GONE);
        }

        // Leave info
        h.tvLeaveType.setText(r.getLeaveType() != null ? r.getLeaveType() : "—");
        h.tvDateRange.setText((r.getFromDate() != null ? r.getFromDate() : "?")
                + "  →  " + (r.getToDate() != null ? r.getToDate() : "?"));

        // Designation / Campaign line
        String designation = r.getDesignation();
        String campaign = r.getCampaign();
        StringBuilder sub = new StringBuilder();
        if (designation != null && !designation.isEmpty()) sub.append(designation);
        if (campaign != null && !campaign.isEmpty()) {
            if (sub.length() > 0) sub.append(" · ");
            sub.append(campaign);
        }
        if (sub.length() > 0) {
            h.tvDesignation.setText(sub.toString());
            h.tvDesignation.setVisibility(View.VISIBLE);
        } else {
            h.tvDesignation.setVisibility(View.GONE);
        }

        h.tvReason.setText(r.getReason() != null ? r.getReason() : "—");

        // Status with color
        String status = r.getStatus() != null ? r.getStatus() : "Pending";
        h.tvStatus.setText(status);
        switch (status) {
            case "Approved":
                h.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
            case "Rejected":
                h.tvStatus.setTextColor(Color.parseColor("#C62828"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
            default: // Pending
                h.tvStatus.setTextColor(Color.parseColor("#E65100"));
                h.tvStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
        }

        // Show approve/reject buttons only when showActions=true AND status is Pending
        boolean showBtn = showActions && "Pending".equals(status);
        h.llActions.setVisibility(showBtn ? View.VISIBLE : View.GONE);

        if (showBtn && listener != null) {
            h.btnApprove.setOnClickListener(v -> listener.onApprove(r));
            h.btnReject.setOnClickListener(v -> listener.onReject(r));
        }
    }

    @Override
    public int getItemCount() { return list == null ? 0 : list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView    tvEmployeeName, tvRole, tvLeaveType, tvDateRange, tvDesignation, tvReason, tvStatus, tvInitials;
        LinearLayout llActions;
        Button       btnApprove, btnReject;

        VH(View v) {
            super(v);
            tvEmployeeName = v.findViewById(R.id.tvEmployeeName);
            tvRole         = v.findViewById(R.id.tvRole);
            tvLeaveType    = v.findViewById(R.id.tvLeaveType);
            tvDateRange    = v.findViewById(R.id.tvDateRange);
            tvDesignation  = v.findViewById(R.id.tvDesignation);
            tvReason       = v.findViewById(R.id.tvReason);
            tvStatus       = v.findViewById(R.id.tvStatus);
            llActions      = v.findViewById(R.id.llActions);
            btnApprove     = v.findViewById(R.id.btnApprove);
            btnReject      = v.findViewById(R.id.btnReject);
            tvInitials     = v.findViewById(R.id.tvInitials);
        }
    }
}