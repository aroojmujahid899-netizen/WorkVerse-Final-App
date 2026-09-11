package com.workverse.app.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.Feedback;
import com.workverse.app.utils.AIFeedbackAnalyzer;
import com.workverse.app.utils.DateTimeUtils;
import java.util.ArrayList;
import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.VH> {

    public interface OnRetryClickListener {
        void onRetry(Feedback feedback);
    }

    private List<Feedback> list;
    private final OnRetryClickListener retryListener;

    public FeedbackAdapter(List<Feedback> l) {
        this(l, null);
    }

    public FeedbackAdapter(List<Feedback> l, OnRetryClickListener retryListener) {
        this.list = l != null ? l : new ArrayList<>();
        this.retryListener = retryListener;
    }

    public void updateList(List<Feedback> nl) {
        this.list = nl != null ? nl : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_feedback, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Feedback f = list.get(pos);

        if (h.tvFrom != null) {
            h.tvFrom.setText(f.getFromUserName() != null ? "From: " + f.getFromUserName() : "From: Anonymous");
        }
        if (h.tvTitle != null) {
            h.tvTitle.setText(f.getTitle() != null ? f.getTitle() : "Feedback");
        }
        if (h.tvMessage != null) {
            h.tvMessage.setText(f.getMessage() != null ? f.getMessage() : "");
        }
        if (h.tvDate != null) {
            h.tvDate.setText(DateTimeUtils.getTimeAgo(f.getTimestamp()));
        }

        String status = f.getAiStatus() != null ? f.getAiStatus() : "pending";
        String badgeText;
        int color;

        if ("completed".equals(status) && f.getAiScore() != null) {
            String sentiment = f.getAiSentiment() != null ? f.getAiSentiment() : "Neutral";
            badgeText = sentiment;
            switch (sentiment) {
                case "Positive":
                    color = Color.parseColor("#2E7D32");
                    break;
                case "Negative":
                    color = Color.parseColor("#C62828");
                    break;
                default:
                    color = Color.parseColor("#E65100");
                    break;
            }
            if (h.tvAiScore != null) {
                h.tvAiScore.setVisibility(View.VISIBLE);
                int score = 0;
                if (f.getAiScore() instanceof Number) {
                    score = ((Number) f.getAiScore()).intValue();
                } else if (f.getAiScore() != null) {
                    try {
                        score = Integer.parseInt(String.valueOf(f.getAiScore()));
                    } catch (Exception e) {}
                }
                h.tvAiScore.setText("AI Score: " + score + "/100 (" + AIFeedbackAnalyzer.getScoreBand(score) + ")");
            }
            if (h.tvAiSummary != null) {
                if (f.getAiSummary() != null && !f.getAiSummary().trim().isEmpty()) {
                    h.tvAiSummary.setVisibility(View.VISIBLE);
                    h.tvAiSummary.setText(f.getAiSummary());
                } else {
                    h.tvAiSummary.setVisibility(View.GONE);
                }
            }
            if (h.btnRetry != null) h.btnRetry.setVisibility(View.GONE);

        } else if ("processing".equals(status)) {
            badgeText = "Analyzing…";
            color = Color.parseColor("#616161");
            if (h.tvAiScore != null) h.tvAiScore.setVisibility(View.GONE);
            if (h.tvAiSummary != null) h.tvAiSummary.setVisibility(View.GONE);
            if (h.btnRetry != null) h.btnRetry.setVisibility(View.GONE);

        } else if ("failed".equals(status)) {
            badgeText = "AI Failed";
            color = Color.parseColor("#C62828");
            if (h.tvAiScore != null) h.tvAiScore.setVisibility(View.GONE);
            if (h.tvAiSummary != null) h.tvAiSummary.setVisibility(View.GONE);
            if (h.btnRetry != null) {
                h.btnRetry.setVisibility(retryListener != null ? View.VISIBLE : View.GONE);
                h.btnRetry.setOnClickListener(v -> {
                    if (retryListener != null) retryListener.onRetry(f);
                });
            }
        } else {
            badgeText = "Pending";
            color = Color.parseColor("#E65100");
            if (h.tvAiScore != null) h.tvAiScore.setVisibility(View.GONE);
            if (h.tvAiSummary != null) h.tvAiSummary.setVisibility(View.GONE);
            if (h.btnRetry != null) h.btnRetry.setVisibility(View.GONE);
        }

        if (h.tvSentiment != null) {
            h.tvSentiment.setText(badgeText);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(24f);
            bg.setColor(color);
            h.tvSentiment.setBackground(bg);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvFrom, tvTitle, tvMessage, tvDate, tvSentiment, tvAiScore, tvAiSummary;
        Button btnRetry;

        VH(View v) {
            super(v);
            tvFrom = v.findViewById(R.id.tvFrom);
            tvTitle = v.findViewById(R.id.tvTitle);
            // Mismatch fixed: XML ID is tvMsg, mapped correctly to tvMessage here
            tvMessage = v.findViewById(R.id.tvMsg);
            tvDate = v.findViewById(R.id.tvDate);
            tvSentiment = v.findViewById(R.id.tvSentiment);
            tvAiScore = v.findViewById(R.id.tvAiScore);
            tvAiSummary = v.findViewById(R.id.tvAiSummary);
            btnRetry = v.findViewById(R.id.btnRetryAnalysis);
        }
    }
}