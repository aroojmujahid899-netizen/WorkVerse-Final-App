package com.workverse.app.activities.admin;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.adapters.EmployeeAdapter;
import com.workverse.app.models.Employee;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;
public class ViewEmployeesActivity extends AppCompatActivity {
    RecyclerView rv; ProgressBar pb; TextView tvEmpty; TextInputEditText etSearch;
    FloatingActionButton fab; EmployeeAdapter adapter; List<Employee> allList=new ArrayList<>();
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_view_employees);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        rv=findViewById(R.id.recyclerView);pb=findViewById(R.id.progressBar);
        tvEmpty=findViewById(R.id.tvEmpty);etSearch=findViewById(R.id.etSearch);
        fab=findViewById(R.id.fabAdd);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter=new EmployeeAdapter(new ArrayList<>(), new EmployeeAdapter.OnEmployeeClickListener(){
            public void onEditClick(Employee e){Intent i=new Intent(ViewEmployeesActivity.this,EmployeeDetailActivity.class);i.putExtra("id",e.getId());startActivity(i);}
            public void onDeleteClick(Employee e){deleteEmployee(e);}
            public void onItemClick(Employee e){Intent i=new Intent(ViewEmployeesActivity.this,EmployeeDetailActivity.class);i.putExtra("id",e.getId());startActivity(i);}
        });
        rv.setAdapter(adapter);
        fab.setOnClickListener(v->startActivity(new Intent(this,AddEmployeeActivity.class)));
        etSearch.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence c,int a,int b,int d){}
            public void onTextChanged(CharSequence c,int a,int b,int d){filter(c.toString());}
            public void afterTextChanged(Editable e){}
        });
        loadEmployees();
    }
    @Override protected void onResume(){super.onResume();loadEmployees();}
    private void loadEmployees(){
        pb.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).get()
            .addOnSuccessListener(snap->{
                allList.clear();
                for(QueryDocumentSnapshot d:snap){Employee e=d.toObject(Employee.class);e.setId(d.getId());allList.add(e);}
                pb.setVisibility(View.GONE);
                tvEmpty.setVisibility(allList.isEmpty()?View.VISIBLE:View.GONE);
                adapter.updateList(new ArrayList<>(allList));
            })
            .addOnFailureListener(e->{pb.setVisibility(View.GONE);Toast.makeText(this,"Failed to load",Toast.LENGTH_SHORT).show();});
    }
    private void filter(String q){
        if(q.isEmpty()){adapter.updateList(new ArrayList<>(allList));return;}
        List<Employee> f=new ArrayList<>();
        for(Employee e:allList){if(e.getName()!=null&&e.getName().toLowerCase().contains(q.toLowerCase()))f.add(e);}
        adapter.updateList(f);
    }
    private void deleteEmployee(Employee e){
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_EMPLOYEES).document(e.getId()).delete()
            .addOnSuccessListener(r->loadEmployees())
            .addOnFailureListener(ex->Toast.makeText(this,"Delete failed",Toast.LENGTH_SHORT).show());
    }
}