package com.workverse.app.activities.employee;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.workverse.app.R;
import com.workverse.app.models.FAQ;
import com.workverse.app.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class ViewFAQActivity extends AppCompatActivity {

    LinearLayout containerFAQ;
    ProgressBar pb;
    TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_faq);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("FAQs");
        tb.setNavigationOnClickListener(v -> finish());

        containerFAQ = findViewById(R.id.containerFAQ);
        pb           = findViewById(R.id.progressBar);
        tvEmpty      = findViewById(R.id.tvEmpty);

        loadFAQs();
    }

    private void loadFAQs() {
        pb.setVisibility(View.VISIBLE);

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_FAQS)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(snap -> {
                pb.setVisibility(View.GONE);
                containerFAQ.removeAllViews();

                if (snap.isEmpty()) {
                    // Show default FAQs if Firestore is empty
                    loadDefaultFAQs();
                    return;
                }

                for (QueryDocumentSnapshot d : snap) {
                    FAQ faq = d.toObject(FAQ.class);
                    faq.setId(d.getId());
                    addFAQCard(faq.getQuestion(), faq.getAnswer(), faq.getCategory());
                }
                tvEmpty.setVisibility(View.GONE);
            })
            .addOnFailureListener(e -> {
                pb.setVisibility(View.GONE);
                // Load default FAQs on failure too
                loadDefaultFAQs();
            });
    }

    private void loadDefaultFAQs() {
        tvEmpty.setVisibility(View.GONE);
        // Default hardcoded FAQs
        addFAQCard("How do I mark my attendance?",
            "Go to Dashboard → Mark Attendance. Tap Check In when you arrive and Check Out when you leave. Attendance is saved automatically in real time.",
            "Attendance");

        addFAQCard("How do I apply for leave?",
            "Go to Dashboard → Apply Leave. Select leave type (Sick/Casual/Annual), choose dates, write reason and submit. Your manager will approve or reject.",
            "Leave");

        addFAQCard("Where can I see my performance and KPI?",
            "Go to Dashboard → Performance. You will see your KPI score calculated from attendance, sales, and AI-analyzed feedback.",
            "Performance");

        addFAQCard("How does the AI Feedback Analyzer work?",
            "When you submit feedback, the AI analyzes your text for sentiment (Positive/Negative/Neutral) and compares it with your actual call and sales data to generate a fair KPI score.",
            "AI & Feedback");

        addFAQCard("How do I submit my daily feedback?",
            "Go to Dashboard → Feedback. Write your title and feedback message about your day, calls, sales etc. and tap Submit.",
            "Feedback");

        addFAQCard("How do I check my leave status?",
            "Go to Dashboard → Leave → View Leave Status. You will see all your leave requests with status (Pending/Approved/Rejected).",
            "Leave");

        addFAQCard("Can I update my profile information?",
            "Yes. Go to Profile from the bottom navigation. Tap Edit Profile to update your name, phone number and other details.",
            "Profile");

        addFAQCard("How do I add my daily sales report?",
            "Go to Dashboard → Sales Report → tap the + button to add a new sale entry with date, amount, and description.",
            "Sales");

        addFAQCard("How will I receive notifications?",
            "Your manager or admin will send notifications about attendance reminders, leave approvals, announcements etc. Check the Notifications section.",
            "Notifications");

        addFAQCard("What should I do if I forget my password?",
            "On the login screen tap 'Forgot Password?' and enter your registered email. A reset link will be sent to your email.",
            "Account");
    }

    private void addFAQCard(String question, String answer, String category) {
        // Create card view programmatically
        View card = LayoutInflater.from(this).inflate(R.layout.item_faq, containerFAQ, false);

        TextView tvQuestion = card.findViewById(R.id.tvQuestion);
        TextView tvAnswer   = card.findViewById(R.id.tvAnswer);
        TextView tvCategory = card.findViewById(R.id.tvCategory);
        LinearLayout answerLayout = card.findViewById(R.id.layoutAnswer);

        tvQuestion.setText(question);
        tvAnswer.setText(answer);
        if (tvCategory != null) tvCategory.setText(category);

        // Toggle answer visibility on click
        card.setOnClickListener(v -> {
            if (answerLayout.getVisibility() == View.VISIBLE) {
                answerLayout.setVisibility(View.GONE);
            } else {
                answerLayout.setVisibility(View.VISIBLE);
            }
        });

        containerFAQ.addView(card);
    }
}
