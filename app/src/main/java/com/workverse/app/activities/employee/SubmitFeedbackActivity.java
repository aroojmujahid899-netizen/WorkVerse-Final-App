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
import com.workverse.app.models.Feedback;
import com.workverse.app.utils.FeedbackAnalysisHelper;
import com.workverse.app.utils.FirebaseHelper;
import com.workverse.app.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;
public class SubmitFeedbackActivity extends AppCompatActivity {
    Button btnSubmit; ProgressBar pb;
    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_submit_feedback);
        Toolbar tb=findViewById(R.id.toolbar);setSupportActionBar(tb);tb.setNavigationOnClickListener(v->finish());
        btnSubmit=findViewById(R.id.btnSubmit);pb=findViewById(R.id.progressBar);
        List<TextInputEditText> eds=new ArrayList<>();
        findEditTexts((android.view.ViewGroup)((android.view.ViewGroup)findViewById(android.R.id.content)).getChildAt(0),eds);
        btnSubmit.setOnClickListener(v->{
            if(eds.size()<2){Toast.makeText(this,"Fill all fields",Toast.LENGTH_SHORT).show();return;}
            String title=eds.get(0).getText().toString().trim();
            String msg=eds.get(1).getText().toString().trim();
            if(TextUtils.isEmpty(title)||TextUtils.isEmpty(msg)){
                Toast.makeText(this,"All fields required",Toast.LENGTH_SHORT).show();return;}
            pb.setVisibility(View.VISIBLE);btnSubmit.setEnabled(false);
            SharedPrefManager spm=SharedPrefManager.getInstance(this);
            String userId = spm.getUid();
            Feedback fb=new Feedback(userId,spm.getFullName()!=null?spm.getFullName():"Employee",title,msg,"Employee");
            // Save first (fast, so the user isn't blocked on the AI call), then
            // call the REAL AI directly from the app (GeminiFeedbackService) and
            // write the result back onto this same document once it arrives.
            FirebaseHelper.getDb().collection(FirebaseHelper.COL_FEEDBACK).add(fb)
                    .addOnSuccessListener(r->{
                        pb.setVisibility(View.GONE);
                        Toast.makeText(this,"Feedback submitted! AI analysis in progress.",Toast.LENGTH_SHORT).show();
                        FeedbackAnalysisHelper.analyzeAndStore(r.getId(), userId, title, msg, null);
                        finish();
                    })
                    .addOnFailureListener(e->{pb.setVisibility(View.GONE);btnSubmit.setEnabled(true);
                        Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show();});
        });
    }
    private void findEditTexts(android.view.ViewGroup vg,List<TextInputEditText> list){
        for(int i=0;i<vg.getChildCount();i++){View c=vg.getChildAt(i);
            if(c instanceof TextInputEditText)list.add((TextInputEditText)c);
            else if(c instanceof android.view.ViewGroup)findEditTexts((android.view.ViewGroup)c,list);}
    }
}
