package com.workverse.app.activities.employee;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.workverse.app.R;
import com.workverse.app.utils.DateTimeUtils;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.HashMap;
import java.util.Map;
public class AddSaleActivity extends AppCompatActivity {
    TextInputEditText etDate, etMonth, etTarget, etAchieved;
    Button btnSubmit; ProgressBar pb;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_add_sale);
        Toolbar tb = findViewById(R.id.toolbar); setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());
        etDate     = findViewById(R.id.etDate);
        etMonth    = findViewById(R.id.etMonth);
        etTarget   = findViewById(R.id.etTarget);
        etAchieved = findViewById(R.id.etAchieved);
        btnSubmit  = findViewById(R.id.btnSubmit);
        pb         = findViewById(R.id.progressBar);
        // Pre-fill today's date
        etDate.setText(DateTimeUtils.getCurrentDate());
        btnSubmit.setOnClickListener(v -> saveSale());
    }
    private void saveSale() {
        String date     = etDate.getText().toString().trim();
        String month    = etMonth.getText().toString().trim();
        String targetStr   = etTarget.getText().toString().trim();
        String achievedStr = etAchieved.getText().toString().trim();
        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(month)
                || TextUtils.isEmpty(targetStr) || TextUtils.isEmpty(achievedStr)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        double target, achieved;
        try {
            target   = Double.parseDouble(targetStr);
            achieved = Double.parseDouble(achievedStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid amounts", Toast.LENGTH_SHORT).show();
            return;
        }
        pb.setVisibility(View.VISIBLE); btnSubmit.setEnabled(false);
        SharedPrefManager spm = SharedPrefManager.getInstance(this);
        Map<String, Object> data = new HashMap<>();
        data.put("userId",         spm.getUid());
        data.put("employeeName",   spm.getFullName() != null ? spm.getFullName() : "Employee");
        data.put("date",           date);
        data.put("month",          month);
        data.put("targetAmount",   target);
        data.put("achievedAmount", achieved);
        data.put("timestamp",      System.currentTimeMillis());
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES).add(data)
                .addOnSuccessListener(r -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Sale record saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE); btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}