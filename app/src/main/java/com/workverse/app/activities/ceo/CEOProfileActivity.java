package com.workverse.app.activities.ceo;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.workverse.app.R;
import com.workverse.app.utils.SharedPrefManager;

public class CEOProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_ceo_profile);
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("CEO Profile");
        tb.setNavigationOnClickListener(v -> finish());

        SharedPrefManager spm = SharedPrefManager.getInstance(this);
        TextView tvName  = findViewById(R.id.tvName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvRole  = findViewById(R.id.tvRole);

        if (tvName  != null) tvName.setText(spm.getFullName() != null ? spm.getFullName() : "CEO");
        if (tvEmail != null) tvEmail.setText(spm.getEmail() != null ? spm.getEmail() : "—");
        if (tvRole  != null) tvRole.setText("Chief Executive Officer");
    }
}
