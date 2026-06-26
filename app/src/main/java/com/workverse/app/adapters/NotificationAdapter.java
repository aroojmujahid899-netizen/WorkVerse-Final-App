package com.workverse.app.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.Notification;
import com.workverse.app.utils.DateTimeUtils;
import java.util.List;
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {
    private List<Notification> list;
    public NotificationAdapter(List<Notification> l){this.list=l;}
    public void updateList(List<Notification> nl){list=nl;notifyDataSetChanged();}
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_notification,p,false));}
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Notification n=list.get(pos);
        h.tvTitle.setText(n.getTitle());
        h.tvMessage.setText(n.getMessage());
        h.tvTime.setText(DateTimeUtils.getTimeAgo(n.getTimestamp()));
    }
    @Override public int getItemCount(){return list.size();}
    static class VH extends RecyclerView.ViewHolder{
        TextView tvTitle,tvMessage,tvTime;
        VH(View v){super(v);tvTitle=v.findViewById(R.id.tvTitle);
            tvMessage=v.findViewById(R.id.tvMessage);tvTime=v.findViewById(R.id.tvTime);}
    }
}