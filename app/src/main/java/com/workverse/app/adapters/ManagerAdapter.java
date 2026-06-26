package com.workverse.app.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.workverse.app.R;
import com.workverse.app.models.Manager;
import java.util.List;
public class ManagerAdapter extends RecyclerView.Adapter<ManagerAdapter.VH> {
    public interface Listener { void onEdit(Manager m); void onDelete(Manager m); void onClick(Manager m); }
    private List<Manager> list; private Listener listener;
    public ManagerAdapter(List<Manager> list, Listener l){this.list=list;this.listener=l;}
    public void updateList(List<Manager> nl){list=nl;notifyDataSetChanged();}
    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int t){
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_manager,p,false));}
    @Override public void onBindViewHolder(@NonNull VH h, int pos){
        Manager m=list.get(pos);
        h.tvName.setText(m.getName()); h.tvEmail.setText(m.getEmail()!=null?m.getEmail():"");
        h.tvDepartment.setText(m.getDepartment()!=null?m.getDepartment():"");
        h.ivEdit.setOnClickListener(v->listener.onEdit(m));
        h.ivDelete.setOnClickListener(v->listener.onDelete(m));
        h.itemView.setOnClickListener(v->listener.onClick(m));
    }
    @Override public int getItemCount(){return list.size();}
    static class VH extends RecyclerView.ViewHolder{
        TextView tvName,tvEmail,tvDepartment; ImageView ivEdit,ivDelete;
        VH(View v){super(v);tvName=v.findViewById(R.id.tvName);tvEmail=v.findViewById(R.id.tvEmail);
            tvDepartment=v.findViewById(R.id.tvDepartment);ivEdit=v.findViewById(R.id.ivEdit);ivDelete=v.findViewById(R.id.ivDelete);}
    }
}