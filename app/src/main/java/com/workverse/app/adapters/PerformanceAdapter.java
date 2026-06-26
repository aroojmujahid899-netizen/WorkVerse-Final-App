package com.workverse.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.PerformanceReport;
import java.util.List;

public class PerformanceAdapter extends RecyclerView.Adapter<PerformanceAdapter.VH> {

    private List<PerformanceReport> list;

    public PerformanceAdapter(List<PerformanceReport> l) { this.list = l; }
    public void updateList(List<PerformanceReport> nl)   { list = nl; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_performance, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PerformanceReport r = list.get(pos);

        if (h.tvEmployeeName != null)
            h.tvEmployeeName.setText(r.getEmployeeName() != null ? r.getEmployeeName() : "—");

        if (h.tvMonth != null)
            h.tvMonth.setText((r.getMonth() != null ? r.getMonth() : "") + " " + (r.getYear() != null ? r.getYear() : ""));

        if (h.tvKpi != null)
            h.tvKpi.setText(String.format("%.0f%%", r.getKpiScore()));

        // Color KPI score
        if (h.tvKpi != null) {
            double kpi = r.getKpiScore();
            if      (kpi >= 80) h.tvKpi.setTextColor(Color.parseColor("#2E7D32")); // green
            else if (kpi >= 50) h.tvKpi.setTextColor(Color.parseColor("#E65100")); // orange
            else                h.tvKpi.setTextColor(Color.parseColor("#C62828")); // red
        }

        // Tasks
        if (h.tvTasks != null)
            h.tvTasks.setText("Tasks: " + r.getTasksCompleted() + "/" + r.getTasksAssigned());

        // Attendance
        if (h.tvAttendance != null)
            h.tvAttendance.setText("Attendance: " + (int) r.getAttendancePercentage() + "%");

        // AI Feedback Sentiment badge
        if (h.tvSentiment != null) {
            String sent = r.getFeedbackSentiment();
            if (sent != null && !sent.isEmpty()) {
                h.tvSentiment.setText("AI: " + sent);
                h.tvSentiment.setVisibility(View.VISIBLE);
                switch (sent) {
                    case "Positive": h.tvSentiment.setBackgroundColor(Color.parseColor("#2E7D32")); break;
                    case "Negative": h.tvSentiment.setBackgroundColor(Color.parseColor("#C62828")); break;
                    default:         h.tvSentiment.setBackgroundColor(Color.parseColor("#E65100")); break;
                }
                h.tvSentiment.setTextColor(Color.WHITE);
            } else {
                h.tvSentiment.setVisibility(View.GONE);
            }
        }

        // AI Insight
        if (h.tvInsight != null) {
            String insight = r.getPerformanceInsight();
            if (insight != null && !insight.isEmpty()) {
                h.tvInsight.setText(insight);
                h.tvInsight.setVisibility(View.VISIBLE);
            } else {
                h.tvInsight.setVisibility(View.GONE);
            }
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvMonth, tvKpi, tvTasks, tvAttendance, tvSentiment, tvInsight;
        VH(View v) {
            super(v);
            tvEmployeeName = v.findViewById(R.id.tvEmployeeName);
            tvMonth        = v.findViewById(R.id.tvMonth);
            tvKpi          = v.findViewById(R.id.tvKpi);
            tvTasks        = v.findViewById(R.id.tvTasks);
            tvAttendance   = v.findViewById(R.id.tvAttendance);
            tvSentiment    = v.findViewById(R.id.tvSentiment);
            tvInsight      = v.findViewById(R.id.tvInsight);
        }
    }
}
