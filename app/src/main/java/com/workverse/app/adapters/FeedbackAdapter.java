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
    public FeedbackAdapter(List<Feedback> l){this.list=l;}
    public void updateList(List<Feedback> nl){list=nl;notifyDataSetChanged();}
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_feedback,p,false));}
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Feedback f=list.get(pos);
        h.tvFrom.setText("From: "+f.getFromUserName());
        h.tvTitle.setText(f.getTitle());
        h.tvMessage.setText(f.getMessage());
        h.tvDate.setText(DateTimeUtils.getTimeAgo(f.getTimestamp()));

        String sentiment = f.getSentiment() != null ? f.getSentiment() : "Neutral";
        h.tvSentiment.setText(sentiment);
        int color;
        switch (sentiment) {
            case "Positive": color = Color.parseColor("#2E7D32"); break;
            case "Negative": color = Color.parseColor("#C62828"); break;
            default:         color = Color.parseColor("#E65100"); break;
        }
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(24f);
        bg.setColor(color);
        h.tvSentiment.setBackground(bg);
    }
    @Override public int getItemCount(){return list.size();}
    static class VH extends RecyclerView.ViewHolder{
        TextView tvFrom,tvTitle,tvMessage,tvDate,tvSentiment;
        VH(View v){super(v);tvFrom=v.findViewById(R.id.tvFrom);tvTitle=v.findViewById(R.id.tvTitle);
            tvMessage=v.findViewById(R.id.tvMessage);tvDate=v.findViewById(R.id.tvDate);
            tvSentiment=v.findViewById(R.id.tvSentiment);}
    }
}
