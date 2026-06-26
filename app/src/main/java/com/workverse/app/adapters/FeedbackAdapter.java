package com.workverse.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.Feedback;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.VH> {
    private List<Feedback> list;
    public FeedbackAdapter(List<Feedback> list) { this.list = list; }
    public void updateList(List<Feedback> l) { list = l; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_feedback, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Feedback f = list.get(i);
        if (h.tvName    != null) h.tvName.setText(f.getFromUserName() != null ? f.getFromUserName() : "—");
        if (h.tvTitle   != null) h.tvTitle.setText(f.getTitle() != null ? f.getTitle() : "");
        if (h.tvMessage != null) h.tvMessage.setText(f.getMessage() != null ? f.getMessage() : "");
        if (h.tvDate    != null) {
            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(new Date(f.getTimestamp()));
            h.tvDate.setText(date);
        }
        // AI Sentiment Badge
        if (h.tvSentiment != null) {
            String sent = f.getSentiment();
            if (sent == null || sent.isEmpty()) sent = "Neutral";
            h.tvSentiment.setText(sent);
            switch (sent) {
                case "Positive": h.tvSentiment.setBackgroundColor(Color.parseColor("#2E7D32")); break;
                case "Negative": h.tvSentiment.setBackgroundColor(Color.parseColor("#C62828")); break;
                default:         h.tvSentiment.setBackgroundColor(Color.parseColor("#E65100")); break;
            }
            h.tvSentiment.setTextColor(Color.WHITE);
            h.tvSentiment.setVisibility(View.VISIBLE);
        }
        // AI Insight
        if (h.tvInsight != null) {
            String insight = f.getInsight();
            if (insight != null && !insight.isEmpty()) {
                h.tvInsight.setText("AI: " + insight);
                h.tvInsight.setVisibility(View.VISIBLE);
            } else {
                h.tvInsight.setVisibility(View.GONE);
            }
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvTitle, tvMessage, tvDate, tvSentiment, tvInsight;
        VH(View v) {
            super(v);
            tvName      = v.findViewById(R.id.tvFromUser);
            tvTitle     = v.findViewById(R.id.tvTitle);
            tvMessage   = v.findViewById(R.id.tvMessage);
            tvDate      = v.findViewById(R.id.tvDate);
            tvSentiment = v.findViewById(R.id.tvSentiment);
            tvInsight   = v.findViewById(R.id.tvInsight);
        }
    }
}
