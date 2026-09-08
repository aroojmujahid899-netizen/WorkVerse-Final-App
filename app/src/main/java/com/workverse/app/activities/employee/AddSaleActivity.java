package com.workverse.app.activities.employee;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.workverse.app.R;
import com.workverse.app.models.SalesReport;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;

public class AddSaleActivity extends AppCompatActivity {

    private AutoCompleteTextView actDesignation, actCampaign;
    private TextInputEditText etTarget, etAchieved;
    private Button btnSubmit;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sale);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Sales Report");
        }
        tb.setNavigationOnClickListener(v -> finish());

        actDesignation = findViewById(R.id.actDesignation);
        actCampaign = findViewById(R.id.actCampaign);
        etTarget = findViewById(R.id.etTarget);
        etAchieved = findViewById(R.id.etAchieved);
        btnSubmit = findViewById(R.id.btnSubmit);
        pb = findViewById(R.id.progressBar);

        // Setup Designation Dropdown
        String[] designations = new String[]{"Fronters", "Verifiers", "Closers"};
        if (actDesignation != null) {
            actDesignation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, designations));
        }

        // Setup Campaign Dropdown
        String[] campaigns = new String[]{"MEDICARE", "FE", "Home Warranty"};
        if (actCampaign != null) {
            actCampaign.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, campaigns));
        }

        // Set default bi-monthly target if empty
        if (etTarget != null && TextUtils.isEmpty(etTarget.getText().toString().trim())) {
            etTarget.setText("30");
        }

        btnSubmit.setOnClickListener(v -> saveSale());
    }

    private void saveSale() {
        String designation = actDesignation != null ? actDesignation.getText().toString().trim() : "";
        String campaign = actCampaign != null ? actCampaign.getText().toString().trim() : "";
        String targetStr = etTarget != null ? etTarget.getText().toString().trim() : "30";
        String achievedStr = etAchieved != null ? etAchieved.getText().toString().trim() : "";

        if (TextUtils.isEmpty(designation) || TextUtils.isEmpty(campaign) || TextUtils.isEmpty(achievedStr)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int target, achieved;
        try {
            target = Integer.parseInt(targetStr);
            achieved = Integer.parseInt(achievedStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid integer counts", Toast.LENGTH_SHORT).show();
            return;
        }

        pb.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        SharedPrefManager spm = SharedPrefManager.getInstance(this);

        SalesReport report = new SalesReport();
        report.setEmployeeName(spm.getFullName() != null ? spm.getFullName() : "Employee");
        report.setDesignation(designation);
        report.setCampaign(campaign);
        report.setTargetAmount(target);
        report.setAchievedAmount(achieved);
        report.setTimestamp(System.currentTimeMillis());

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_SALES).add(report)
                .addOnSuccessListener(r -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(this, "Sale record saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pb.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}