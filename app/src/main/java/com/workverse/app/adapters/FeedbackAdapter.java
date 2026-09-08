package com.workverse.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.workverse.app.R;
import com.workverse.app.models.Feedback;
import com.workverse.app.utils.DateTimeUtils;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.VH> {

    private List<Feedback> list;

    public FeedbackAdapter(List<Feedback> l) {
        this.list = l;
    }

    public void updateList(List<Feedback> nl) {
        this.list = nl;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_feedback, p, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Feedback f = list.get(pos);

        // Employee Name
        String name = f.getFromUserName() != null ? f.getFromUserName() : "Employee";
        h.tvEmployeeName.setText(name);

        // Avatar Initial & Dynamic Color Logic
        if (!name.isEmpty()) {
            h.tvAvatar.setText(name.substring(0, 1).toUpperCase());
            h.tvAvatar.setTextColor(Color.WHITE);

            int[] avatarColors = {
                    Color.parseColor("#1E88E5"), // Blue
                    Color.parseColor("#43A047"), // Green
                    Color.parseColor("#8E24AA"), // Purple
                    Color.parseColor("#FB8C00"), // Orange
                    Color.parseColor("#00ACC1")  // Teal
            };

            int colorIndex = Math.abs(name.hashCode()) % avatarColors.length;
            GradientDrawable avatarBg = new GradientDrawable();
            avatarBg.setShape(GradientDrawable.OVAL);
            avatarBg.setColor(avatarColors[colorIndex]);
            h.tvAvatar.setBackground(avatarBg);
        }

        h.tvTitle.setText(f.getTitle());
        h.tvMessage.setText(f.getMessage());
        h.tvDate.setText(DateTimeUtils.getTimeAgo(f.getTimestamp()));

        // Auto Sentiment Logic
        String message = f.getMessage() != null ? f.getMessage().toLowerCase() : "";
        String sentiment = f.getSentiment();

        if (sentiment == null || sentiment.isEmpty()) {
            if (message.contains("poor") || message.contains("crash") || message.contains("terrible") || message.contains("failed") || message.contains("unhappy")) {
                sentiment = "Negative";
            } else if (message.contains("low") || message.contains("delay") || message.contains("disappointing")) {
                sentiment = "Neutral";
            } else {
                sentiment = "Positive";
            }
        }

        h.tvSentimentBadge.setText(sentiment);

        int badgeColor;
        switch (sentiment) {
            case "Positive":
                badgeColor = Color.parseColor("#2E7D32");
                break;
            case "Negative":
                badgeColor = Color.parseColor("#C62828");
                break;
            default:
                badgeColor = Color.parseColor("#E65100");
                break;
        }

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(24f);
        badgeBg.setColor(badgeColor);
        h.tvSentimentBadge.setBackground(badgeBg);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvEmployeeName, tvTitle, tvMessage, tvDate, tvSentimentBadge;

        VH(@NonNull View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            tvEmployeeName = v.findViewById(R.id.tvEmployeeName);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvDate = v.findViewById(R.id.tvDate);
            tvSentimentBadge = v.findViewById(R.id.tvSentimentBadge);
        }
    }
}