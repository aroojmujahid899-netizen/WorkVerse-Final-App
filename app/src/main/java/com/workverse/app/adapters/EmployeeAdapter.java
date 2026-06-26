package com.workverse.app.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.Employee;
import java.util.List;
public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.VH> {
    public interface OnEmployeeClickListener {
        void onEditClick(Employee e);
        void onDeleteClick(Employee e);
        void onItemClick(Employee e);
    }
    private List<Employee> list;
    private OnEmployeeClickListener listener;
    public EmployeeAdapter(List<Employee> list, OnEmployeeClickListener l){this.list=list;this.listener=l;}
    public void updateList(List<Employee> newList){list=newList;notifyDataSetChanged();}
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t){
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_employee,p,false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Employee e = list.get(pos);
        h.tvName.setText(e.getName());
        h.tvDesignation.setText(e.getDesignation()!=null?e.getDesignation():"");
        h.tvDepartment.setText(e.getDepartment()!=null?e.getDepartment():"");
        h.ivEdit.setOnClickListener(v->listener.onEditClick(e));
        h.ivDelete.setOnClickListener(v->listener.onDeleteClick(e));
        h.itemView.setOnClickListener(v->listener.onItemClick(e));
    }
    @Override public int getItemCount(){return list.size();}
    static class VH extends RecyclerView.ViewHolder{
        TextView tvName,tvDesignation,tvDepartment; ImageView ivEdit,ivDelete;
        VH(View v){super(v);
            tvName=v.findViewById(R.id.tvName);tvDesignation=v.findViewById(R.id.tvDesignation);
            tvDepartment=v.findViewById(R.id.tvDepartment);
            ivEdit=v.findViewById(R.id.ivEdit);ivDelete=v.findViewById(R.id.ivDelete);}
    }
}