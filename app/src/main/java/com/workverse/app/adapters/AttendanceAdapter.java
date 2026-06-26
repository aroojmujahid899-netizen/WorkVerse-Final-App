package com.workverse.app.adapters;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.Attendance;
import java.util.List;
public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.VH> {
    private List<Attendance> list;
    public AttendanceAdapter(List<Attendance> l){this.list=l;}
    public void updateList(List<Attendance> nl){list=nl;notifyDataSetChanged();}
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_attendance,p,false));}
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Attendance a=list.get(pos);
        h.tvName.setText(a.getEmployeeName());
        h.tvDate.setText(a.getDate());
        h.tvCheckIn.setText("In: "+(a.getCheckInTime()!=null?a.getCheckInTime():"—"));
        h.tvStatus.setText(a.getStatus());
        if("Present".equals(a.getStatus())){h.tvStatus.setTextColor(Color.parseColor("#2E7D32"));}
        else{h.tvStatus.setTextColor(Color.parseColor("#C62828"));}
    }
    @Override public int getItemCount(){return list.size();}
    static class VH extends RecyclerView.ViewHolder{
        TextView tvName,tvDate,tvCheckIn,tvStatus;
        VH(View v){super(v);tvName=v.findViewById(R.id.tvName);tvDate=v.findViewById(R.id.tvDate);
            tvCheckIn=v.findViewById(R.id.tvCheckIn);tvStatus=v.findViewById(R.id.tvStatus);}
    }
}