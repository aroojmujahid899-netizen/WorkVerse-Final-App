package com.workverse.app.activities.manager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.EmployeeAdapter;
import com.workverse.app.models.Employee;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;
public class ManagerTeamActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; EmployeeAdapter adapter;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_manager_team);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        rv=findViewById(R.id.recyclerView);pb=findViewById(R.id.progressBar);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter=new EmployeeAdapter(new ArrayList<>(),new EmployeeAdapter.OnEmployeeClickListener(){
            public void onEditClick(Employee e){}
            public void onDeleteClick(Employee e){}
            public void onItemClick(Employee e){}
        });
        rv.setAdapter(adapter);
        loadTeam();
    }
    private void loadTeam(){
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).get()
            .addOnSuccessListener(snap->{
                List<Employee> list=new ArrayList<>();
                for(QueryDocumentSnapshot d:snap){Employee e=d.toObject(Employee.class);e.setId(d.getId());list.add(e);}
                pb.setVisibility(View.GONE);
                adapter.updateList(list);
            }).addOnFailureListener(e->{pb.setVisibility(View.GONE);Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show();});
    }
}