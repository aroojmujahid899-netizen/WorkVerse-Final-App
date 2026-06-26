package com.workverse.app.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.SalesReport;
import java.util.List;
public class SalesReportAdapter extends RecyclerView.Adapter<SalesReportAdapter.VH> {
    private List<SalesReport> list;
    public SalesReportAdapter(List<SalesReport> l){this.list=l;}
    public void updateList(List<SalesReport> nl){list=nl;notifyDataSetChanged();}
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_sales_report,p,false));}
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        SalesReport s=list.get(pos);
        h.tvEmployee.setText(s.getEmployeeName());
        h.tvDate.setText(s.getDate());
        h.tvAchieved.setText("PKR "+String.format("%.0f",s.getAchievedAmount()));
        h.tvTarget.setText("Target: "+String.format("%.0f",s.getTargetAmount()));
    }
    @Override public int getItemCount(){return list.size();}
    static class VH extends RecyclerView.ViewHolder{
        TextView tvEmployee,tvDate,tvAchieved,tvTarget;
        VH(View v){super(v);tvEmployee=v.findViewById(R.id.tvEmployee);tvDate=v.findViewById(R.id.tvDate);
            tvAchieved=v.findViewById(R.id.tvAchieved);tvTarget=v.findViewById(R.id.tvTarget);}
    }
}